# Rediseño del Subsistema de Sincronización Room ↔ Firebase

> **Estado:** ✅ Implementado en `feature/database-sync-refactor` (2026-05-13).
> **Versión objetivo:** v3.3.0 (consolidado en una sola release; no se desplegará hasta que el owner valide).
> **Motivación:** Bugs sistémicos en el sync actual están provocando pérdida de datos en usuarios de producción.

---

## Estado de implementación

Commits en el branch:

| Commit | Alcance |
|--------|---------|
| `c77eda1` | chore: bump versionCode a 45, versionName a 3.3.0, dependencias |
| `d650c02` | docs: este documento + .gitignore para reports |
| `f91b552` | feat(db): syncId, tombstones, outbox table, MIGRATION_32_33 |
| `b7796df` | feat(sync): outbox worker, tolerant parser, repositorios reescritos |
| `7afd0fc` | test+rules: 22 tests JVM verdes + reglas RTDB fase-1 |
| `3304bf6` | test+build: instrumented test SyncIdBackfill + schema export |

Componentes finales:

- ✅ `SyncIdGenerator` — UUID v4 random + UUID v3 determinista (`<table>:<id>`)
- ✅ `SyncOperation` + `SyncOperationDao` — tabla outbox con coalescing UPSERTs
- ✅ `SyncOutboxConfig` — tunables (batch, retries, backoff exponencial)
- ✅ `SyncOutboxEnqueuer` — JSON serialize entity → outbox row
- ✅ `SyncOutboxWorker` — drena outbox a Firebase, exponential backoff, marca SYNCED/RETRY/FAILED
- ✅ `SyncIdBackfill` — reemplaza placeholders post-migración con UUIDs deterministas; rellena `compras.guia_sync_id`
- ✅ `TolerantParsers` — nunca descarta entidades; marca `degraded`/`lost` en su lugar
- ✅ `FirebaseFormatMigrator` v2 → v3 — rekey de Int → syncId en Firebase, idempotente
- ✅ `TombstoneCleanupWorker` — purga diaria de tombstones >30d y outbox synced >7d
- ✅ Repositorios reescritos (Licencia/Guia/Compra/Tirada): write-path por outbox, read-path no destructivo
- ✅ `SyncDataUseCase` — orquesta downloads + dispara outbox; auto-fix y syncToFirebase neutralizados (no-op)
- ✅ `database.rules.json` — reglas RTDB fase-1 (per-user + validación de tipos críticos)
- ✅ `MunicionApplication` — Configuration.Provider para HiltWorkerFactory + boot de los workers + SyncIdBackfill

Tests:

- ✅ 22 tests JVM unitarios verdes (SyncIdGenerator, TolerantParsers, SyncOutboxConfig, SyncOutboxEnqueuer)
- ✅ Test instrumented (`SyncIdBackfillAndroidTest`) — 5 escenarios cubriendo idempotencia, determinismo cross-device, vinculación parent-child

Eliminado:

