# Informe de migración Munición → KMP

Informe vivo (esquemático). Se actualiza al cerrar cada fase. Plan completo: `~/.claude/plans/a-continuacion-hay-que-scalable-music.md`.

Stack destino: Kotlin 2.4.0 · Compose MP 1.11.1 · AGP 9.2.1 · módulos `:shared` (KMP lib) + `:androidApp` + `iosApp`. Koin · Room KMP · GitLive Firebase (RTDB) · Navigation MP · Coil3 · multiplatform-settings · kotlinx-datetime/serialization · BuildKonfig (gmazzo) · FileKit · AdMob expect/actual · RevenueCat `purchases-kmp`.

---

## 1. Qué se ha migrado (checklist por fase)

- [ ] **Fase 0 — Fundación**
  - [x] 0.0 Limpieza de boilerplate de la plantilla (App demo, Greeting, tests dummy, drawable demo)
  - [x] 0.1 Catálogo de versiones alineado a FamilyFilm (adaptado: RTDB, purchases-kmp, WorkManager, ExifInterface)
  - [x] 0.2 `:shared` configurado (JVM17, Compose resources, BuildKonfig, Room/KSP por target, framework iOS estático)
  - [x] 0.3 `:androidApp` configurado (google-services + crashlytics, firma desde keystore.properties, manifest, AdMob)
  - [x] 0.4 Esqueleto Koin (`initKoin` idempotente, `platformModule` expect/actual, `MunicionApplication`, init iOS)
  - [x] 0.5 Theme (Color/Theme/Type) + drawables + i18n (es default / en) en composeResources; `Res` en `al.ahgitdevelopment.municion.resources`
  - [~] 0.6 Esqueleto Room KMP → **fusionado en Fase 2** (Room exige ≥1 entidad en `@Database`)
  - **Verificado:** `:androidApp:assembleDebug` ✅ · `:shared:linkDebugFrameworkIosSimulatorArm64` ✅
- [x] **Fase 1 — Capa Firebase común (GitLive)** (Kotlin) + init iOS Swift
  - `CrashReporter`, `AnalyticsTracker`/`FirebaseAnalyticsTracker` (lean), `CurrentUserIdProvider` (GitLive crashlytics/analytics/auth) cableados en Koin
  - `iOSApp.swift`: `FirebaseApp.configure()` + persistencia RTDB + App Check + Analytics
  - **Pendiente acción usuario:** añadir paquetes SPM de Firebase en Xcode + `GoogleService-Info.plist` (ver §4). El datasource RTDB se hace en Fase 3 (con el sync).
  - **Verificado (Kotlin):** Android ✅ · iOS link ✅. (Swift compila al añadir SPM.)
- [x] **Fase 2 — Entidades, DAOs, migraciones Room** (incluye 0.6)
  - 6 entidades a commonMain (sin `@Parcelize`/`Context`/Java dates; `@Serializable` + Room intactos; columnas idénticas para compatibilidad de schema)
  - utils comunes: `Time` (`nowMillis`), `DateUtils` (parse/format/diff "dd/MM/yyyy" con kotlinx-datetime), `NumberFormat`, **`Md5`** (MD5 puro) + `SyncIdGenerator` (v4 con `kotlin.uuid`, v3 byte-idéntico a Java)
  - 6 DAOs (Flow/suspend/@Transaction) + proyecciones `*SyncMeta`
  - `MunicionDatabase` v33 (@ConstructedBy) + 10 migraciones v23→v33 portadas a `SQLiteConnection`; builder con `BundledSQLiteDriver`; Room builder por plataforma (Android `getDatabasePath`, iOS `NSDocumentDirectory`)
  - **Verificado:** `:androidApp:assembleDebug` ✅ · `:shared:linkDebugFrameworkIosSimulatorArm64` ✅ · `:shared:testAndroidHostTest` (golden UUID) ✅
- [x] **Fase 3 — Repositorios + sync + SyncScheduler**
  - `MunicionRtdbDatasource` (GitLive RTDB): escritura tipada de entidad, lectura tipada a DTOs tolerantes
  - `SyncOutboxConfig`/`SyncOutboxEnqueuer`/`SyncOutboxDrainer` (drenado en commonMain), `TolerantParsers` (DTO→entidad), DTOs `@Serializable`
  - 4 repos (write: Room+outbox+drain; read: merge no destructivo, pending-wins, newer-wins)
  - `SyncScheduler` (interfaz común): `AndroidSyncScheduler` (WorkManager: periódico 15min + cleanup diario + on-demand) / `IosSyncScheduler` (foreground + on-demand)
  - `SyncDataUseCase` (download paralelo 4 colecciones), casos de uso Compra (cupo), `ClearLocalDataUseCase`
  - **Verificado:** Android `assembleDebug` ✅ · iOS link ✅ · host tests ✅
