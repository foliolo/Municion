package al.ahgitdevelopment.municion.data.local.room

import al.ahgitdevelopment.municion.data.local.room.dao.AppPurchaseDao
import al.ahgitdevelopment.municion.data.local.room.dao.CompraDao
import al.ahgitdevelopment.municion.data.local.room.dao.GuiaDao
import al.ahgitdevelopment.municion.data.local.room.dao.LicenciaDao
import al.ahgitdevelopment.municion.data.local.room.dao.TiradaDao
import al.ahgitdevelopment.municion.data.local.room.entities.AppPurchase
import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
import android.content.Context
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
        AppPurchase::class
    ],
    version = 26,
    exportSchema = true
)
abstract class MunicionDatabase : RoomDatabase() {

    abstract fun guiaDao(): GuiaDao
    abstract fun compraDao(): CompraDao
    abstract fun licenciaDao(): LicenciaDao
    abstract fun tiradaDao(): TiradaDao
    abstract fun appPurchaseDao(): AppPurchaseDao

    companion object {
        private const val DATABASE_NAME = "municion.db"

        /**
         * MIGRATION: v25 → v26
         *
         * CAMBIO: Agregar tabla app_purchases para almacenar compras in-app (remove ads)
         */
        val MIGRATION_25_26 = object : Migration(25, 26) {
            override fun migrate(database: SupportSQLiteDatabase) {
                android.util.Log.i("MunicionDatabase", "Starting migration v25 → v26 (Add AppPurchase)")
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
                .addMigrations(MIGRATION_23_24, MIGRATION_24_25, MIGRATION_25_26)
                .fallbackToDestructiveMigration(false)  // Solo como último recurso
                .build()
        }
    }
}
