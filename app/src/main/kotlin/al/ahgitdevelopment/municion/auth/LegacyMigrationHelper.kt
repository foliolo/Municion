package al.ahgitdevelopment.municion.auth

import android.content.Context
import android.content.SharedPreferences
import al.ahgitdevelopment.municion.data.local.room.dao.CompraDao
import al.ahgitdevelopment.municion.data.local.room.dao.GuiaDao
import al.ahgitdevelopment.municion.data.local.room.dao.LicenciaDao
import al.ahgitdevelopment.municion.data.local.room.dao.TiradaDao
import al.ahgitdevelopment.municion.databases.DataBaseSQLiteHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * LegacyMigrationHelper - Migración de usuarios legacy a nuevo sistema
 *
 * FASE 4: Legacy Migration
 * - Detecta usuarios con datos legacy (SharedPrefs + SQLite)
 * - Migra PIN de SharedPreferences a AuthManager encriptado
 * - Migra datos de SQLite a Room
 * - Intenta recuperar cuenta Firebase si existe
 * - Ofrece opción de vinculación manual si falla auto-migración
 *
 * Estrategia de migración:
 * 1. Detectar si hay datos legacy
 * 2. Migrar PIN a AuthManager (si no está ya migrado)
 * 3. Migrar datos de SQLite a Room (si no existen en Room)
 * 4. Intentar recuperar cuenta Firebase con email+PIN legacy
 * 5. Si falla, crear usuario anónimo y ofrecer vinculación manual
 *
 * @since v3.0.0 (TRACK B - Auth Modernization)
 */
