package al.ahgitdevelopment.municion.data.repository

import al.ahgitdevelopment.municion.data.local.room.dao.GuiaDao
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
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
 * Repository for Guia
 *
 * PHASE 2.4: Repository Pattern
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
@Singleton
class GuiaRepository @Inject constructor(
    private val guiaDao: GuiaDao,
    private val firebaseDb: DatabaseReference,
    private val crashlytics: FirebaseCrashlytics
) {

    companion object {
        private const val TAG = "GuiaRepository"
    }

    /**
     * Observes ALL guides
     */
    val guias: Flow<List<Guia>> = guiaDao.getAllGuiasFlow()

    /**
     * Observes guides by license type
     */
    fun getGuiasByTipoLicencia(tipoLicencia: Int): Flow<List<Guia>> {
        return guiaDao.getGuiasByTipoLicenciaFlow(tipoLicencia)
    }

    /**
     * Gets a guide by ID
     */
    suspend fun getGuiaById(id: Int): Guia? = withContext(Dispatchers.IO) {
        guiaDao.getGuiaById(id)
    }

    /**
     * Saves a guide
     */
    suspend fun saveGuia(guia: Guia, userId: String? = null): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val id = guiaDao.insert(guia)

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
     * Updates a guide
     */
    suspend fun updateGuia(guia: Guia, userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            guiaDao.update(guia)

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
     * Deletes a guide
     */
    suspend fun deleteGuia(guia: Guia, userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            guiaDao.delete(guia)

            userId?.let {
                syncToFirebase(it, deletedId = guia.id)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Updates the spent amount of a guide
     */
    suspend fun updateGastado(guiaId: Int, gastado: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            guiaDao.updateGastado(guiaId, gastado)
            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Increments the spent amount
     */
    suspend fun incrementGastado(guiaId: Int, cantidad: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            guiaDao.incrementGastado(guiaId, cantidad)
            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Decrements the spent amount (for rollback)
     */
    suspend fun decrementGastado(guiaId: Int, cantidad: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            guiaDao.decrementGastado(guiaId, cantidad)
            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Resets the spent amount of ALL guides (for new year)
     */
    suspend fun resetAllGastado(userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            guiaDao.resetAllGastado()

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
     * Gets guides with exhausted quota
     */
    suspend fun getGuiasConCupoAgotado(): List<Guia> = withContext(Dispatchers.IO) {
        guiaDao.getGuiasConCupoAgotado()
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
                .child("guias")
                .get()
                .await()

            val totalInFirebase = snapshot.childrenCount.toInt()
            android.util.Log.d(TAG, "Firebase snapshot: $totalInFirebase guias")

            val firebaseGuias = mutableListOf<Guia>()
            val parseErrors = mutableListOf<ParseError>()

            snapshot.children.forEach { child ->
                val itemKey = child.key ?: "unknown"
                val result = parseAndValidateGuia(child, userId, itemKey, parseErrors)
                result?.let { firebaseGuias.add(it) }
            }

            if (firebaseGuias.isNotEmpty()) {
                guiaDao.replaceAll(firebaseGuias)
                android.util.Log.i(TAG, "Synced ${firebaseGuias.size} guias from Firebase (${parseErrors.size} errors)")
            } else if (totalInFirebase > 0) {
                android.util.Log.w(TAG, "Firebase has $totalInFirebase guias but 0 parsed successfully")
            } else {
                android.util.Log.d(TAG, "No guias in Firebase")
            }

            val hasLocalData = guiaDao.getCount() > 0

            Result.success(SyncResultWithErrors(
                success = true,
                syncedCount = firebaseGuias.size,
                totalInFirebase = totalInFirebase,
                parseErrors = parseErrors,
                hasLocalData = hasLocalData
            ))
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Sync failed: ${e.message}", e)
            crashlytics.log("Failed to sync guias from Firebase: ${e.message}")
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseAndValidateGuia(
        snapshot: DataSnapshot,
        userId: String,
        itemKey: String,
        parseErrors: MutableList<ParseError>
    ): Guia? {
        val map = snapshot.value as? Map<String, Any?> ?: run {
            reportFieldError(userId, itemKey, "root", "Not a Map", snapshot.value?.toString(), parseErrors)
            return null
        }

        // Mandatory fields
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

        // Optional fields
        val id = (map["id"] as? Number)?.toInt() ?: 0
        val idCompra = (map["idCompra"] as? Number)?.toInt() ?: 0
        val tipoArma = (map["tipoArma"] as? Number)?.toInt() ?: 0
        val calibre2 = map["calibre2"] as? String
        val gastado = (map["gastado"] as? Number)?.toInt() ?: 0
        val imagePath = map["imagePath"] as? String

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
                imagePath = imagePath
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

        val error = ParseError(
            entity = "Guia",
            itemKey = itemKey,
            failedField = fieldName,
            errorType = errorType,
            fieldValue = redactedValue
        )
        parseErrors.add(error)

        crashlytics.apply {
            setCustomKey("entity", "Guia")
            setCustomKey("item_key", itemKey)
            setCustomKey("failed_field", fieldName)
            setCustomKey("error_type", errorType)
            setCustomKey("field_value", redactedValue)
            recordException(FirebaseParseException("[Guia] Field '$fieldName' failed: $errorType"))
        }

        android.util.Log.e(TAG, "Parse error Guia[$itemKey].$fieldName: $errorType (value: $redactedValue)")
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
            // 1. Fetch Remote (to avoid overwriting data from other devices)
            val snapshot = firebaseDb
                .child("users")
                .child(userId)
                .child("db")
                .child("guias")
                .get()
                .await()

            val remoteList = mutableListOf<Guia>()
            // We use a temporary list of errors that we don't report to avoid cluttering logs on every save
            val dummyErrors = mutableListOf<ParseError>()
            
            snapshot.children.forEach { child ->
                // We try to recover everything possible from remote
                parseAndValidateGuia(child, userId, child.key ?: "unknown", dummyErrors)?.let {
                    remoteList.add(it)
                }
            }

            // 2. Fetch Local
            val localList = guiaDao.getAllGuias()

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
                .child("guias")
                .setValue(finalList)
                .await()

            // 5. Update Local (To bring remote items we didn't have)
            if (finalList.isNotEmpty() || (localList.isNotEmpty() || remoteList.isNotEmpty())) {
                guiaDao.replaceAll(finalList)
            }

            android.util.Log.i(TAG, "Synced ${finalList.size} guias to Firebase (Merged Local+Remote)")

            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.log("Failed to sync guias to Firebase: ${e.message}")
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }
}