package al.ahgitdevelopment.municion.data.repository

import al.ahgitdevelopment.municion.data.local.room.dao.LicenciaDao
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.data.sync.FirebaseSyncHelper
import al.ahgitdevelopment.municion.data.sync.toFirebaseMap
import al.ahgitdevelopment.municion.domain.usecase.FirebaseParseException
import al.ahgitdevelopment.municion.domain.usecase.ParseError
import al.ahgitdevelopment.municion.domain.usecase.SensitiveFields
import al.ahgitdevelopment.municion.domain.usecase.SyncResultWithErrors
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Licencia
 *
 * Uses per-entity Firebase writes instead of full-list sync.
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
@Singleton
class LicenciaRepository @Inject constructor(
    private val licenciaDao: LicenciaDao,
    private val syncHelper: FirebaseSyncHelper,
    private val crashlytics: FirebaseCrashlytics
) {

    companion object {
        private const val TAG = "LicenciaRepository"
        private const val ENTITY_PATH = "licencias"
    }

    val licencias: Flow<List<Licencia>> = licenciaDao.getAllLicenciasFlow()

    fun getLicenciasByTipo(tipo: Int): Flow<List<Licencia>> {
        return licenciaDao.getLicenciasByTipoFlow(tipo)
    }

    suspend fun getLicenciaById(id: Int): Licencia? = withContext(Dispatchers.IO) {
        licenciaDao.getLicenciaById(id)
    }

    suspend fun getLicenciaByNumero(numLicencia: String): Licencia? = withContext(Dispatchers.IO) {
        licenciaDao.getLicenciaByNumero(numLicencia)
    }

    suspend fun existsLicencia(numLicencia: String): Boolean = withContext(Dispatchers.IO) {
        licenciaDao.existsLicencia(numLicencia)
    }

    suspend fun saveLicencia(licencia: Licencia, userId: String? = null): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val stamped = licencia.copy(updatedAt = System.currentTimeMillis())
            val id = licenciaDao.insert(stamped)
            val saved = stamped.copy(id = id.toInt())

            userId?.let {
                syncHelper.writeEntity(it, ENTITY_PATH, saved.id, saved.toFirebaseMap())
            }

            Result.success(id)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    suspend fun updateLicencia(licencia: Licencia, userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val stamped = licencia.copy(updatedAt = System.currentTimeMillis())
            licenciaDao.update(stamped)

            userId?.let {
                syncHelper.writeEntity(it, ENTITY_PATH, stamped.id, stamped.toFirebaseMap())
            }

            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    suspend fun deleteLicencia(licencia: Licencia, userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            licenciaDao.delete(licencia)

            userId?.let {
                syncHelper.deleteEntity(it, ENTITY_PATH, licencia.id)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Syncs FROM Firebase -> Room using smart diff
     */
    suspend fun syncFromFirebase(userId: String): Result<SyncResultWithErrors> = withContext(Dispatchers.IO) {
        try {
            crashlytics.setUserId(userId)

            val localTimestamps = licenciaDao.getAllTimestamps()
            val localIds = localTimestamps.keys

            val parseErrors = mutableListOf<ParseError>()

            val diffResult = syncHelper.syncFromFirebaseWithDiff(
                userId = userId,
                entityPath = ENTITY_PATH,
                localIds = localIds,
                localTimestamps = localTimestamps,
                parser = { key, value ->
                    parseLicenciaFromMap(key, value, userId, parseErrors)
                },
                getId = { it.id },
                getUpdatedAt = { it.updatedAt },
                upsert = { licenciaDao.insert(it) },
                deleteLocal = { licenciaDao.deleteById(it) }
            )

            val hasLocalData = licenciaDao.countLicencias() > 0

            Result.success(SyncResultWithErrors(
                success = diffResult.success,
                syncedCount = diffResult.upserted,
                totalInFirebase = diffResult.totalInFirebase,
                parseErrors = parseErrors,
                hasLocalData = hasLocalData
            ))
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed: ${e.message}", e)
            crashlytics.log("Failed to sync licencias from Firebase: ${e.message}")
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Full sync TO Firebase (all licencias as map). Used for auto-fix only.
     */
    suspend fun syncToFirebase(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val allLicencias = licenciaDao.getAllLicencias()
            val entityMaps = allLicencias.associate { it.id.toString() to it.toFirebaseMap() }
            syncHelper.fullSyncToFirebase(userId, ENTITY_PATH, entityMaps)
        } catch (e: Exception) {
            crashlytics.log("Failed to full-sync licencias to Firebase: ${e.message}")
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    // --- Parsing ---

    @Suppress("UNCHECKED_CAST")
    private fun parseLicenciaFromMap(
        itemKey: String,
        value: Any?,
        userId: String,
        parseErrors: MutableList<ParseError>
    ): Licencia? {
        val map = value as? Map<String, Any?> ?: run {
            reportFieldError(userId, itemKey, "root", "Not a Map", value?.toString(), parseErrors)
            return null
        }

        val tipo = (map["tipo"] as? Number)?.toInt() ?: run {
            reportFieldError(userId, itemKey, "tipo", "Missing or invalid", map["tipo"]?.toString(), parseErrors)
            return null
        }
        if (tipo < 0) {
            reportFieldError(userId, itemKey, "tipo", "Must be >= 0", tipo.toString(), parseErrors)
            return null
        }

        val edad = (map["edad"] as? Number)?.toInt() ?: run {
            reportFieldError(userId, itemKey, "edad", "Missing or invalid", map["edad"]?.toString(), parseErrors)
            return null
        }
        if (edad <= 0) {
            reportFieldError(userId, itemKey, "edad", "Must be > 0", edad.toString(), parseErrors)
            return null
        }

        val numLicencia = (map["numLicencia"] as? String)?.takeIf { it.isNotBlank() } ?: run {
            reportFieldError(userId, itemKey, "numLicencia", "Blank or missing", null, parseErrors)
            return null
        }

        val fechaExpedicion = (map["fechaExpedicion"] as? String)?.takeIf { it.isNotBlank() } ?: run {
            reportFieldError(userId, itemKey, "fechaExpedicion", "Blank or missing", map["fechaExpedicion"]?.toString(), parseErrors)
            return null
        }

        val fechaCaducidad = (map["fechaCaducidad"] as? String)?.takeIf { it.isNotBlank() } ?: run {
            reportFieldError(userId, itemKey, "fechaCaducidad", "Blank or missing", map["fechaCaducidad"]?.toString(), parseErrors)
            return null
        }

        val id = (map["id"] as? Number)?.toInt() ?: 0
        val nombre = map["nombre"] as? String
        val tipoPermisoConduccion = (map["tipoPermisoConduccion"] as? Number)?.toInt() ?: -1
        val numAbonado = (map["numAbonado"] as? Number)?.toInt() ?: -1
        val numSeguro = map["numSeguro"] as? String
        val autonomia = (map["autonomia"] as? Number)?.toInt() ?: -1
        val escala = (map["escala"] as? Number)?.toInt() ?: -1
        val categoria = (map["categoria"] as? Number)?.toInt() ?: -1
        val fotoUrl = map["fotoUrl"] as? String
        val storagePath = map["storagePath"] as? String
        val updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()

        return try {
            Licencia(
                id = id,
                tipo = tipo,
                nombre = nombre,
                tipoPermisoConduccion = tipoPermisoConduccion,
                edad = edad,
                fechaExpedicion = fechaExpedicion,
                fechaCaducidad = fechaCaducidad,
                numLicencia = numLicencia,
                numAbonado = numAbonado,
                numSeguro = numSeguro,
                autonomia = autonomia,
                escala = escala,
                categoria = categoria,
                fotoUrl = fotoUrl,
                storagePath = storagePath,
                updatedAt = updatedAt
            )
        } catch (e: Exception) {
            reportFieldError(userId, itemKey, "constructor", e.message ?: "Unknown", null, parseErrors)
            null
        }
    }

    private fun reportFieldError(
        userId: String,
        itemKey: String,
        fieldName: String,
        errorType: String,
        fieldValue: String?,
        parseErrors: MutableList<ParseError>
    ) {
        val redactedValue = SensitiveFields.redactIfNeeded(fieldName, fieldValue)

        parseErrors.add(ParseError(
            entity = "Licencia",
            itemKey = itemKey,
            failedField = fieldName,
            errorType = errorType,
            fieldValue = redactedValue
        ))

        crashlytics.apply {
            setCustomKey("entity", "Licencia")
            setCustomKey("item_key", itemKey)
            setCustomKey("failed_field", fieldName)
            setCustomKey("error_type", errorType)
            setCustomKey("field_value", redactedValue)
            recordException(FirebaseParseException("[Licencia] Field '$fieldName' failed: $errorType"))
        }

        Log.e(TAG, "Parse error Licencia[$itemKey].$fieldName: $errorType (value: $redactedValue)")
    }
}