- [x] **Fase 4 — Autenticación (GitLive auth)**
  - `FirebaseAuthRepository` (create/signIn/link/migrateFromLegacy/reset/delete/reauth/signOut) sobre GitLive
  - `AuthViewModel` reacciona al `Flow authStateChanged` de GitLive (estados Loading/NotAuthenticated/RequiresMigration/Authenticated)
  - `Legacy{Converter,MigrationHelper}` (v2.x Android-only, código muerto) **descartados**
  - **Verificado:** Android ✅ · iOS link ✅
- [x] **Fase 5 — Navegación MP** — `NavRoutes` `@Serializable` con `id` (sin NavType custom Parcelable). NavHost real va con las pantallas (Fase 6).
- [~] **Fase 6 — Features por entidad** (EN CURSO)
  - [x] ViewModels de lista (Licencia/Guia/Compra/Tirada) + `MainViewModel` + `EntityUiState` (CRUD vía repos/usecases, `CurrentUserIdProvider`)
  - [ ] Componentes compartidos (TopBar/BottomBar/FAB/DatePickerField/DropdownField/Delete/EmptyState/DataQualityBanner) — **traducir APIs de recursos Android (`R`/`androidx...res`) → Compose MP (`Res`/`org.jetbrains.compose.resources`)**
  - [ ] Pantallas lista + items + diálogos de selección (Guia/Compra)
  - [ ] Formularios (screen/state/viewmodel) de las 4 entidades
  - [ ] Login + Migration screens (+ Login/Migration ViewModels)
  - [ ] MainScreen (Scaffold + bottom nav) + MunicionNavHost + MainActivity + App() iOS
- [ ] Fase 7 — Plataforma (imagen FileKit/cámara, AdMob expect/actual, RevenueCat purchases-kmp, calendario EventKit, FCM)
- [ ] Fase 8 — Settings, tutorial, calidad de datos
- [ ] Fase 9 — Tests (portar a commonTest + mokkery)
- [ ] Fase 10 — CI/CD (Fastlane + GitHub Actions)
- [ ] Fase 11 — Finalización Xcode (SPM, Info.plist, entitlements, iconos)

---

## 2. Problemas encontrados (y resolución)

| # | Problema | Resolución |
|---|----------|------------|
| 1 | AGP 9: `resValue` falla con "feature is disabled" | Habilitado `buildFeatures { resValues = true }` en `:androidApp`. |
| 2 | Deps gestionadas por BOM sin versión en `:androidApp` (`koin-android`, `firebase-appcheck-debug`) | Importado el BOM de Koin en `:androidApp`; App Check Debug diferido a Fase 1 (con firebase BOM). |
| 3 | `play-services-ads` crashea al arrancar si `AdMob APPLICATION_ID` está vacío | `resValue admob_app_id` con fallback al **AdMob test app id** hasta tener el real. |
| 4 | Sin `google-services.json` (proyecto `municion-95caa` no accesible) | Creado **placeholder** gitignored para builds locales; el real lo aporta el usuario / CI (ver §4). |
| 5 | `keystore.properties` no estaba en `.gitignore` (contiene contraseñas) | Añadido a `.gitignore`. |
| 6 | `.mcp.json` con tokens GitHub en claro, `.backups/` (RTDB prod) y `docs/affected_users.txt` (PII) sin trackear | Excluidos del commit + `.gitignore`. **Rotar tokens GitHub (acción del usuario).** |
| 7 | `UUID.nameUUIDFromBytes` (UUID v3/MD5) no existe en KMP | MD5 puro en Kotlin (`util/Md5.kt`) + golden test contra vectores de Java. ✅ idéntico. |
| 8 | `getAllTimestamps()` usaba `Map`/`@MapColumn` (soporte dudoso en Room KMP, además @deprecated) | Eliminado de los DAOs (el sync v3.5+ usa `getAllSyncMetadata`). |
| 9 | `Dispatchers.IO` es JVM-only (interno en Native) | `setQueryCoroutineContext(Dispatchers.Default)` (multiplataforma). |
| 10 | Lectura RTDB cruda corrompería datos en iOS (`NSNumber` no es `kotlin.Number`) | Lectura con **deserialización tipada GitLive a DTOs tolerantes**; `TolerantParsers` opera sobre el DTO. (mejora) |
| 11 | `withTransaction` (room-ktx) es Android-only; no hay transacción cross-DAO en Room KMP | insert+enqueue **secuencial** (Room es source of truth; sin pérdida — entidad reenviada en siguiente edición si el proceso muere entre medias). Endurecer con `useWriterConnection` más adelante. |
| 12 | `SyncDataUseCase` dependía de auth/billing/FirebaseFormatMigrator (otras fases) | Simplificado a 4 repos + `CurrentUserIdProvider` + `SyncScheduler`. Reconcile de ads se cablea en Fase 7; `FirebaseFormatMigrator` descartado (parsing tolerante + syncId determinista lo subsumen). |
| 13 | `@HiltWorker` (Hilt) | Workers `CoroutineWorker` + `KoinComponent` (factory por defecto de WorkManager; sin Configuration.Provider). |

