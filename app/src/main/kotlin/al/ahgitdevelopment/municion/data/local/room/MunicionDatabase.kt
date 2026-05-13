package al.ahgitdevelopment.municion.data.local.room

import al.ahgitdevelopment.municion.data.local.room.dao.AppPurchaseDao
import al.ahgitdevelopment.municion.data.local.room.dao.CompraDao
import al.ahgitdevelopment.municion.data.local.room.dao.GuiaDao
import al.ahgitdevelopment.municion.data.local.room.dao.LicenciaDao
import al.ahgitdevelopment.municion.data.local.room.dao.SyncOperationDao
import al.ahgitdevelopment.municion.data.local.room.dao.TiradaDao
import al.ahgitdevelopment.municion.data.local.room.entities.AppPurchase
import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.data.local.room.entities.SyncOperation
import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room Database para Munición App
 *
 * Reemplaza el legacy DataBaseSQLiteHelper.java
 *
 * FASE 2.2: Room Database implementation
 * - Database version 26 (migration desde v25)
 * - Export schema enabled para testing
 * - Migrations para preservar datos de usuarios
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
@Database(
    entities = [
        Guia::class,
        Compra::class,
        Licencia::class,
        Tirada::class,
        AppPurchase::class,
        SyncOperation::class
    ],
    version = 33,
    exportSchema = true
)
abstract class MunicionDatabase : RoomDatabase() {

    abstract fun guiaDao(): GuiaDao
    abstract fun compraDao(): CompraDao
    abstract fun licenciaDao(): LicenciaDao
    abstract fun tiradaDao(): TiradaDao
    abstract fun appPurchaseDao(): AppPurchaseDao
    abstract fun syncOperationDao(): SyncOperationDao

