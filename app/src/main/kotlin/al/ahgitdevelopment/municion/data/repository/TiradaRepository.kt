package al.ahgitdevelopment.municion.data.repository

import al.ahgitdevelopment.municion.data.local.room.dao.TiradaDao
import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
import al.ahgitdevelopment.municion.domain.usecase.FirebaseParseException
import al.ahgitdevelopment.municion.domain.usecase.ParseError
import al.ahgitdevelopment.municion.domain.usecase.SensitiveFields
import al.ahgitdevelopment.municion.domain.usecase.SyncResultWithErrors
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Tirada
 *
 * PHASE 2.4: Repository Pattern
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
@Singleton
class TiradaRepository @Inject constructor(
    private val tiradaDao: TiradaDao,
    private val firebaseDb: DatabaseReference,
    private val crashlytics: FirebaseCrashlytics
) {

    companion object {
        private const val TAG = "TiradaRepository"
    }

    /**
     * Observes ALL shoots
     */
    val tiradas: Flow<List<Tirada>> = tiradaDao.getAllTiradasFlow()

    /**
     * Gets a shoot by ID
     */
    suspend fun getTiradaById(id: Int): Tirada? = withContext(Dispatchers.IO) {
        tiradaDao.getTiradaById(id)
    }

    /**
     * Gets shoots with score
     */
    suspend fun getTiradasConPuntuacion(): List<Tirada> = withContext(Dispatchers.IO) {
        tiradaDao.getTiradasConPuntuacion()
    }

    /**
     * Calculates statistics
     */
    suspend fun getEstadisticas(): TiradaEstadisticas = withContext(Dispatchers.IO) {
        TiradaEstadisticas(
            total = tiradaDao.countTiradas(),
            promedio = tiradaDao.getPromedioPuntuacion() ?: 0f,
            mejor = tiradaDao.getMejorPuntuacion() ?: 0f
        )
    }

    /**
     * Saves a shoot
     */
    suspend fun saveTirada(tirada: Tirada, userId: String? = null): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val id = tiradaDao.insert(tirada)

            userId?.let {
                syncToFirebase(it)
            }

            Result.success(id)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Updates a shoot
     */
    suspend fun updateTirada(tirada: Tirada, userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            tiradaDao.update(tirada)

            userId?.let {
                syncToFirebase(it)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Deletes a shoot
     */
    suspend fun deleteTirada(tirada: Tirada, userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            tiradaDao.delete(tirada)

            userId?.let {
                syncToFirebase(it, deletedId = tirada.id)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Syncs FROM Firebase -> Room with manual parsing and Crashlytics reporting
     */
    suspend fun syncFromFirebase(userId: String): Result<SyncResultWithErrors> = withContext(Dispatchers.IO) {
        try {
            crashlytics.setUserId(userId)

            val snapshot = firebaseDb
                .child("users")
                .child(userId)
                .child("db")
                .child("tiradas")
                .get()
                .await()

            val totalInFirebase = snapshot.childrenCount.toInt()
            android.util.Log.d(TAG, "Firebase snapshot: $totalInFirebase tiradas")

            val firebaseTiradas = mutableListOf<Tirada>()
            val parseErrors = mutableListOf<ParseError>()

            snapshot.children.forEach { child ->
                val itemKey = child.key ?: "unknown"
                val result = parseAndValidateTirada(child, itemKey, parseErrors)
                result?.let { firebaseTiradas.add(it) }
            }

            if (firebaseTiradas.isNotEmpty()) {
                tiradaDao.replaceAll(firebaseTiradas)
                android.util.Log.i(TAG, "Synced ${firebaseTiradas.size} tiradas from Firebase (${parseErrors.size} errors)")
            } else if (totalInFirebase > 0) {
                android.util.Log.w(TAG, "Firebase has $totalInFirebase tiradas but 0 parsed successfully")
            } else {
                android.util.Log.d(TAG, "No tiradas in Firebase")
            }

            val hasLocalData = tiradaDao.countTiradas() > 0

            Result.success(SyncResultWithErrors(
                success = true,
                syncedCount = firebaseTiradas.size,
                totalInFirebase = totalInFirebase,
                parseErrors = parseErrors,
                hasLocalData = hasLocalData
            ))
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Sync failed: ${e.message}", e)
            crashlytics.log("Failed to sync tiradas from Firebase: ${e.message}")
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseAndValidateTirada(
        snapshot: DataSnapshot,
        itemKey: String,
        parseErrors: MutableList<ParseError>
    ): Tirada? {
        val map = snapshot.value as? Map<String, Any?> ?: run {
            reportFieldError(itemKey, "root", "Not a Map", snapshot.value?.toString(), parseErrors)
            return null
        }

        // Mandatory fields
        val descripcion = (map["descripcion"] as? String)?.takeIf { it.isNotBlank() } ?: run {
            reportFieldError(itemKey, "descripcion", "Blank or missing", map["descripcion"]?.toString(), parseErrors)
            return null
        }

        val fecha = (map["fecha"] as? String)?.takeIf { it.isNotBlank() } ?: run {
            reportFieldError(itemKey, "fecha", "Blank or missing", map["fecha"]?.toString(), parseErrors)
            return null
        }

        // Optional fields
        val id = (map["id"] as? Number)?.toInt() ?: 0
        val rango = map["rango"] as? String
        val puntuacion = (map["puntuacion"] as? Number)?.toInt() ?: 0

        // Validate score range
        if (puntuacion < 0 || puntuacion > 600) {
            reportFieldError(itemKey, "puntuacion", "Must be 0-600", puntuacion.toString(), parseErrors)
            return null
        }

        return try {
            Tirada(
                id = id,
                descripcion = descripcion,
                rango = rango,
                fecha = fecha,
                puntuacion = puntuacion
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

        val error = ParseError(
            entity = "Tirada",
            itemKey = itemKey,
            failedField = fieldName,
            errorType = errorType,
            fieldValue = redactedValue
        )
        parseErrors.add(error)

        crashlytics.apply {
            setCustomKey("entity", "Tirada")
            setCustomKey("item_key", itemKey)
            setCustomKey("failed_field", fieldName)
            setCustomKey("error_type", errorType)
            setCustomKey("field_value", redactedValue)
            recordException(FirebaseParseException("[Tirada] Field '$fieldName' failed: $errorType"))
        }

        android.util.Log.e(TAG, "Parse error Tirada[$itemKey].$fieldName: $errorType (value: $redactedValue)")
    }

    /**
     * Syncs TO Firebase <- Room (Safe Merge)
     *
     * Implements Fetch-Merge-Push strategy to avoid data loss:
     * 1. Downloads current state from Firebase.
     * 2. Merges with local state (Local has priority if ID matches).
     * 3. Applies deletion if deletedId is specified.
     * 4. Uploads the merged result.
     * 5. Updates Room with the merged result.
     *
     * @param userId Authenticated user ID
     * @param deletedId Optional ID of an item that has just been locally deleted and must be removed from the merge
     */
    suspend fun syncToFirebase(userId: String, deletedId: Int? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Fetch Remote
            val snapshot = firebaseDb
                .child("users")
                .child(userId)
                .child("db")
                .child("tiradas")
                .get()
                .await()

            val remoteList = mutableListOf<Tirada>()
            // We use a temporary list of errors that we don't report to avoid cluttering logs on every save
            val dummyErrors = mutableListOf<ParseError>()
            
            snapshot.children.forEach { child ->
                // We try to recover everything possible from remote
                parseAndValidateTirada(child, child.key ?: "unknown", dummyErrors)?.let {
                    remoteList.add(it)
                }
            }

            // 2. Fetch Local
            val localList = tiradaDao.getAllTiradas()

            // 3. Merge Logic
            // We start with Remote as base
            val mergedMap = remoteList.associateBy { it.id }.toMutableMap()
            
            // Apply Local (Overwrites Remote if ID matches, adds if new)
            localList.forEach { mergedMap[it.id] = it }

            // Apply explicit deletion (because localList no longer has the item, but remote might)
            if (deletedId != null) {
                mergedMap.remove(deletedId)
            }

            val finalList = mergedMap.values.toList()

            // 4. Push to Firebase (Full merged list)
            firebaseDb
                .child("users")
                .child(userId)
                .child("db")
                .child("tiradas")
                .setValue(finalList)
                .await()

            // 5. Update Local (To bring remote items we didn't have)
            if (finalList.isNotEmpty() || (localList.isNotEmpty() || remoteList.isNotEmpty())) {
                tiradaDao.replaceAll(finalList)
            }

            android.util.Log.i(TAG, "Synced ${finalList.size} tiradas to Firebase (Merged Local+Remote)")

            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.log("Failed to sync tiradas to Firebase: ${e.message}")
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Data class for shoot statistics
     */
    data class TiradaEstadisticas(
        val total: Int,
        val promedio: Float,
        val mejor: Float
    )
}