- ❌ `FirebaseSyncHelper` (raíz del Bug #2 + #3)
- ❌ `fullSyncToFirebase` (el `setValue(map)` que vaciaba colecciones)
- ❌ Auto-fix vía `syncFromFirebaseWithAutoFix` (camino destructivo del Bug #4)
- ❌ `workers/SyncWorker` (código muerto)

Próximo paso: validar el branch por el owner y desplegar.

---

## 1. Contexto

Munición v3.x implementa un sistema de sincronización Room (local) ↔ Firebase RTDB (nube) bajo el principio declarado de **"Room is the source of truth, Firebase is cloud backup"**. La implementación actual contradice ese principio en rutas críticas y provoca pérdida silenciosa de datos.

Tras una auditoría exhaustiva del código (`SyncDataUseCase`, `FirebaseSyncHelper`, los 4 `*Repository`, `MainViewModel`, `AuthViewModel`, `LegacyMigrationHelper`, `MunicionDatabase`, formularios y NavHost), se han identificado **11 bugs de diseño** que interactúan entre sí, y al menos **un camino catastrófico** que destruye datos tanto locales como remotos.

No es viable parchear individualmente: requiere rediseño del subsistema completo.

---

## 2. Bugs identificados

### 🔴 BUG #1 — Pérdida silenciosa por fallos de red en escritura
Los repositorios ignoran el `Result` de `syncHelper.writeEntity()`. Si Firebase falla (red, timeout, server error), Room queda con la entidad pero Firebase no, y el método devuelve `success`. La próxima ejecución de `syncFromFirebase` borra la entidad de Room (Bug #2).

**Archivos:** `LicenciaRepository.kt:56`, `GuiaRepository.kt:49`, `CompraRepository.kt:48`, `TiradaRepository.kt:56`.

### 🔴 BUG #2 — Diff sync borra entidades por parse errors transitorios
`FirebaseSyncHelper.syncFromFirebaseWithDiff` compara `localIds - remoteIds`, donde `remoteIds` sólo incluye entidades parseadas con éxito. Si Firebase tiene una entidad con un campo "blank or missing", el parser devuelve null, no se añade a `remoteIds`, y la entidad **se borra de Room** aunque exista en Firebase.

**Archivo:** `FirebaseSyncHelper.kt:155-187`.

### 🔴 BUG #3 — `fullSyncToFirebase` reemplaza la colección entera
`setValue(entityMaps)` en el path de la colección **sobrescribe todos los hijos**. Si `entityMaps` está vacío o le faltan entidades, se borran de Firebase. Combinado con Bug #2, una entidad corrupta en Firebase se acaba destruyendo en ambos sistemas.

**Archivo:** `FirebaseSyncHelper.kt:214-236`.

### 🔴 BUG #4 — `hasLocalData` se evalúa después del diff
`needsAutoFix = hasParseErrors && hasLocalData`, pero `hasLocalData` se consulta **después** de que el diff potencialmente vaciara Room. Si el diff borró todo, el auto-fix no salta y los datos se dan por perdidos. Si el diff borró sólo algunas, el auto-fix sube Room a Firebase y destruye también las eliminadas en Firebase.

**Archivos:** `LicenciaRepository.kt:107-138`, equivalentes en otros repos.

### 🔴 BUG #5 — Race condition entre sync inicial y escrituras del usuario
`MainActivity.LaunchedEffect(authState)` dispara `syncFromFirebase()` de forma asíncrona. La UI ya es usable: el usuario puede crear entidades mientras el sync captura el snapshot. El diff verá las nuevas entidades como "missing from remote" y las borrará.

**Archivo:** `MainActivity.kt:174-187`.

### 🔴 BUG #6 — Colisión de IDs entre dispositivos
`@PrimaryKey(autoGenerate = true)` + `OnConflictStrategy.REPLACE`: dos dispositivos asignan el mismo id (1, 2, 3...) a entidades distintas; la sincronización las sobrescribe mutuamente.

**Archivos:** `Licencia.kt:43`, `Guia.kt:37`, `Compra.kt:43`, `Tirada.kt:40`.

### 🔴 BUG #7 — Parsers demasiado estrictos para datos legacy
Los parsers fuerzan "blank or missing" como fatal en campos que en v2.x podían quedar vacíos (`apodo`, `descripcion`, etc.). Datos legacy quedan inparseables → caen en Bug #2 → se destruyen.

**Archivos:** `GuiaRepository.kt:244-272`, `CompraRepository.kt:180-225`, `LicenciaRepository.kt:194-207`, `TiradaRepository.kt:175-183`.

### 🔴 BUG #8 — Falsificación de `updatedAt` para datos legacy
Cuando una entidad de Firebase carece de `updatedAt`, el parser usa `System.currentTimeMillis()`. La comparación `remote.updatedAt >= local.updatedAt` casi siempre da true para legacy → siempre se machaca el local.

**Archivos:** todos los parsers, fallback `(map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()`.

### 🔴 BUG #9 — `permissionRequestedThisSession` bloquea re-sync tras logout/login
La variable `remember { mutableStateOf(false) }` persiste durante la composición. Logout + login en la misma sesión deja la app sin sincronizar; nuevas escrituras pueden destruir lo que ya existía en Firebase.

**Archivo:** `MainActivity.kt:157`.

### 🔴 BUG #10 — Caché RTDB + race con red intermitente
`setPersistenceEnabled(true)` permite que `.get()` devuelva caché stale durante reconexiones. Snapshot incompleto → diff borra entidades locales que sí existen en Firebase real.

**Archivo:** `MunicionApplication.kt:30`.

### 🔴 BUG #11 — Sin integridad referencial Compra→Guia
`Compra.idPosGuia` apunta a `Guia.id`. Si la Guia se borra remotamente o cambia de id (Bug #6), las Compras quedan huérfanas e invisibles.

**Archivos:** `Compra.kt:48`, `CreateCompraUseCase.kt:54`.

---

## 3. Causa raíz del borrado masivo observado

### 3.1. El "stability bug" (causa primaria, originada en v3.0.0–v3.1.1)

Auditoría de la RTDB (2026-05-13) reveló **31 usuarios con entidades corruptas reducidas a `{"stability": 0}`**. La causa fue identificada en el commit `0c0c2e7` (v3.1.2, 2025-12-01) que ya documentaba el problema:

> *"Stabilize Firebase synchronization for tiradas by creating a map, which prevents serialization errors related to the stability field."*

El bug original (v3.0.0–v3.1.1, ~Oct–Dic 2025) era que `setValue(kotlinObject)` directamente sobre `Tirada`/`Guia`/`Compra`/`Licencia` (clases Kotlin con `@Parcelize` y `@Serializable`) provocaba una serialización defectuosa donde el objeto entero quedaba como `{"stability": 0}` en Firebase. Las entidades originales se perdieron en el momento del save defectuoso.

El fix de v3.1.2 SOLO se aplicó a Tiradas (cambió a `mapOf("id" to ..., ...)`). Las otras 3 entidades quedaron con el bug hasta v3.2.x, momento en el que se migró a `toFirebaseMap()` (que sí evita el problema), pero **las entidades ya corruptas en Firebase quedaron así**.

Resultado: a fecha de hoy, 25 usuarios tienen al menos una colección con entidades `{"stability": 0}` esperando ser destruidas definitivamente por el bug actual.

### 3.2. La cadena catastrófica de v3.2.x (Bugs #2 + #3 + #4)

```
1. Una entidad en Firebase tiene un campo "blank/missing"
   (motivos: dato legacy, edición manual de Firebase Console, escritura parcial, race)
                                ▼
2. syncFromFirebaseWithDiff:
   - totalInFirebase = N > 0
   - remoteIds NO incluye la entidad corrupta (parse falló)
   - localIds sí la incluye (sincronizada anteriormente)
   - deleteLocal() de la entidad
                                ▼
3. needsAutoFix = hasParseErrors=true && hasLocalData=true → TRUE
                                ▼
4. syncToFirebase() → fullSyncToFirebase() ejecuta setValue(map) en la
   raíz de la colección. entityMaps = Room ACTUAL (sin la entidad borrada).
                                ▼
5. Firebase reemplaza la colección. Entidad corrupta DESAPARECE de Firebase.
   Cualquier otra entidad ausente en Room (por otras causas) también.
                                ▼
6. Pérdida permanente, irrecuperable, silenciosa.
```

---

## 4. Diseño nuevo

### 4.1. Principios

- **Sin operaciones destructivas durante sync.** El borrado sólo se propaga mediante tombstones explícitos.
- **IDs estables globales (UUID).** Los `Int autoGenerate` no coordinan entre dispositivos.
- **Parsing tolerante.** Una entidad con campos incompletos se preserva con valores por defecto y se marca `dataQuality='degraded'`.
- **Outbox pattern.** Toda escritura va primero a Room + outbox; un worker drena el outbox a Firebase con backoff.
- **`updatedAt` honesto.** Si una entidad de Firebase carece de timestamp, no se inventa; se trata como "vieja" y se respeta el local.
- **Sin `fullSyncToFirebase`.** Eliminado. Auto-fix por entidad si es necesario, jamás por colección.
- **Reglas RTDB defensivas.** El servidor rechaza writes malformados o que vacíen colecciones.

### 4.2. Modelo de IDs

Cada entidad gana un campo `syncId: String` (UUID) además del `id: Int` auto-increment local.

```kotlin
@Entity(
    tableName = "licencias",
    indices = [
        Index(value = ["num_licencia"]),
        Index(value = ["fecha_caducidad"]),
        Index(value = ["sync_id"], unique = true)  // NEW
    ]
)
data class Licencia(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,                                          // Local row id

    @ColumnInfo(name = "sync_id")
    val syncId: String = UUID.randomUUID().toString(),         // Global UUID

    // ... campos actuales ...

    @ColumnInfo(name = "deleted")
    val deleted: Boolean = false,                              // Tombstone flag

    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long? = null,                               // Tombstone ts

    @ColumnInfo(name = "data_quality")
    val dataQuality: String = "ok",                            // "ok" | "degraded"

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
```

`Compra.idPosGuia` (Int) se reemplaza por `guiaSyncId` (String). En la migración: por cada Compra, buscar la Guía por id local, copiar su syncId.

**Path en Firebase:** `users/{uid}/db/{collection}/{syncId}`.

### 4.3. Tombstones (soft delete)

Borrado = update con `deleted=true, deletedAt=now`. Queries de UI filtran tombstones.

```kotlin
@Query("SELECT * FROM licencias WHERE deleted = 0 ORDER BY fecha_caducidad ASC")
fun getAllLicenciasFlow(): Flow<List<Licencia>>

// Query interna para sync:
@Query("SELECT * FROM licencias")
suspend fun getAllIncludingDeleted(): List<Licencia>
```

Worker periódico borra tombstones con `deletedAt < now - 30 days` (sólo si Firebase también los tiene como tombstone).

### 4.4. Outbox

```kotlin
@Entity(tableName = "sync_outbox")
data class SyncOperation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String,           // "Licencia" | "Guia" | "Compra" | "Tirada"
    val entitySyncId: String,
    val operation: String,            // "UPSERT" | "DELETE"
    val payloadJson: String,
    val createdAt: Long,
    val lastAttemptAt: Long? = null,
    val retryCount: Int = 0,
    val lastError: String? = null,
    val status: String = "PENDING"    // PENDING | IN_FLIGHT | SYNCED | FAILED
)
```

Flujo transaccional de escritura:

```kotlin
@Transaction
suspend fun saveLicenciaWithOutbox(licencia: Licencia): Licencia {
    val stamped = licencia.copy(updatedAt = System.currentTimeMillis())
    val rowId = licenciaDao.insert(stamped)
    val saved = stamped.copy(id = rowId.toInt())
    outboxDao.enqueue(SyncOperation(
        entityType = "Licencia",
        entitySyncId = saved.syncId,
        operation = "UPSERT",
        payloadJson = Json.encodeToString(saved),
        createdAt = System.currentTimeMillis()
    ))
    return saved
}
```

`SyncOutboxWorker` (WorkManager, requiere red):
- Drena items `PENDING` en orden de inserción.
- Por cada item: marca `IN_FLIGHT` → ejecuta `setValue(payload)` en Firebase → marca `SYNCED` si OK; si error, incrementa `retryCount` con backoff exponencial.
- Si `retryCount > 10`: marca `FAILED` y emite notificación al usuario.

UI muestra contador de operaciones `PENDING` en la pantalla de settings.

### 4.5. Sync desde Firebase (no destructivo)

```kotlin
suspend fun syncFromFirebase(userId: String): SyncResult {
    val snapshot = firebaseDb.child("users/$userId/db/$entityPath").get().await()
    val localBySyncId = entityDao.getAllIncludingDeleted().associateBy { it.syncId }
    val remoteBySyncId = snapshot.children
        .mapNotNull { parseEntityTolerant(it.key, it.value) }
        .associateBy { it.syncId }

    val pendingOutboxIds = outboxDao.getPendingSyncIds(entityType)

    // Upsert: remoto MÁS RECIENTE que local Y sin outbox pendiente
    for ((syncId, remote) in remoteBySyncId) {
        if (syncId in pendingOutboxIds) {
            // La versión local está pendiente de subir → ganará
            continue
        }
        val local = localBySyncId[syncId]
        if (local == null || remote.updatedAt > local.updatedAt) {
            entityDao.upsert(remote)
        }
    }

    // NUNCA borrar entidades por ausencia en remoto.
    // Los tombstones llegan como entidades con deleted=true y se aplican
    // por la rama upsert de arriba.

    return SyncResult(...)
}
```

**Garantías:**
- Si una entidad no está en Firebase pero sí en Room, **no se borra**. Asumimos que es local-only pendiente de subir.
- Las escrituras pendientes en outbox tienen prioridad sobre lo remoto.
- Los tombstones (deleted=true) se propagan vía upsert normal.

### 4.6. Parsing tolerante

```kotlin
private fun parseLicenciaTolerant(itemKey: String, value: Any?): Licencia? {
    val map = value as? Map<String, Any?> ?: return null
    val syncId = (map["syncId"] as? String)?.takeIf { it.isNotBlank() }
        ?: itemKey.takeIf { isValidUuid(it) }
        ?: return null  // sin identidad estable → realmente irrecuperable

    val numLicencia = (map["numLicencia"] as? String).orEmpty()
    val fechaExpedicion = (map["fechaExpedicion"] as? String).orEmpty()
    val fechaCaducidad = (map["fechaCaducidad"] as? String).orEmpty()
    val tipo = (map["tipo"] as? Number)?.toInt() ?: 0
    val edad = (map["edad"] as? Number)?.toInt() ?: 18
    // ...

    val degraded = numLicencia.isBlank() ||
                   fechaExpedicion.isBlank() ||
                   fechaCaducidad.isBlank()

    return Licencia(
        syncId = syncId,
        tipo = tipo,
        numLicencia = numLicencia,
        fechaExpedicion = fechaExpedicion,
        fechaCaducidad = fechaCaducidad,
        edad = edad,
        // ...
        dataQuality = if (degraded) "degraded" else "ok",
        updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: 0L  // 0 = legacy sin ts
    )
}
```

Para entidades con `dataQuality='degraded'`, la UI muestra un banner: *"Esta entidad tiene datos incompletos. ¿Quieres revisarla?"* en lugar de borrarla.

### 4.7. Reglas Firebase RTDB

```json
{
  "rules": {
    "users": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid",
        "db": {
          "licencias": {
            "$syncId": {
              ".validate": "$syncId.length >= 16",
              "syncId":    { ".validate": "newData.val() === $syncId" },
              "updatedAt": { ".validate": "newData.isNumber()" },
              "deleted":   { ".validate": "newData.isBoolean()" }
            }
          },
          "guias":    { /* idem */ },
          "compras":  { /* idem */ },
          "tiradas":  { /* idem */ },
          "_meta": {
            "format_version": { ".validate": "newData.isNumber() && newData.val() >= 3" }
          }
        }
      }
    }
  }
}
```

Las reglas garantizan a nivel servidor que:
- Sólo el dueño accede a sus datos.
- Las claves son UUIDs (longitud ≥ 16).
- El `syncId` del payload coincide con la clave del path.
- No se puede degradar `format_version` a un valor inferior.

### 4.8. Migración de schema

#### Room v32 → v33

```kotlin
val MIGRATION_32_33 = object : Migration(32, 33) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 1. Backup defensivo: copia del fichero (manual o vía Application backup)

        // 2. Añadir columnas a las 4 tablas existentes
        listOf("licencias", "guias", "compras", "tiradas").forEach { table ->
            database.execSQL("ALTER TABLE $table ADD COLUMN sync_id TEXT NOT NULL DEFAULT ''")
            database.execSQL("ALTER TABLE $table ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE $table ADD COLUMN deleted_at INTEGER")
            database.execSQL("ALTER TABLE $table ADD COLUMN data_quality TEXT NOT NULL DEFAULT 'ok'")
        }

        // 3. Backfill sync_id con UUID determinista:
        //    UUID v5 con namespace fijo del proyecto + hash(table_name + id)
        //    De este modo, dos dispositivos con la misma entidad legacy
        //    (mismo id) convergen al mismo syncId tras la primera sync.
        //    Para entidades creadas tras la migración, UUID random (v4) está bien.
        // Implementado en código Kotlin tras la migración, en un onCreate hook.

        // 4. Crear tabla outbox
        database.execSQL("""
            CREATE TABLE sync_outbox (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                entityType TEXT NOT NULL,
                entitySyncId TEXT NOT NULL,
                operation TEXT NOT NULL,
                payloadJson TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                lastAttemptAt INTEGER,
                retryCount INTEGER NOT NULL DEFAULT 0,
                lastError TEXT,
                status TEXT NOT NULL DEFAULT 'PENDING'
            )
        """)
        database.execSQL("CREATE INDEX index_sync_outbox_status ON sync_outbox(status)")
        database.execSQL("CREATE INDEX index_sync_outbox_entity ON sync_outbox(entityType, entitySyncId)")

        // 5. Reemplazar Compra.idPosGuia (Int) por guiaSyncId (String):
        //    - Crear columna guia_sync_id TEXT
        //    - UPDATE compras SET guia_sync_id = (SELECT sync_id FROM guias WHERE id = compras.id_pos_guia)
        //    - Mantener id_pos_guia como columna legacy hasta v3.7 (limpieza)
        database.execSQL("ALTER TABLE compras ADD COLUMN guia_sync_id TEXT")
        // El backfill se ejecuta en código tras la migración.
    }
}
```

#### Firebase format v2 → v3

Worker `FirebaseSchemaMigratorV3` (idempotente, ejecutado una vez por usuario):

```
Por cada usuario en su primer arranque tras la actualización:
  1. Leer users/{uid}/db/_meta/format_version
  2. Si format_version >= 3: no-op
  3. Si format_version < 3:
     a) Para cada colección {licencias, guias, compras, tiradas}:
        - Leer la colección entera
        - Para cada entidad sin syncId: generar UUID v5 determinista (mismo namespace que la migración Room)
        - Escribir nueva key = syncId con payload enriquecido: { ...campos, syncId, deleted=false, updatedAt=now si ausente }
        - Si la key original era distinta (Int), borrar la entrada original
     b) Escribir users/{uid}/db/_meta/format_version = 3
```

Como el `syncId` se genera con UUID v5 determinista a partir del id legacy + tabla, **dos dispositivos del mismo usuario convergen al mismo syncId**, evitando duplicados al re-sincronizar.

---

## 5. Roadmap

| Versión | Duración | Entregable |
|---------|----------|------------|
| v3.5.0-alpha1 | 1 sem | Schema migration v32→v33, syncId backfill, parser tolerante. **Sin** outbox. Tests unitarios. |
| v3.5.0-alpha2 | 1 sem | Outbox + worker. Reescribir repositorios sobre el outbox. Tests integración (Firebase Emulator). |
| v3.5.0-alpha3 | 1 sem | Reescribir `syncFromFirebase` sin destrucción. Tombstones. Sync de prueba con datos reales de testing. |
| v3.5.0-beta1 | 1 sem | Reglas Firebase. QA manual. Documentación de cambios. |
| v3.5.0 | release | Deploy. Comunicación a usuarios afectados. |

**Total: ~4 semanas** asumiendo dedicación parcial.

---

## 6. Testing obligatorio

### 6.1. Unitarios
- `MigrationV32V33Test`: 100 entidades legacy → 100 con syncId estable.
- `ParserToleranceTest`: 50 entidades corruptas (campos vacíos, ausentes, tipos incorrectos) → ninguna se descarta.
- `OutboxQueueTest`: enqueue, dequeue, retry con backoff, marcado SYNCED.
- `SyncFromFirebaseTest`: el diff nunca borra entidades por ausencia.

### 6.2. Integración (Firebase Emulator)
- **S1 NetworkFailureOnWrite:** save → kill network → app restart → entidad sigue → red vuelve → outbox la sube.
- **S2 RemoteCorruptField:** Firebase tiene una entidad con `fechaCaducidad=""` → sync → entidad sigue local y remoto, marcada `degraded`.
- **S3 TwoDevicesConcurrent:** device A crea, device B crea → tras sync ambos ids únicos, ambos en Firebase.
- **S4 RemoteTombstone:** device A borra → device B sync → entidad desaparece de UI de B (queda tombstoned en Room).
- **S5 LogoutLogin:** signOut limpia Room → signIn vuelve a sync → todas las entidades vuelven.
- **S6 EmptyFirebaseFullRoom:** Firebase vacío, Room con 10 entidades → sync no borra Room → outbox sube las 10.
- **S7 MigrationIdempotent:** ejecutar migración 2 veces → 2ª ejecución es no-op.

### 6.3. Manual
Checklist firmado por QA antes de release.

---

## 7. Riesgos

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|--------------|---------|------------|
| Migración Firebase falla a mitad | Media | Alto | Migración idempotente; flag `_meta/format_version`; reintento automático |
| Outbox crece sin control offline | Baja | Medio | Cap a 1000 ops, notificación al usuario al alcanzar 80% |
| Pérdida durante migración Room v32→v33 | Baja | Catastrófico | Backup automático del fichero `.db` antes; rollback si falla |
| UUID v5 determinista no converge entre devices | Baja | Medio | Tests con misma combinación id+tabla en distintos devices |
| Reglas RTDB rompen clientes v3.x existentes | Media | Alto | Deploy de reglas DESPUÉS de que la migración v3.5 cubra >90% de usuarios |

---

## 8. Recuperación de usuarios afectados

### 8.1. Identificación (REALIZADA — 2026-05-13)
- **Backup completo RTDB:** `.backups/rtdb_snapshot_20260513_032030.json` (299 KB, 817 usuarios)
- **Lista de afectados:** `docs/affected_users.txt` (31 usuarios con problemas, ambos archivos gitignored)

Resultados de la auditoría:
- 925 cuentas en Firebase Auth (779 con email, 146 anónimas)
- 817 con datos en RTDB
- **25 usuarios HIGH (categoría A)**: tienen entidades `{"stability": 0}` corruptas - víctimas del stability-bug. Datos irrecuperables del propio Firebase.
- **6 usuarios MEDIUM (categoría B)**: inconsistencias estructurales (compras huérfanas, guías sin licencias)
- **5 usuarios "_meta only"**: probables víctimas de la cadena destructiva v3.2.x reciente (Mar-May 2026)
- **487 usuarios sin `db/`**: mayoría inactiva >1 año, pero 2 con sign-in reciente sospechoso

Crashlytics confirma: 8 eventos de `FirebaseParseException: [Guia] Field 'tipoLicencia' failed: Missing or invalid` el 2026-02-28 desde un único dispositivo (Xiaomi Redmi Note 13) que disparó la cadena destructiva contra sus 5 guías corruptas.

### 8.2. Comunicación
- Email a UIDs afectados antes del lanzamiento de v3.5.0 explicando el bug y disculpándose.
- In-app banner al usuario afectado tras actualizar: *"Detectamos que se perdieron datos en tu cuenta. Si los tenías en otro dispositivo, se restaurarán."*
- Si hay backups RTDB disponibles: restaurar manualmente por UID.

### 8.3. Recuperación automática parcial
Tras el rediseño, si un usuario afectado abre la app en un segundo dispositivo que aún tenga la caché RTDB local con los datos perdidos, el outbox subirá esos datos a Firebase y se restaurarán en todos los devices. Esto sólo funciona si:
- El usuario tiene un segundo dispositivo
- La caché local de RTDB aún contiene los datos (no fue purgada)

---

## 9. Cambios fuera del scope de Sync

Aunque no son estrictamente parte del rediseño, mientras tocamos este código conviene también:
- Eliminar el código muerto de `MainViewModel.syncToFirebase()` (no se llama desde UI).
- Mover el `permissionRequestedThisSession` flag de Compose state a SharedPreferences/DataStore (se pierde tras rotación de pantalla).
- Eliminar `SyncWorker` periódico cada 6h y reemplazar por trigger basado en outbox (cuando hay items PENDING, intenta sincronizar).
- Mejorar mensajes de UI: snackbars actuales son ambiguos.

Estos cambios irán en PRs separados etiquetados como `refactor:` en lugar de mezclarse con el rediseño de sync.

---

## 10. Decisiones tomadas

- **2026-05-13** @foliolo: rediseño completo aprobado, sin hotfix intermedio. Se desplegará sólo cuando la solución completa esté lista y probada.
- **2026-05-13** @foliolo: migraciones de schema autorizadas (Room + Firebase).
- **2026-05-13** @foliolo: la comunicación a usuarios se hará después del fix (Fase 0 de plan inicial reciclada como parte del release final).
- **2026-05-13** @foliolo: se requiere análisis de usuarios afectados; entregar lista en formato txt cuando esté disponible.

---

## 11. Apéndice: archivos a modificar

### Romper / Reescribir
- `app/src/main/kotlin/al/ahgitdevelopment/municion/data/sync/FirebaseSyncHelper.kt` — eliminar `fullSyncToFirebase`, reescribir `syncFromFirebaseWithDiff`.
- `app/src/main/kotlin/al/ahgitdevelopment/municion/domain/usecase/SyncDataUseCase.kt` — eliminar paths de auto-fix por colección.
- `app/src/main/kotlin/al/ahgitdevelopment/municion/data/repository/{Licencia,Guia,Compra,Tirada}Repository.kt` — usar outbox; parser tolerante; nunca silenciar fallos.

### Añadir
- `app/src/main/kotlin/al/ahgitdevelopment/municion/data/sync/SyncOutboxDao.kt`
- `app/src/main/kotlin/al/ahgitdevelopment/municion/data/sync/SyncOutboxWorker.kt`
- `app/src/main/kotlin/al/ahgitdevelopment/municion/data/sync/IdMigration.kt` — generación UUID v5 determinista
- `app/src/main/kotlin/al/ahgitdevelopment/municion/data/sync/FirebaseSchemaMigratorV3.kt` — migración format v2→v3
- `app/src/main/kotlin/al/ahgitdevelopment/municion/data/local/room/entities/SyncOperation.kt`
- `database.rules.json` — nuevas reglas RTDB

### Modificar
- `app/src/main/kotlin/al/ahgitdevelopment/municion/data/local/room/entities/{Licencia,Guia,Compra,Tirada}.kt` — añadir `syncId`, `deleted`, `deletedAt`, `dataQuality`.
- `app/src/main/kotlin/al/ahgitdevelopment/municion/data/local/room/dao/*.kt` — queries filtran tombstones por defecto.
- `app/src/main/kotlin/al/ahgitdevelopment/municion/data/local/room/MunicionDatabase.kt` — versión 33 + MIGRATION_32_33.
- `app/src/main/kotlin/al/ahgitdevelopment/municion/ui/MainActivity.kt` — `lastSyncedUserId` en DataStore en lugar de `remember`.
- `app/src/main/kotlin/al/ahgitdevelopment/municion/ui/viewmodel/MainViewModel.kt` — eliminar `syncToFirebase()` público; mostrar contador outbox.

### Tests nuevos
- `app/src/test/kotlin/al/ahgitdevelopment/municion/data/sync/SyncOutboxTest.kt`
- `app/src/test/kotlin/al/ahgitdevelopment/municion/data/sync/ParserToleranceTest.kt`
- `app/src/test/kotlin/al/ahgitdevelopment/municion/data/sync/SyncFromFirebaseTest.kt`
- `app/src/androidTest/kotlin/al/ahgitdevelopment/municion/data/local/room/MigrationV32V33Test.kt`
- `app/src/androidTest/kotlin/al/ahgitdevelopment/municion/data/sync/FirebaseEmulatorScenariosTest.kt`