@Singleton
class LegacyMigrationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authManager: AuthManager,
    private val firebaseAuthRepository: FirebaseAuthRepository,
    private val guiaDao: GuiaDao,
    private val compraDao: CompraDao,
    private val licenciaDao: LicenciaDao,
    private val tiradaDao: TiradaDao
) {
    companion object {
        private const val TAG = "LegacyMigrationHelper"
        private const val PREFS_LEGACY = "Preferences"
        private const val KEY_LEGACY_PASSWORD = "password"
        private const val KEY_MIGRATION_COMPLETED = "migration_v3_completed"
        private const val KEY_MIGRATION_PIN_DONE = "migration_pin_done"
        private const val KEY_MIGRATION_DATA_DONE = "migration_data_done"
        private const val KEY_LEGACY_YEAR = "year"
    }

    private val legacyPrefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_LEGACY, Context.MODE_PRIVATE)
    }

    private val migrationPrefs: SharedPreferences by lazy {
        context.getSharedPreferences("migration_prefs", Context.MODE_PRIVATE)
    }

    /**
     * Verifica si hay datos legacy que necesitan migración
     */
    fun hasLegacyData(): Boolean {
        val hasLegacyPin = legacyPrefs.contains(KEY_LEGACY_PASSWORD) &&
                legacyPrefs.getString(KEY_LEGACY_PASSWORD, "")?.isNotEmpty() == true
        val hasSqliteData = hasSqliteData()

        return (hasLegacyPin || hasSqliteData) && !isMigrationCompleted()
    }

    /**
     * Verifica si la migración ya fue completada
     */
    fun isMigrationCompleted(): Boolean {
        return migrationPrefs.getBoolean(KEY_MIGRATION_COMPLETED, false)
    }

    /**
     * Ejecuta la migración completa
     */
    suspend fun executeMigration(): MigrationStatus {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.i(TAG, "Starting legacy migration...")

                // Paso 1: Migrar PIN si existe
                val pinMigrationResult = migratePin()
                android.util.Log.i(TAG, "PIN migration: ${pinMigrationResult.name}")

                // Paso 2: Migrar datos de SQLite a Room
                val dataMigrationResult = migrateData()
                android.util.Log.i(TAG, "Data migration: $dataMigrationResult items migrated")

                // Paso 3: Intentar recuperar cuenta Firebase
                val firebaseMigrationResult = attemptFirebaseMigration()
                android.util.Log.i(TAG, "Firebase migration: ${firebaseMigrationResult.name}")

                // Marcar migración como completada
                markMigrationCompleted()

                MigrationStatus.Success(
                    pinMigrated = pinMigrationResult == PinMigrationResult.Migrated,
                    dataItemsMigrated = dataMigrationResult,
                    firebaseRecovered = firebaseMigrationResult == FirebaseMigrationResult.Recovered
                )
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Migration failed", e)
                MigrationStatus.Failed(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Migra el PIN de SharedPreferences a AuthManager (encriptado)
     */
    private fun migratePin(): PinMigrationResult {
        // Si ya está migrado, no hacer nada
        if (migrationPrefs.getBoolean(KEY_MIGRATION_PIN_DONE, false)) {
            return PinMigrationResult.AlreadyMigrated
        }

        // Si AuthManager ya tiene PIN configurado, no sobrescribir
        if (authManager.hasPinConfigured()) {
            migrationPrefs.edit().putBoolean(KEY_MIGRATION_PIN_DONE, true).apply()
            return PinMigrationResult.AlreadyConfigured
        }

        // Obtener PIN legacy
        val legacyPin = legacyPrefs.getString(KEY_LEGACY_PASSWORD, null)
        if (legacyPin.isNullOrEmpty()) {
            return PinMigrationResult.NoPinToMigrate
        }

        // Migrar a AuthManager
        val result = authManager.setupPin(legacyPin)
        return if (result.isSuccess) {
            // Eliminar PIN legacy (pero mantener otros datos por compatibilidad)
            legacyPrefs.edit().remove(KEY_LEGACY_PASSWORD).apply()
            migrationPrefs.edit().putBoolean(KEY_MIGRATION_PIN_DONE, true).apply()
            PinMigrationResult.Migrated
        } else {
            PinMigrationResult.Failed(result.exceptionOrNull()?.message ?: "Unknown error")
        }
    }

    /**
     * Migra datos de SQLite a Room
     * Solo migra si Room está vacío
     */
    private suspend fun migrateData(): Int {
        // Si ya está migrado, no hacer nada
        if (migrationPrefs.getBoolean(KEY_MIGRATION_DATA_DONE, false)) {
            return 0
        }

        var totalMigrated = 0

        try {
            val dbHelper = DataBaseSQLiteHelper(context)

            // Verificar si Room tiene datos
            val roomHasData = guiaDao.getCount() > 0 ||
                    compraDao.getCount() > 0 ||
                    licenciaDao.getCount() > 0 ||
                    tiradaDao.getCount() > 0

            if (roomHasData) {
                android.util.Log.i(TAG, "Room already has data, skipping SQLite migration")
                migrationPrefs.edit().putBoolean(KEY_MIGRATION_DATA_DONE, true).apply()
                dbHelper.close()
                return 0
            }

            // Migrar Licencias
            val licenciasLegacy = dbHelper.getListLicencias(null)
            val licenciasRoom = LegacyConverter.convertLicencias(licenciasLegacy)
            for (licencia in licenciasRoom) {
                try {
                    licenciaDao.insert(licencia)
                    totalMigrated++
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "Error migrating licencia: ${licencia.id}", e)
                }
            }

            // Migrar Guias
            val guiasLegacy = dbHelper.getListGuias(null)
            val guiasRoom = LegacyConverter.convertGuias(guiasLegacy)
            for (guia in guiasRoom) {
                try {
                    guiaDao.insert(guia)
                    totalMigrated++
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "Error migrating guia: ${guia.id}", e)
                }
            }

            // Migrar Compras
            val comprasLegacy = dbHelper.getListCompras(null)
            val comprasRoom = LegacyConverter.convertCompras(comprasLegacy)
            for (compra in comprasRoom) {
                try {
                    compraDao.insert(compra)
                    totalMigrated++
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "Error migrating compra: ${compra.id}", e)
                }
            }

            // Migrar Tiradas
            val tiradasLegacy = dbHelper.getListTiradas(null)
            val tiradasRoom = LegacyConverter.convertTiradas(tiradasLegacy)
            for (tirada in tiradasRoom) {
                try {
                    tiradaDao.insert(tirada)
                    totalMigrated++
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "Error migrating tirada: ${tirada.id}", e)
                }
            }

            dbHelper.close()
            migrationPrefs.edit().putBoolean(KEY_MIGRATION_DATA_DONE, true).apply()

            android.util.Log.i(TAG, "Data migration completed: $totalMigrated items")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Data migration failed", e)
        }

        return totalMigrated
    }

    /**
     * Intenta recuperar cuenta Firebase con credenciales legacy
     */
    private suspend fun attemptFirebaseMigration(): FirebaseMigrationResult {
        // Si ya hay usuario autenticado, no hacer nada
        if (firebaseAuthRepository.isAuthenticated()) {
            return FirebaseMigrationResult.AlreadyAuthenticated
        }

        // Obtener email del dispositivo (legacy method)
        val legacyEmail = getLegacyEmail()
        val legacyPin = legacyPrefs.getString(KEY_LEGACY_PASSWORD, null)

        if (legacyEmail.isNullOrEmpty() || legacyPin.isNullOrEmpty()) {
            // No hay credenciales legacy, crear usuario anónimo
            val anonResult = firebaseAuthRepository.signInAnonymously()
            return if (anonResult.isSuccess) {
                FirebaseMigrationResult.CreatedAnonymous
            } else {
                FirebaseMigrationResult.Failed(anonResult.exceptionOrNull()?.message ?: "Unknown")
            }
        }

        // Intentar recuperar cuenta con credenciales legacy
        val migrationResult = firebaseAuthRepository.migrateFromLegacy(legacyEmail, legacyPin)

        return when (val result = migrationResult.getOrNull()) {
            is MigrationResult.Success -> FirebaseMigrationResult.Recovered
            is MigrationResult.UserNotFound,
            is MigrationResult.InvalidCredentials -> {
                // Cuenta no existe o credenciales inválidas, crear anónimo
                val anonResult = firebaseAuthRepository.signInAnonymously()
                if (anonResult.isSuccess) {
                    FirebaseMigrationResult.CreatedAnonymous
                } else {
                    FirebaseMigrationResult.Failed("Could not create anonymous user")
                }
            }
            is MigrationResult.NetworkError -> FirebaseMigrationResult.NetworkError
            else -> {
                val anonResult = firebaseAuthRepository.signInAnonymously()
                if (anonResult.isSuccess) {
                    FirebaseMigrationResult.CreatedAnonymous
                } else {
                    FirebaseMigrationResult.Failed("Migration failed")
                }
            }
        }
    }

    /**
     * Obtiene el email del dispositivo usando el método legacy
     * NOTA: Este método era problemático por privacidad, solo se usa para migración
     */
    @Suppress("DEPRECATION")
    private fun getLegacyEmail(): String? {
        // En versiones modernas de Android, GET_ACCOUNTS requiere permiso
        // y ya no devuelve emails de otras apps
        // Solo intentamos si hay permiso
        try {
            val accountManager = android.accounts.AccountManager.get(context)
            val accounts = accountManager.getAccountsByType("com.google")
            if (accounts.isNotEmpty()) {
                return accounts[0].name
            }
        } catch (e: SecurityException) {
            android.util.Log.w(TAG, "Cannot get accounts - permission denied")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error getting legacy email", e)
        }
        return null
    }

    /**
     * Verifica si hay datos en SQLite
     */
    private fun hasSqliteData(): Boolean {
        return try {
            val dbHelper = DataBaseSQLiteHelper(context)
            val hasData = dbHelper.getListLicencias(null).isNotEmpty() ||
                    dbHelper.getListGuias(null).isNotEmpty() ||
                    dbHelper.getListCompras(null).isNotEmpty() ||
                    dbHelper.getListTiradas(null).isNotEmpty()
            dbHelper.close()
            hasData
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error checking SQLite data", e)
            false
        }
    }

    /**
     * Marca la migración como completada
     */
    private fun markMigrationCompleted() {
        migrationPrefs.edit()
            .putBoolean(KEY_MIGRATION_COMPLETED, true)
            .putLong("migration_timestamp", System.currentTimeMillis())
            .apply()
    }

    /**
     * Resetea el estado de migración (para testing)
     */
    fun resetMigrationState() {
        migrationPrefs.edit().clear().apply()
    }
}

