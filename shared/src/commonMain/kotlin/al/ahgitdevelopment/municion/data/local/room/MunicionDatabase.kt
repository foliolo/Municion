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
import al.ahgitdevelopment.municion.data.sync.SyncIdGenerator
import al.ahgitdevelopment.municion.util.nowMillis
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.coroutines.Dispatchers

const val MUNICION_DATABASE_NAME = "municion.db"

@Database(
    entities = [
        Guia::class,
        Compra::class,
        Licencia::class,
        Tirada::class,
        AppPurchase::class,
        SyncOperation::class,
    ],
    version = 33,
    exportSchema = true,
)
@ConstructedBy(MunicionDatabaseConstructor::class)
abstract class MunicionDatabase : RoomDatabase() {
    abstract fun guiaDao(): GuiaDao

    abstract fun compraDao(): CompraDao

    abstract fun licenciaDao(): LicenciaDao

    abstract fun tiradaDao(): TiradaDao

    abstract fun appPurchaseDao(): AppPurchaseDao

    abstract fun syncOperationDao(): SyncOperationDao
}

// Room KSP generates the actual per platform.
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object MunicionDatabaseConstructor : RoomDatabaseConstructor<MunicionDatabase> {
    override fun initialize(): MunicionDatabase
}

/** Builds the database with all migrations and the bundled SQLite driver. */
fun buildMunicionDatabase(builder: RoomDatabase.Builder<MunicionDatabase>): MunicionDatabase =
    builder
        .addMigrations(*MUNICION_MIGRATIONS)
        .setDriver(BundledSQLiteDriver())
        // Dispatchers.IO is JVM-only; Default is multiplatform and fine for this DB's load.
        .setQueryCoroutineContext(Dispatchers.Default)
        .build()

private val MUNICION_MIGRATIONS: Array<Migration>
    get() =
        arrayOf(
            MIGRATION_23_24,
            MIGRATION_24_25,
            MIGRATION_25_26,
            MIGRATION_26_27,
            MIGRATION_27_28,
            MIGRATION_28_29,
            MIGRATION_29_30,
            MIGRATION_30_31,
            MIGRATION_31_32,
            MIGRATION_32_33,
        )

/**
 * CRITICAL: legacy SQLite (v23) → Room (v24). Preserves all user data and fixes the
 * `compras.peso` TEXT→INTEGER bug.
 */
