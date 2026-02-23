package al.ahgitdevelopment.municion.data.repository

import al.ahgitdevelopment.municion.data.local.room.dao.TiradaDao
import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
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
 * Repository for Tirada
 *
 * Uses per-entity Firebase writes instead of full-list sync.
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
@Singleton
class TiradaRepository @Inject constructor(
    private val tiradaDao: TiradaDao,
    private val syncHelper: FirebaseSyncHelper,
    private val crashlytics: FirebaseCrashlytics
) {

    companion object {
        private const val TAG = "TiradaRepository"
        private const val ENTITY_PATH = "tiradas"
    }

    val tiradas: Flow<List<Tirada>> = tiradaDao.getAllTiradasFlow()

    suspend fun getTiradaById(id: Int): Tirada? = withContext(Dispatchers.IO) {
        tiradaDao.getTiradaById(id)
    }

    suspend fun getTiradasConPuntuacion(): List<Tirada> = withContext(Dispatchers.IO) {
        tiradaDao.getTiradasConPuntuacion()
    }

    suspend fun getEstadisticas(): TiradaEstadisticas = withContext(Dispatchers.IO) {
        TiradaEstadisticas(
            total = tiradaDao.countTiradas(),
            promedio = tiradaDao.getPromedioPuntuacion() ?: 0f,
            mejor = tiradaDao.getMejorPuntuacion() ?: 0f
        )
    }

    suspend fun saveTirada(tirada: Tirada, userId: String? = null): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val stamped = tirada.copy(updatedAt = System.currentTimeMillis())
            val id = tiradaDao.insert(stamped)
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

    suspend fun updateTirada(tirada: Tirada, userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val stamped = tirada.copy(updatedAt = System.currentTimeMillis())
            tiradaDao.update(stamped)

            userId?.let {
                syncHelper.writeEntity(it, ENTITY_PATH, stamped.id, stamped.toFirebaseMap())
            }

            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    suspend fun deleteTirada(tirada: Tirada, userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            tiradaDao.delete(tirada)

            userId?.let {
                syncHelper.deleteEntity(it, ENTITY_PATH, tirada.id)
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

            val localTimestamps = tiradaDao.getAllTimestamps()
            val localIds = localTimestamps.keys

            val parseErrors = mutableListOf<ParseError>()

            val diffResult = syncHelper.syncFromFirebaseWithDiff(
                userId = userId,
                entityPath = ENTITY_PATH,
                localIds = localIds,
                localTimestamps = localTimestamps,
                parser = { key, value ->
                    parseTiradaFromMap(key, value, parseErrors)
                },
                getId = { it.id },
                getUpdatedAt = { it.updatedAt },
                upsert = { tiradaDao.insert(it) },
                deleteLocal = { tiradaDao.deleteById(it) }
            )

            val hasLocalData = tiradaDao.countTiradas() > 0

            Result.success(SyncResultWithErrors(
                success = diffResult.success,
                syncedCount = diffResult.upserted,
                totalInFirebase = diffResult.totalInFirebase,
                parseErrors = parseErrors,
                hasLocalData = hasLocalData
            ))
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed: ${e.message}", e)
            crashlytics.log("Failed to sync tiradas from Firebase: ${e.message}")
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Full sync TO Firebase (all tiradas as map). Used for auto-fix only.
     */
    suspend fun syncToFirebase(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val allTiradas = tiradaDao.getAllTiradas()
            val entityMaps = allTiradas.associate { it.id.toString() to it.toFirebaseMap() }
            syncHelper.fullSyncToFirebase(userId, ENTITY_PATH, entityMaps)
        } catch (e: Exception) {
            crashlytics.log("Failed to full-sync tiradas to Firebase: ${e.message}")
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    // --- Parsing ---

    @Suppress("UNCHECKED_CAST")
    private fun parseTiradaFromMap(
        itemKey: String,
        value: Any?,
        parseErrors: MutableList<ParseError>
    ): Tirada? {
        val map = value as? Map<String, Any?> ?: run {
            reportFieldError(itemKey, "root", "Not a Map", value?.toString(), parseErrors)
            return null
        }

        val descripcion = (map["descripcion"] as? String)?.takeIf { it.isNotBlank() } ?: run {
            reportFieldError(itemKey, "descripcion", "Blank or missing", map["descripcion"]?.toString(), parseErrors)
            return null
        }

        val fecha = (map["fecha"] as? String)?.takeIf { it.isNotBlank() } ?: run {
            reportFieldError(itemKey, "fecha", "Blank or missing", map["fecha"]?.toString(), parseErrors)
            return null
        }

        val id = (map["id"] as? Number)?.toInt() ?: 0
        val localizacion = map["rango"] as? String  // Firebase uses "rango" for compatibility
        val categoria = map["categoria"] as? String
        val modalidad = map["modalidad"] as? String
        val puntuacion = (map["puntuacion"] as? Number)?.toInt() ?: 0
        val updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()

        val maxPuntuacion = Tirada.getMaxPuntuacion(modalidad)
        if (puntuacion !in 0..maxPuntuacion) {
            reportFieldError(itemKey, "puntuacion", "Must be 0-$maxPuntuacion for $modalidad", puntuacion.toString(), parseErrors)
            return null
        }

        return try {
            Tirada(
                id = id,
                descripcion = descripcion,
                localizacion = localizacion,
                categoria = categoria,
                modalidad = modalidad,
                fecha = fecha,
                puntuacion = puntuacion,
                updatedAt = updatedAt
            )
        } catch (e: Exception) {
            reportFieldError(itemKey, "constructor", e.message ?: "Unknown", null, parseErrors)
            null
        }
    }

    private fun reportFieldError(
        itemKey: String,
        fieldName: String,
        errorType: String,
        fieldValue: String?,
        parseErrors: MutableList<ParseError>
    ) {
        val redactedValue = SensitiveFields.redactIfNeeded(fieldName, fieldValue)

        parseErrors.add(ParseError(
            entity = "Tirada",
            itemKey = itemKey,
            failedField = fieldName,
            errorType = errorType,
            fieldValue = redactedValue
        ))

        crashlytics.apply {
            setCustomKey("entity", "Tirada")
            setCustomKey("item_key", itemKey)
            setCustomKey("failed_field", fieldName)
            setCustomKey("error_type", errorType)
            setCustomKey("field_value", redactedValue)
            recordException(FirebaseParseException("[Tirada] Field '$fieldName' failed: $errorType"))
        }

        Log.e(TAG, "Parse error Tirada[$itemKey].$fieldName: $errorType (value: $redactedValue)")
    }

    data class TiradaEstadisticas(
        val total: Int,
        val promedio: Float,
        val mejor: Float
    )
}