/**
 * Estados de migración completa
 */
sealed class MigrationStatus {
    data class Success(
        val pinMigrated: Boolean,
        val dataItemsMigrated: Int,
        val firebaseRecovered: Boolean
    ) : MigrationStatus()

    data class Failed(val error: String) : MigrationStatus()
    object NotNeeded : MigrationStatus()
}

/**
 * Resultados de migración de PIN
 */
enum class PinMigrationResult {
    Migrated,
    AlreadyMigrated,
    AlreadyConfigured,
    NoPinToMigrate,
    Failed;

    companion object {
        fun Failed(message: String) = Failed
    }
}

/**
 * Resultados de migración de Firebase
 */
sealed class FirebaseMigrationResult {
    object Recovered : FirebaseMigrationResult()
    object AlreadyAuthenticated : FirebaseMigrationResult()
    object CreatedAnonymous : FirebaseMigrationResult()
    object NetworkError : FirebaseMigrationResult()
    data class Failed(val message: String) : FirebaseMigrationResult()

    val name: String
        get() = when (this) {
            is Recovered -> "Recovered"
            is AlreadyAuthenticated -> "AlreadyAuthenticated"
            is CreatedAnonymous -> "CreatedAnonymous"
            is NetworkError -> "NetworkError"
            is Failed -> "Failed: $message"
        }
}