    companion object {
        private const val DATABASE_NAME = "municion.db"

        /**
         * MIGRATION: v29 → v30
         *
         * CAMBIO: Agregar columnas 'foto_url' y 'storage_path' a tabla licencias
         * para almacenar la URL de Firebase Storage y la ruta de borrado
         */
        /**
         * MIGRATION: v30 → v31
         *
         * CAMBIO: Agregar columnas 'foto_url' y 'storage_path' a tabla compras
         * para almacenar la URL de Firebase Storage y la ruta de borrado
         */
        val MIGRATION_30_31 = object : Migration(30, 31) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.i("MunicionDatabase", "Starting migration v30 → v31 (Add foto_url and storage_path to compras)")
                database.execSQL(
                    """
                    ALTER TABLE compras ADD COLUMN foto_url TEXT DEFAULT NULL
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    ALTER TABLE compras ADD COLUMN storage_path TEXT DEFAULT NULL
                    """.trimIndent()
                )
                Log.i("MunicionDatabase", "Migration v30 → v31 completed")
            }
        }

        /**
         * MIGRATION: v32 → v33
         *
         * Adds the sync-redesign columns to all four entity tables and creates
         * the [SyncOperation] outbox table.
         *
         * Added columns (per table):
         *   - sync_id TEXT NOT NULL DEFAULT ''  — UUID, assigned IN THIS MIGRATION
         *     via [SyncIdGenerator.deterministicSyncId]. Indexed UNIQUE afterwards.
         *   - deleted INTEGER NOT NULL DEFAULT 0  — tombstone flag.
         *   - deleted_at INTEGER  — tombstone timestamp.
         *   - data_quality TEXT NOT NULL DEFAULT 'ok'  — 'ok' | 'degraded' | 'lost'.
         *
         * Additionally, [compras] gets `guia_sync_id TEXT` (nullable) as the
         * future replacement for `id_pos_guia`. Both coexist during v3.3–v3.5.
         *
         * **Crucially, this migration is SYNCHRONOUS and SELF-CONTAINED**:
         * by the time it returns, every row already has a real deterministic
         * UUID in sync_id. No placeholders, no follow-up async work needed.
         * This closes the race window where a sync could fire between the
         * migration and a separate backfill pass, comparing local placeholder
         * syncIds against remote real UUIDs and treating every entity as new
         * (the bug in the first cut of the redesign).
         *
         * [SyncIdBackfill] is kept as an idempotent safety net.
         *
         * @see al.ahgitdevelopment.municion.data.sync.SyncIdGenerator
         */
        val MIGRATION_32_33 = object : Migration(32, 33) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.i("MunicionDatabase", "Starting migration v32 → v33 (sync redesign columns)")

                val entityTablesByType = linkedMapOf(
                    "licencias" to "Licencia",
                    "guias" to "Guia",
                    "compras" to "Compra",
                    "tiradas" to "Tirada"
                )

                for (table in entityTablesByType.keys) {
                    database.execSQL("ALTER TABLE $table ADD COLUMN sync_id TEXT NOT NULL DEFAULT ''")
                    database.execSQL("ALTER TABLE $table ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0")
                    database.execSQL("ALTER TABLE $table ADD COLUMN deleted_at INTEGER")
                    database.execSQL("ALTER TABLE $table ADD COLUMN data_quality TEXT NOT NULL DEFAULT 'ok'")
                }

                database.execSQL("ALTER TABLE compras ADD COLUMN guia_sync_id TEXT")

                // Assign deterministic UUIDs to every existing row. The same
                // (entityType, legacyId) pair always yields the same UUID, so
                // multiple devices observing the same legacy entity converge
                // on the same syncId without coordination. We compute the
                // UUID in Kotlin and UPDATE row-by-row inside the migration
                // transaction.
                for ((table, entityType) in entityTablesByType) {
                    val cursor = database.query("SELECT id FROM $table")
                    val rows = mutableListOf<Pair<Int, String>>()
                    cursor.use {
                        while (it.moveToNext()) {
                            val legacyId = it.getInt(0)
                            val syncId = al.ahgitdevelopment.municion.data.sync.SyncIdGenerator
                                .deterministicSyncId(entityType, legacyId)
                            rows += legacyId to syncId
                        }
                    }
                    for ((legacyId, syncId) in rows) {
                        database.execSQL(
                            "UPDATE $table SET sync_id = ? WHERE id = ?",
                            arrayOf<Any?>(syncId, legacyId)
                        )
                    }
                    Log.i("MunicionDatabase", "Migration v32→v33: assigned syncId to ${rows.size} rows in $table")
                }

                // Link compras.guia_sync_id to the parent guia's sync_id
                // (using the existing positional reference). Orphan compras
                // keep guia_sync_id = NULL and will surface as dataQuality
                // problems on the next sync.
                database.execSQL(
                    """
                    UPDATE compras
                    SET guia_sync_id = (
                        SELECT g.sync_id FROM guias g WHERE g.id = compras.id_pos_guia LIMIT 1
                    )
                    """.trimIndent()
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_compras_guia_sync_id ON compras(guia_sync_id)")

                // Now that every row has a real (unique-per-table) sync_id we
                // can create the UNIQUE index without conflicts.
                for (table in entityTablesByType.keys) {
                    database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_${table}_sync_id ON $table(sync_id)")
                }

                // Outbox table for the new write pipeline.
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS sync_outbox (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        entity_type TEXT NOT NULL,
                        entity_sync_id TEXT NOT NULL,
                        operation TEXT NOT NULL,
                        payload_json TEXT NOT NULL,
                        user_id TEXT NOT NULL,
                        created_at INTEGER NOT NULL,
                        last_attempt_at INTEGER,
                        retry_count INTEGER NOT NULL DEFAULT 0,
                        last_error TEXT,
                        status TEXT NOT NULL DEFAULT 'PENDING'
                    )
                    """.trimIndent()
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_sync_outbox_status ON sync_outbox(status)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_sync_outbox_entity_type_entity_sync_id ON sync_outbox(entity_type, entity_sync_id)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_sync_outbox_created_at ON sync_outbox(created_at)")

                Log.i("MunicionDatabase", "Migration v32 → v33 completed")
            }
        }

        val MIGRATION_31_32 = object : Migration(31, 32) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.i("MunicionDatabase", "Starting migration v31 → v32 (Add updated_at to all tables)")
                val now = System.currentTimeMillis()
                database.execSQL("ALTER TABLE guias ADD COLUMN updated_at INTEGER NOT NULL DEFAULT $now")
                database.execSQL("ALTER TABLE compras ADD COLUMN updated_at INTEGER NOT NULL DEFAULT $now")
                database.execSQL("ALTER TABLE licencias ADD COLUMN updated_at INTEGER NOT NULL DEFAULT $now")
                database.execSQL("ALTER TABLE tiradas ADD COLUMN updated_at INTEGER NOT NULL DEFAULT $now")
                Log.i("MunicionDatabase", "Migration v31 → v32 completed")
            }
        }

        val MIGRATION_29_30 = object : Migration(29, 30) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.i("MunicionDatabase", "Starting migration v29 → v30 (Add foto_url and storage_path to licencias)")
                database.execSQL(
                    """
                    ALTER TABLE licencias ADD COLUMN foto_url TEXT DEFAULT NULL
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    ALTER TABLE licencias ADD COLUMN storage_path TEXT DEFAULT NULL
                    """.trimIndent()
                )
                Log.i("MunicionDatabase", "Migration v29 → v30 completed")
            }
        }

        /**
         * MIGRATION: v28 → v29
         *
         * CAMBIO: Agregar columnas 'foto_url' y 'storage_path' a tabla guias
         * para almacenar la URL de Firebase Storage y la ruta de borrado
         */
        val MIGRATION_28_29 = object : Migration(28, 29) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.i("MunicionDatabase", "Starting migration v28 → v29 (Add foto_url and storage_path to guias)")
                database.execSQL(
                    """
                    ALTER TABLE guias ADD COLUMN foto_url TEXT DEFAULT NULL
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    ALTER TABLE guias ADD COLUMN storage_path TEXT DEFAULT NULL
                    """.trimIndent()
                )
                Log.i("MunicionDatabase", "Migration v28 → v29 completed")
            }
        }

        /**
         * MIGRATION: v27 → v28
         *
         * CAMBIO: Agregar columna 'modalidad' a tabla tiradas
         * para especificar tipo de puntuación: Precisión (0-600) o IPSC (0-100)
         */
        val MIGRATION_27_28 = object : Migration(27, 28) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.i("MunicionDatabase", "Starting migration v27 → v28 (Add modalidad to tiradas)")
                database.execSQL(
                    """
                    ALTER TABLE tiradas ADD COLUMN modalidad TEXT DEFAULT NULL
                    """.trimIndent()
                )
                Log.i("MunicionDatabase", "Migration v27 → v28 completed")
            }
        }

        /**
         * MIGRATION: v26 → v27
         *
         * CAMBIO: Agregar columna 'categoria' a tabla tiradas
         * para clasificar tiradas como Nacional, Autonómica o Local/Social
         */
        val MIGRATION_26_27 = object : Migration(26, 27) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.i("MunicionDatabase", "Starting migration v26 → v27 (Add categoria to tiradas)")
                database.execSQL(
                    """
                    ALTER TABLE tiradas ADD COLUMN categoria TEXT DEFAULT NULL
                    """.trimIndent()
                )
                Log.i("MunicionDatabase", "Migration v26 → v27 completed")
            }
        }

        /**
         * MIGRATION: v25 → v26
         *
         * CAMBIO: Agregar tabla app_purchases para almacenar compras in-app (remove ads)
         */
        val MIGRATION_25_26 = object : Migration(25, 26) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.i("MunicionDatabase", "Starting migration v25 → v26 (Add AppPurchase)")
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS app_purchases (
                        sku TEXT PRIMARY KEY NOT NULL,
                        purchaseToken TEXT NOT NULL,
                        purchaseTime INTEGER NOT NULL,
                        isAcknowledged INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                android.util.Log.i("MunicionDatabase", "Migration v25 → v26 completed")
            }
        }

        /**
         * MIGRATION: v24 → v25
         *
         * CAMBIO: Eliminar restricción UNIQUE en licencias.num_licencia
         * MOTIVO: Usuarios pueden tener múltiples licencias (tipos) con el mismo número (ej. DNI).
         */
        val MIGRATION_24_25 = object : Migration(24, 25) {
            override fun migrate(database: SupportSQLiteDatabase) {
                android.util.Log.i(
                    "MunicionDatabase",
                    "Starting migration v24 → v25 (Remove Unique Licencia)"
                )
                try {
                    // 1. Crear nueva tabla SIN índice único (pero con estructura idéntica)
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS licencias_new (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            tipo INTEGER NOT NULL,
                            nombre TEXT,
                            tipo_permiso_conduccion INTEGER NOT NULL DEFAULT -1,
                            edad INTEGER NOT NULL,
                            fecha_expedicion TEXT NOT NULL,
                            fecha_caducidad TEXT NOT NULL,
                            num_licencia TEXT NOT NULL,
                            num_abonado INTEGER NOT NULL DEFAULT -1,
                            num_seguro TEXT,
                            autonomia INTEGER NOT NULL DEFAULT -1,
                            escala INTEGER NOT NULL DEFAULT -1,
                            categoria INTEGER NOT NULL DEFAULT -1
                        )
                    """.trimIndent()
                    )

                    // 2. Copiar datos
                    database.execSQL(
                        """
                        INSERT INTO licencias_new (id, tipo, nombre, tipo_permiso_conduccion, edad,
                                                  fecha_expedicion, fecha_caducidad, num_licencia,
                                                  num_abonado, num_seguro, autonomia, escala, categoria)
                        SELECT id, tipo, nombre, tipo_permiso_conduccion, edad,
                               fecha_expedicion, fecha_caducidad, num_licencia,
                               num_abonado, num_seguro, autonomia, escala, categoria
                        FROM licencias
                    """
                    )

                    // 3. Eliminar tabla vieja y renombrar nueva
                    database.execSQL("DROP TABLE licencias")
                    database.execSQL("ALTER TABLE licencias_new RENAME TO licencias")

                    // 4. Recrear índices (num_licencia ahora es normal, NO unique)
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_licencias_num_licencia ON licencias(num_licencia)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_licencias_fecha_caducidad ON licencias(fecha_caducidad)")

                    android.util.Log.i("MunicionDatabase", "Migration v24 → v25 completed")
                } catch (e: Exception) {
                    android.util.Log.e(
                        "MunicionDatabase",
                        "Migration v24 → v25 failed: ${e.message}"
                    )
                    throw e
                }
            }
        }

        /**
         * CRITICAL MIGRATION: v23 (legacy SQLite) → v24 (Room)
         *
         * CAMBIOS PRINCIPALES:
         * 1. Compras.peso: TEXT → INTEGER (FIX del bug v2.0.2)
         * 2. Agregar índices para performance
         * 3. Copiar todos los datos existentes
         * 4. Validar integridad de datos
         *
         * IMPORTANTE: Esta migration preserva TODOS los datos del usuario.
         * Se ejecuta automáticamente cuando el usuario actualiza de v2.0.4 → v3.0.0
         */
        val MIGRATION_23_24 = object : Migration(23, 24) {
            override fun migrate(database: SupportSQLiteDatabase) {
                android.util.Log.i("MunicionDatabase", "========================================")
                android.util.Log.i("MunicionDatabase", "Starting CRITICAL migration v23 → v24")
                android.util.Log.i("MunicionDatabase", "========================================")

                try {
                    // ============================================================
                    // 1. BACKUP: Renombrar tablas antiguas
                    // ============================================================
                    android.util.Log.i("MunicionDatabase", "STEP 1: Backing up existing tables...")
                    database.execSQL("ALTER TABLE guias RENAME TO guias_old")
                    database.execSQL("ALTER TABLE compras RENAME TO compras_old")
                    database.execSQL("ALTER TABLE licencias RENAME TO licencias_old")
                    database.execSQL("ALTER TABLE tiradas RENAME TO tiradas_old")
                    android.util.Log.i("MunicionDatabase", "✓ Tables backed up successfully")

                    // ============================================================
                    // 2. CREAR NUEVAS TABLAS CON ROOM SCHEMA
                    // ============================================================
                    android.util.Log.i("MunicionDatabase", "STEP 2: Creating new Room tables...")

                    // Tabla GUIAS con índices
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS guias (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            id_compra INTEGER NOT NULL DEFAULT 0,
                            tipo_licencia INTEGER NOT NULL,
                            marca TEXT NOT NULL,
                            modelo TEXT NOT NULL,
                            apodo TEXT NOT NULL,
                            tipo_arma INTEGER NOT NULL,
                            calibre1 TEXT NOT NULL,
                            calibre2 TEXT,
                            num_guia TEXT NOT NULL,
                            num_arma TEXT NOT NULL,
                            cupo INTEGER NOT NULL,
                            gastado INTEGER NOT NULL DEFAULT 0,
                            image_path TEXT
                        )
                    """.trimIndent()
                    )
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_guias_tipo_licencia ON guias(tipo_licencia)")
                    database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_guias_num_guia ON guias(num_guia)")

                    // Tabla COMPRAS con índices (CRITICAL: peso es INTEGER)
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS compras (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            id_pos_guia INTEGER NOT NULL,
                            calibre1 TEXT NOT NULL,
                            calibre2 TEXT,
                            unidades INTEGER NOT NULL,
                            precio REAL NOT NULL,
                            fecha TEXT NOT NULL,
                            tipo TEXT NOT NULL,
                            peso INTEGER NOT NULL,
                            marca TEXT NOT NULL,
                            tienda TEXT,
                            valoracion REAL NOT NULL DEFAULT 0.0,
                            image_path TEXT
                        )
                    """.trimIndent()
                    )
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_compras_id_pos_guia ON compras(id_pos_guia)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_compras_fecha ON compras(fecha)")

                    // Tabla LICENCIAS con índices
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS licencias (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            tipo INTEGER NOT NULL,
                            nombre TEXT,
                            tipo_permiso_conduccion INTEGER NOT NULL DEFAULT -1,
                            edad INTEGER NOT NULL,
                            fecha_expedicion TEXT NOT NULL,
                            fecha_caducidad TEXT NOT NULL,
                            num_licencia TEXT NOT NULL,
                            num_abonado INTEGER NOT NULL DEFAULT -1,
                            num_seguro TEXT,
                            autonomia INTEGER NOT NULL DEFAULT -1,
                            escala INTEGER NOT NULL DEFAULT -1,
                            categoria INTEGER NOT NULL DEFAULT -1
                        )
                    """.trimIndent()
                    )
                    database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_licencias_num_licencia ON licencias(num_licencia)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_licencias_fecha_caducidad ON licencias(fecha_caducidad)")

                    // Tabla TIRADAS con índices
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS tiradas (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            descripcion TEXT NOT NULL,
                            rango TEXT,
                            fecha TEXT NOT NULL,
                            puntuacion REAL
                        )
                    """.trimIndent()
                    )
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_tiradas_fecha ON tiradas(fecha)")

                    android.util.Log.i("MunicionDatabase", "✓ New tables created with indices")

                    // ============================================================
                    // 3. COPIAR DATOS (con conversión de tipos donde sea necesario)
                    // ============================================================
                    android.util.Log.i(
                        "MunicionDatabase",
                        "STEP 3: Migrating data from old tables..."
                    )

                    // Copiar GUIAS (directo, sin cambios)
                    val guiasCount = database.compileStatement("SELECT COUNT(*) FROM guias_old")
                        .simpleQueryForLong()
                    database.execSQL(
                        """
                        INSERT INTO guias (id, id_compra, tipo_licencia, marca, modelo, apodo, tipo_arma,
                                          calibre1, calibre2, num_guia, num_arma, cupo, gastado, image_path)
                        SELECT id, id_compra, tipo_licencia, marca, modelo, apodo, tipo_arma,
                               calibre1, calibre2, num_guia, num_arma, cupo, gastado, image_path
                        FROM guias_old
                    """.trimIndent()
                    )
                    android.util.Log.i("MunicionDatabase", "✓ Migrated $guiasCount guias")

                    // Copiar COMPRAS (CRITICAL: convertir peso TEXT → INTEGER)
                    val comprasCount = database.compileStatement("SELECT COUNT(*) FROM compras_old")
                        .simpleQueryForLong()
                    database.execSQL(
                        """
                        INSERT INTO compras (id, id_pos_guia, calibre1, calibre2, unidades, precio, fecha,
                                            tipo, peso, marca, tienda, valoracion, image_path)
                        SELECT id, id_pos_guia, calibre1, calibre2, unidades, precio, fecha, tipo,
                               CAST(CASE
                                   WHEN peso = '' OR peso IS NULL THEN '0'
                                   ELSE peso
                               END AS INTEGER) as peso,
                               marca, tienda, valoracion, image_path
                        FROM compras_old
                    """.trimIndent()
                    )
                    android.util.Log.i(
                        "MunicionDatabase",
                        "✓ Migrated $comprasCount compras (peso TEXT→INTEGER)"
                    )

                    // Copiar LICENCIAS (directo)
                    val licenciasCount =
                        database.compileStatement("SELECT COUNT(*) FROM licencias_old")
                            .simpleQueryForLong()
                    database.execSQL(
                        """
                        INSERT INTO licencias (id, tipo, nombre, tipo_permiso_conduccion, edad,
                                              fecha_expedicion, fecha_caducidad, num_licencia,
                                              num_abonado, num_seguro, autonomia, escala, categoria)
                        SELECT id, tipo, nombre, tipo_permiso_conduccion, edad,
                               fecha_expedicion, fecha_caducidad, num_licencia,
                               num_abonado, num_seguro, autonomia, escala, categoria
                        FROM licencias_old
                    """.trimIndent()
                    )
                    android.util.Log.i("MunicionDatabase", "✓ Migrated $licenciasCount licencias")

                    // Copiar TIRADAS (directo)
                    val tiradasCount = database.compileStatement("SELECT COUNT(*) FROM tiradas_old")
                        .simpleQueryForLong()
                    database.execSQL(
                        """
                        INSERT INTO tiradas (id, descripcion, rango, fecha, puntuacion)
                        SELECT id, descripcion, rango, fecha, puntuacion
                        FROM tiradas_old
                    """.trimIndent()
                    )
                    android.util.Log.i("MunicionDatabase", "✓ Migrated $tiradasCount tiradas")

                    // ============================================================
                    // 4. ELIMINAR TABLAS ANTIGUAS
                    // ============================================================
                    android.util.Log.i("MunicionDatabase", "STEP 4: Cleaning up old tables...")
                    database.execSQL("DROP TABLE IF EXISTS guias_old")
                    database.execSQL("DROP TABLE IF EXISTS compras_old")
                    database.execSQL("DROP TABLE IF EXISTS licencias_old")
                    database.execSQL("DROP TABLE IF EXISTS tiradas_old")
                    android.util.Log.i("MunicionDatabase", "✓ Old tables removed")

                    // ============================================================
                    // 5. VALIDACIÓN FINAL
                    // ============================================================
                    android.util.Log.i("MunicionDatabase", "STEP 5: Validating migration...")
                    val finalGuias =
                        database.compileStatement("SELECT COUNT(*) FROM guias").simpleQueryForLong()
                    val finalCompras = database.compileStatement("SELECT COUNT(*) FROM compras")
                        .simpleQueryForLong()
                    val finalLicencias = database.compileStatement("SELECT COUNT(*) FROM licencias")
                        .simpleQueryForLong()
                    val finalTiradas = database.compileStatement("SELECT COUNT(*) FROM tiradas")
                        .simpleQueryForLong()

                    android.util.Log.i(
                        "MunicionDatabase",
                        "========================================"
                    )
                    android.util.Log.i(
                        "MunicionDatabase",
                        "MIGRATION v23→v24 COMPLETED SUCCESSFULLY"
                    )
                    android.util.Log.i("MunicionDatabase", "Final counts:")
                    android.util.Log.i("MunicionDatabase", "  - Guias: $finalGuias")
                    android.util.Log.i("MunicionDatabase", "  - Compras: $finalCompras")
                    android.util.Log.i("MunicionDatabase", "  - Licencias: $finalLicencias")
                    android.util.Log.i("MunicionDatabase", "  - Tiradas: $finalTiradas")
                    android.util.Log.i(
                        "MunicionDatabase",
                        "========================================"
                    )

                } catch (e: Exception) {
                    android.util.Log.e(
                        "MunicionDatabase",
                        "========================================"
                    )
                    android.util.Log.e("MunicionDatabase", "MIGRATION FAILED: ${e.message}")
                    android.util.Log.e(
                        "MunicionDatabase",
                        "========================================"
                    )
                    throw e  // Re-throw para que Room maneje el error
                }
            }
        }

        /**
         * Crea instancia del database (para uso en Hilt module)
         */
        fun create(context: Context): MunicionDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                MunicionDatabase::class.java,
                DATABASE_NAME
            )
                .addMigrations(
                    MIGRATION_23_24,
                    MIGRATION_24_25,
                    MIGRATION_25_26,
                    MIGRATION_26_27,
                    MIGRATION_27_28,
                    MIGRATION_28_29,
                    MIGRATION_29_30,
                    MIGRATION_30_31,
                    MIGRATION_31_32,
                    MIGRATION_32_33
                )
                .fallbackToDestructiveMigration(false)  // Solo como último recurso
                .build()
        }
    }
}