**Avisos (no bloqueantes, vigilar):**
- Skiko: `coil3 3.4.0` arrastra skiko 0.9.22.2 vs Compose MP 0.144.6 (resuelve a la mayor; vigilar render de imágenes en iOS).
- `androidLibrary {}` deprecado a favor de `android {}` en el plugin KMP-library (solo warning; mantenido como lo generó el wizard).
- iOS framework: bundleId no inferido (cosmético; se puede fijar con `-Xbinary=bundleId`).

---

## 3. Mejoras hechas (no rompedoras)

- `deterministicSyncId` se replanteará con MD5/v3 multiplataforma + golden tests (Fase 3) para garantizar convergencia cross-device (Java `UUID.nameUUIDFromBytes` no existe en KMP).
- Navegación: se sustituirá el paso de entidad completa (Parcelable+Base64, Android-only) por paso de `id`/`syncId` y carga en el ViewModel (Fase 5).
- Eliminadas dependencias declaradas sin uso (biometric, security-crypto) — pendiente confirmar.
- Fuentes **Raleway omitidas**: el theme de `develop` (`Type.kt`) usa `FontFamily.Default`, no las fuentes de assets (eran del XML antiguo). Sin pérdida visual.
- `selector.xml` (drawable `<selector>` del bottom-nav XML antiguo) no portado (Compose no usa state-list drawables).

---

## 4. Guías de configuración externa (paso a paso)

> Pendiente de completar al avanzar las fases. Resumen de lo necesario:

- **Firebase** (`municion-95caa`, no accesible desde la cuenta CLI/MCP actual): registrar app iOS (bundle `al.ahgitdevelopment.municion`) → `GoogleService-Info.plist`; aportar `google-services.json` real (Android) → reemplaza el placeholder y se publica como secret `FIREBASE_JSON` (base64); habilitar RTDB/Storage/Auth(Email+Anónimo)/Crashlytics/Analytics/FCM.
- **Xcode — SPM Firebase (Fase 1):** en Xcode → File → Add Package Dependencies → `https://github.com/firebase/firebase-ios-sdk` (Up to Next Major). Añadir al target `iosApp` los productos: `FirebaseCore`, `FirebaseAuth`, `FirebaseDatabase`, `FirebaseStorage`, `FirebaseCrashlytics`, `FirebaseAnalytics`, `FirebaseMessaging`, `FirebaseAppCheck`. Arrastrar `GoogleService-Info.plist` a `iosApp/iosApp/` (Target Membership = iosApp). Sin Podfile. (`iOSApp.swift` ya hace `FirebaseApp.configure()` + persistencia RTDB.)
- **AdMob:** apps iOS+Android, unidades banner → `ADMOB_APPLICATION_ID(_IOS)`, `ADMOB_BOTTOM_BANNER_ID(_IOS)` en `local.properties`/secrets.
- **RevenueCat:** proyecto, productos remove-ads (Play + App Store) → entitlement `ad_free`; claves SDK.
- **Apple Developer / App Store Connect, Google Play Console, GitHub secrets, Xcode (SPM Firebase/AdMob), keystore:** detalle en fases 7/10/11.
