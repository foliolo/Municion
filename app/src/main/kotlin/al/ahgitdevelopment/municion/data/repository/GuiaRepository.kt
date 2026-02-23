package al.ahgitdevelopment.municion.data.repository

import al.ahgitdevelopment.municion.data.local.room.dao.GuiaDao
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
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
 * Repository for Guia
 *
 * Uses per-entity Firebase writes instead of full-list sync.
 * syncFromFirebase uses smart diff (upsert newer, delete removed).
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
@Singleton
class GuiaRepository @Inject constructor(
    private val guiaDao: GuiaDao,
    private val syncHelper: FirebaseSyncHelper,
    private val crashlytics: FirebaseCrashlytics
) {

    companion object {
        private const val TAG = "GuiaRepository"
        private const val ENTITY_PATH = "guias"
    }

    val guias: Flow<List<Guia>> = guiaDao.getAllGuiasFlow()

    fun getGuiasByTipoLicencia(tipoLicencia: Int): Flow<List<Guia>> {
        return guiaDao.getGuiasByTipoLicenciaFlow(tipoLicencia)
    }

    suspend fun getGuiaById(id: Int): Guia? = withContext(Dispatchers.IO) {
        guiaDao.getGuiaById(id)
    }

    suspend fun saveGuia(guia: Guia, userId: String? = null): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val stamped = guia.copy(updatedAt = System.currentTimeMillis())
            val id = guiaDao.insert(stamped)
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

    suspend fun updateGuia(guia: Guia, userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val stamped = guia.copy(updatedAt = System.currentTimeMillis())
            guiaDao.update(stamped)

            userId?.let {
                syncHelper.writeEntity(it, ENTITY_PATH, stamped.id, stamped.toFirebaseMap())
            }

            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    suspend fun deleteGuia(guia: Guia, userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            guiaDao.delete(guia)

            userId?.let {
                syncHelper.deleteEntity(it, ENTITY_PATH, guia.id)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    suspend fun updateGastado(guiaId: Int, gastado: Int, userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            guiaDao.updateGastado(guiaId, gastado)

            // Write the updated entity to Firebase
            userId?.let { uid ->
                guiaDao.getGuiaById(guiaId)?.let { updated ->
                    syncHelper.writeEntity(uid, ENTITY_PATH, updated.id, updated.toFirebaseMap())
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    suspend fun incrementGastado(guiaId: Int, cantidad: Int, userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            guiaDao.incrementGastado(guiaId, cantidad)

            userId?.let { uid ->
                guiaDao.getGuiaById(guiaId)?.let { updated ->
                    syncHelper.writeEntity(uid, ENTITY_PATH, updated.id, updated.toFirebaseMap())
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    suspend fun decrementGastado(guiaId: Int, cantidad: Int, userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            guiaDao.decrementGastado(guiaId, cantidad)

            userId?.let { uid ->
                guiaDao.getGuiaById(guiaId)?.let { updated ->
                    syncHelper.writeEntity(uid, ENTITY_PATH, updated.id, updated.toFirebaseMap())
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    suspend fun resetAllGastado(userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            guiaDao.resetAllGastado()

            // Full sync needed since we updated all guias
            userId?.let { syncToFirebase(it) }

            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    suspend fun getGuiasConCupoAgotado(): List<Guia> = withContext(Dispatchers.IO) {
        guiaDao.getGuiasConCupoAgotado()
    }

    /**
     * Syncs FROM Firebase -> Room using smart diff (upsert newer, delete removed).
     */
    suspend fun syncFromFirebase(userId: String): Result<SyncResultWithErrors> = withContext(Dispatchers.IO) {
        try {
            crashlytics.setUserId(userId)

            val localTimestamps = guiaDao.getAllTimestamps()
            val localIds = localTimestamps.keys

            val parseErrors = mutableListOf<ParseError>()

            val diffResult = syncHelper.syncFromFirebaseWithDiff(
                userId = userId,
                entityPath = ENTITY_PATH,
                localIds = localIds,
                localTimestamps = localTimestamps,
                parser = { key, value ->
                    parseGuiaFromMap(key, value, userId, parseErrors)
                },
                getId = { it.id },
                getUpdatedAt = { it.updatedAt },
                upsert = { guiaDao.insert(it) },
                deleteLocal = { guiaDao.deleteById(it) }
            )

            val hasLocalData = guiaDao.getCount() > 0

            Result.success(SyncResultWithErrors(
                success = diffResult.success,
                syncedCount = diffResult.upserted,
                totalInFirebase = diffResult.totalInFirebase,
                parseErrors = parseErrors,
                hasLocalData = hasLocalData
            ))
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed: ${e.message}", e)
            crashlytics.log("Failed to sync guias from Firebase: ${e.message}")
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Full sync TO Firebase (all guias as map). Used for auto-fix and manual sync only.
     */
    suspend fun syncToFirebase(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val allGuias = guiaDao.getAllGuias()
            val entityMaps = allGuias.associate { it.id.toString() to it.toFirebaseMap() }
            syncHelper.fullSyncToFirebase(userId, ENTITY_PATH, entityMaps)
        } catch (e: Exception) {
            crashlytics.log("Failed to full-sync guias to Firebase: ${e.message}")
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    // --- Parsing ---

    @Suppress("UNCHECKED_CAST")
    private fun parseGuiaFromMap(
        itemKey: String,
        value: Any?,
        userId: String,
        parseErrors: MutableList<ParseError>
    ): Guia? {
        val map = value as? Map<String, Any?> ?: run {
            reportFieldError(userId, itemKey, "root", "Not a Map", value?.toString(), parseErrors)
            return null
        }

        val tipoLicencia = (map["tipoLicencia"] as? Number)?.toInt() ?: run {
            reportFieldError(userId, itemKey, "tipoLicencia", "Missing or invalid", map["tipoLicencia"]?.toString(), parseErrors)
            return null
        }

        val marca = (map["marca"] as? String)?.takeIf { it.isNotBlank() } ?: run {
            reportFieldError(userId, itemKey, "marca", "Blank or missing", map["marca"]?.toString(), parseErrors)
            return null
        }

        val modelo = (map["modelo"] as? String)?.takeIf { it.isNotBlank() } ?: run {
            reportFieldError(userId, itemKey, "modelo", "Blank or missing", map["modelo"]?.toString(), parseErrors)
            return null
        }

        val apodo = (map["apodo"] as? String)?.takeIf { it.isNotBlank() } ?: run {
            reportFieldError(userId, itemKey, "apodo", "Blank or missing", map["apodo"]?.toString(), parseErrors)
            return null
        }

        val calibre1 = (map["calibre1"] as? String)?.takeIf { it.isNotBlank() } ?: run {
            reportFieldError(userId, itemKey, "calibre1", "Blank or missing", map["calibre1"]?.toString(), parseErrors)
            return null
        }

        val numGuia = (map["numGuia"] as? String)?.takeIf { it.isNotBlank() } ?: run {
            reportFieldError(userId, itemKey, "numGuia", "Blank or missing", null, parseErrors)
            return null
        }

        val numArma = (map["numArma"] as? String)?.takeIf { it.isNotBlank() } ?: run {
            reportFieldError(userId, itemKey, "numArma", "Blank or missing", null, parseErrors)
            return null
        }

        val cupo = (map["cupo"] as? Number)?.toInt() ?: run {
            reportFieldError(userId, itemKey, "cupo", "Missing or invalid", map["cupo"]?.toString(), parseErrors)
            return null
        }
        if (cupo <= 0) {
            reportFieldError(userId, itemKey, "cupo", "Must be > 0", cupo.toString(), parseErrors)
            return null
        }

        val id = (map["id"] as? Number)?.toInt() ?: 0
        val idCompra = (map["idCompra"] as? Number)?.toInt() ?: 0
        val tipoArma = (map["tipoArma"] as? Number)?.toInt() ?: 0
        val calibre2 = map["calibre2"] as? String
        val gastado = (map["gastado"] as? Number)?.toInt() ?: 0
        val imagePath = map["imagePath"] as? String
        val fotoUrl = map["fotoUrl"] as? String
        val storagePath = map["storagePath"] as? String
        val updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()

        return try {
            Guia(
                id = id,
                idCompra = idCompra,
                tipoLicencia = tipoLicencia,
                marca = marca,
                modelo = modelo,
                apodo = apodo,
                tipoArma = tipoArma,
                calibre1 = calibre1,
                calibre2 = calibre2,
                numGuia = numGuia,
                numArma = numArma,
                cupo = cupo,
                gastado = gastado,
                imagePath = imagePath,
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
            entity = "Guia",
            itemKey = itemKey,
            failedField = fieldName,
            errorType = errorType,
            fieldValue = redactedValue
        ))

        crashlytics.apply {
            setCustomKey("entity", "Guia")
            setCustomKey("item_key", itemKey)
            setCustomKey("failed_field", fieldName)
            setCustomKey("error_type", errorType)
            setCustomKey("field_value", redactedValue)
            recordException(FirebaseParseException("[Guia] Field '$fieldName' failed: $errorType"))
        }

        Log.e(TAG, "Parse error Guia[$itemKey].$fieldName: $errorType (value: $redactedValue)")
    }
}