private val MIGRATION_23_24 =
    object : Migration(23, 24) {
        override fun migrate(connection: SQLiteConnection) {
            // 1. Back up old tables
            connection.execSQL("ALTER TABLE guias RENAME TO guias_old")
            connection.execSQL("ALTER TABLE compras RENAME TO compras_old")
            connection.execSQL("ALTER TABLE licencias RENAME TO licencias_old")
            connection.execSQL("ALTER TABLE tiradas RENAME TO tiradas_old")

            // 2. Create Room tables
            connection.execSQL(
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
                """.trimIndent(),
            )
            connection.execSQL("CREATE INDEX IF NOT EXISTS index_guias_tipo_licencia ON guias(tipo_licencia)")
            connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_guias_num_guia ON guias(num_guia)")

            connection.execSQL(
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
                """.trimIndent(),
            )
            connection.execSQL("CREATE INDEX IF NOT EXISTS index_compras_id_pos_guia ON compras(id_pos_guia)")
            connection.execSQL("CREATE INDEX IF NOT EXISTS index_compras_fecha ON compras(fecha)")

            connection.execSQL(
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
                """.trimIndent(),
            )
            connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_licencias_num_licencia ON licencias(num_licencia)")
            connection.execSQL("CREATE INDEX IF NOT EXISTS index_licencias_fecha_caducidad ON licencias(fecha_caducidad)")

            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS tiradas (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    descripcion TEXT NOT NULL,
                    rango TEXT,
                    fecha TEXT NOT NULL,
                    puntuacion REAL
                )
                """.trimIndent(),
            )
            connection.execSQL("CREATE INDEX IF NOT EXISTS index_tiradas_fecha ON tiradas(fecha)")

            // 3. Copy data (peso TEXT→INTEGER)
            connection.execSQL(
                """
                INSERT INTO guias (id, id_compra, tipo_licencia, marca, modelo, apodo, tipo_arma,
                                   calibre1, calibre2, num_guia, num_arma, cupo, gastado, image_path)
                SELECT id, id_compra, tipo_licencia, marca, modelo, apodo, tipo_arma,
                       calibre1, calibre2, num_guia, num_arma, cupo, gastado, image_path
                FROM guias_old
                """.trimIndent(),
            )
            connection.execSQL(
                """
                INSERT INTO compras (id, id_pos_guia, calibre1, calibre2, unidades, precio, fecha,
                                     tipo, peso, marca, tienda, valoracion, image_path)
                SELECT id, id_pos_guia, calibre1, calibre2, unidades, precio, fecha, tipo,
                       CAST(CASE WHEN peso = '' OR peso IS NULL THEN '0' ELSE peso END AS INTEGER) AS peso,
                       marca, tienda, valoracion, image_path
                FROM compras_old
                """.trimIndent(),
            )
            connection.execSQL(
                """
                INSERT INTO licencias (id, tipo, nombre, tipo_permiso_conduccion, edad,
                                       fecha_expedicion, fecha_caducidad, num_licencia,
                                       num_abonado, num_seguro, autonomia, escala, categoria)
                SELECT id, tipo, nombre, tipo_permiso_conduccion, edad,
                       fecha_expedicion, fecha_caducidad, num_licencia,
                       num_abonado, num_seguro, autonomia, escala, categoria
                FROM licencias_old
                """.trimIndent(),
            )
            connection.execSQL(
                """
                INSERT INTO tiradas (id, descripcion, rango, fecha, puntuacion)
                SELECT id, descripcion, rango, fecha, puntuacion
                FROM tiradas_old
                """.trimIndent(),
            )

            // 4. Drop old tables
            connection.execSQL("DROP TABLE IF EXISTS guias_old")
            connection.execSQL("DROP TABLE IF EXISTS compras_old")
            connection.execSQL("DROP TABLE IF EXISTS licencias_old")
            connection.execSQL("DROP TABLE IF EXISTS tiradas_old")
        }
    }

/** Removes the UNIQUE constraint on licencias.num_licencia. */
private val MIGRATION_24_25 =
    object : Migration(24, 25) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
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
                """.trimIndent(),
            )
            connection.execSQL(
                """
                INSERT INTO licencias_new (id, tipo, nombre, tipo_permiso_conduccion, edad,
                                           fecha_expedicion, fecha_caducidad, num_licencia,
                                           num_abonado, num_seguro, autonomia, escala, categoria)
                SELECT id, tipo, nombre, tipo_permiso_conduccion, edad,
                       fecha_expedicion, fecha_caducidad, num_licencia,
                       num_abonado, num_seguro, autonomia, escala, categoria
                FROM licencias
                """.trimIndent(),
            )
            connection.execSQL("DROP TABLE licencias")
            connection.execSQL("ALTER TABLE licencias_new RENAME TO licencias")
            connection.execSQL("CREATE INDEX IF NOT EXISTS index_licencias_num_licencia ON licencias(num_licencia)")
            connection.execSQL("CREATE INDEX IF NOT EXISTS index_licencias_fecha_caducidad ON licencias(fecha_caducidad)")
        }
    }

/** Adds the app_purchases table. */
private val MIGRATION_25_26 =
    object : Migration(25, 26) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS app_purchases (
                    sku TEXT PRIMARY KEY NOT NULL,
                    purchaseToken TEXT NOT NULL,
                    purchaseTime INTEGER NOT NULL,
                    isAcknowledged INTEGER NOT NULL
                )
                """.trimIndent(),
            )
        }
    }

private val MIGRATION_26_27 =
    object : Migration(26, 27) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL("ALTER TABLE tiradas ADD COLUMN categoria TEXT DEFAULT NULL")
        }
    }

private val MIGRATION_27_28 =
    object : Migration(27, 28) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL("ALTER TABLE tiradas ADD COLUMN modalidad TEXT DEFAULT NULL")
        }
    }

private val MIGRATION_28_29 =
    object : Migration(28, 29) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL("ALTER TABLE guias ADD COLUMN foto_url TEXT DEFAULT NULL")
            connection.execSQL("ALTER TABLE guias ADD COLUMN storage_path TEXT DEFAULT NULL")
        }
    }

private val MIGRATION_29_30 =
    object : Migration(29, 30) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL("ALTER TABLE licencias ADD COLUMN foto_url TEXT DEFAULT NULL")
            connection.execSQL("ALTER TABLE licencias ADD COLUMN storage_path TEXT DEFAULT NULL")
        }
    }

private val MIGRATION_30_31 =
    object : Migration(30, 31) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL("ALTER TABLE compras ADD COLUMN foto_url TEXT DEFAULT NULL")
            connection.execSQL("ALTER TABLE compras ADD COLUMN storage_path TEXT DEFAULT NULL")
        }
    }

/** Adds updated_at to all entity tables. */
private val MIGRATION_31_32 =
    object : Migration(31, 32) {
        override fun migrate(connection: SQLiteConnection) {
            val now = nowMillis()
            connection.execSQL("ALTER TABLE guias ADD COLUMN updated_at INTEGER NOT NULL DEFAULT $now")
            connection.execSQL("ALTER TABLE compras ADD COLUMN updated_at INTEGER NOT NULL DEFAULT $now")
            connection.execSQL("ALTER TABLE licencias ADD COLUMN updated_at INTEGER NOT NULL DEFAULT $now")
            connection.execSQL("ALTER TABLE tiradas ADD COLUMN updated_at INTEGER NOT NULL DEFAULT $now")
        }
    }

/**
 * Sync redesign: adds sync_id/deleted/deleted_at/data_quality to every entity table,
 * assigns a deterministic UUID to every existing row, links compras.guia_sync_id and
 * creates the sync_outbox table. Self-contained: by the time it returns every row has a
 * real, unique sync_id.
 */
private val MIGRATION_32_33 =
    object : Migration(32, 33) {
        override fun migrate(connection: SQLiteConnection) {
            val entityTablesByType =
                linkedMapOf(
                    "licencias" to "Licencia",
                    "guias" to "Guia",
                    "compras" to "Compra",
                    "tiradas" to "Tirada",
                )

            for (table in entityTablesByType.keys) {
                connection.execSQL("ALTER TABLE $table ADD COLUMN sync_id TEXT NOT NULL DEFAULT ''")
                connection.execSQL("ALTER TABLE $table ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0")
                connection.execSQL("ALTER TABLE $table ADD COLUMN deleted_at INTEGER")
                connection.execSQL("ALTER TABLE $table ADD COLUMN data_quality TEXT NOT NULL DEFAULT 'ok'")
            }
            connection.execSQL("ALTER TABLE compras ADD COLUMN guia_sync_id TEXT")

            // Assign deterministic UUIDs to every existing row.
            for ((table, entityType) in entityTablesByType) {
                val ids = mutableListOf<Int>()
                connection.prepare("SELECT id FROM $table").use { stmt ->
                    while (stmt.step()) ids.add(stmt.getLong(0).toInt())
                }
                for (legacyId in ids) {
                    val syncId = SyncIdGenerator.deterministicSyncId(entityType, legacyId)
                    connection.prepare("UPDATE $table SET sync_id = ? WHERE id = ?").use { stmt ->
                        stmt.bindText(1, syncId)
                        stmt.bindLong(2, legacyId.toLong())
                        stmt.step()
                    }
                }
            }

            // Link compras.guia_sync_id to the parent guia's sync_id via the positional reference.
            connection.execSQL(
                """
                UPDATE compras
                SET guia_sync_id = (
                    SELECT g.sync_id FROM guias g WHERE g.id = compras.id_pos_guia LIMIT 1
                )
                """.trimIndent(),
            )
            connection.execSQL("CREATE INDEX IF NOT EXISTS index_compras_guia_sync_id ON compras(guia_sync_id)")

            for (table in entityTablesByType.keys) {
                connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_${table}_sync_id ON $table(sync_id)")
            }

            connection.execSQL(
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
                """.trimIndent(),
            )
            connection.execSQL("CREATE INDEX IF NOT EXISTS index_sync_outbox_status ON sync_outbox(status)")
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS index_sync_outbox_entity_type_entity_sync_id ON sync_outbox(entity_type, entity_sync_id)",
            )
            connection.execSQL("CREATE INDEX IF NOT EXISTS index_sync_outbox_created_at ON sync_outbox(created_at)")
        }
    }
