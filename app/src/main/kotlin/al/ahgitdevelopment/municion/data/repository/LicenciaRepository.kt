package al.ahgitdevelopment.municion.data.repository

import al.ahgitdevelopment.municion.data.local.room.dao.LicenciaDao
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
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
 * Repository for Licencia
 *
 * PHASE 2.4: Repository Pattern
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
@Singleton
class LicenciaRepository @Inject constructor(
    private val licenciaDao: LicenciaDao,
    private val firebaseDb: DatabaseReference,
    private val crashlytics: FirebaseCrashlytics
) {

    companion object {
        private const val TAG = "LicenciaRepository"
    }

    /**
     * Observes ALL licenses
     */
    val licencias: Flow<List<Licencia>> = licenciaDao.getAllLicenciasFlow()

    /**
     * Observes licenses by type
     */
    fun getLicenciasByTipo(tipo: Int): Flow<List<Licencia>> {
        return licenciaDao.getLicenciasByTipoFlow(tipo)
    }

    /**
     * Gets a license by ID
     */
    suspend fun getLicenciaById(id: Int): Licencia? = withContext(Dispatchers.IO) {
        licenciaDao.getLicenciaById(id)
    }

    /**
     * Gets license by number
     */
    suspend fun getLicenciaByNumero(numLicencia: String): Licencia? = withContext(Dispatchers.IO) {
        licenciaDao.getLicenciaByNumero(numLicencia)
    }

    /**
     * Checks if a license exists
     */
    suspend fun existsLicencia(numLicencia: String): Boolean = withContext(Dispatchers.IO) {
        licenciaDao.existsLicencia(numLicencia)
    }

    /**
     * Saves a license
     */
    suspend fun saveLicencia(licencia: Licencia, userId: String? = null): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val id = licenciaDao.insert(licencia)

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
     * Updates a license
     */
    suspend fun updateLicencia(licencia: Licencia, userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            licenciaDao.update(licencia)

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
     * Deletes a license
     */
    suspend fun deleteLicencia(licencia: Licencia, userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            licenciaDao.delete(licencia)

            userId?.let {
                syncToFirebase(it, deletedId = licencia.id)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Syncs FROM Firebase → Room with manual parsing and Crashlytics reporting
     */
    suspend fun syncFromFirebase(userId: String): Result<SyncResultWithErrors> = withContext(Dispatchers.IO) {
        try {
            // Configure userId in Crashlytics for tracking
            crashlytics.setUserId(userId)

            val snapshot = firebaseDb
                .child("users")
                .child(userId)
                .child("db")
                .child("licencias")
                .get()
                .await()

            val totalInFirebase = snapshot.childrenCount.toInt()
            android.util.Log.d(TAG, "Firebase snapshot: $totalInFirebase licencias")

            val firebaseLicencias = mutableListOf<Licencia>()
            val parseErrors = mutableListOf<ParseError>()

            snapshot.children.forEach { child ->
                val itemKey = child.key ?: "unknown"
                val result = parseAndValidateLicencia(child, userId, itemKey, parseErrors)
                result?.let { firebaseLicencias.add(it) }
            }

            if (firebaseLicencias.isNotEmpty()) {
                licenciaDao.replaceAll(firebaseLicencias)
                android.util.Log.i(TAG, "Synced ${firebaseLicencias.size} licencias from Firebase (${parseErrors.size} errors)")
            } else if (totalInFirebase > 0) {
                android.util.Log.w(TAG, "Firebase has $totalInFirebase licencias but 0 parsed successfully")
            } else {
                android.util.Log.d(TAG, "No licencias in Firebase")
            }

            val hasLocalData = licenciaDao.countLicencias() > 0

            Result.success(SyncResultWithErrors(
                success = true,
                syncedCount = firebaseLicencias.size,
                totalInFirebase = totalInFirebase,
                parseErrors = parseErrors,
                hasLocalData = hasLocalData
            ))
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Sync failed: ${e.message}", e)
            crashlytics.log("Failed to sync licencias from Firebase: ${e.message}")
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Parses and validates a License field by field, reporting specific errors
     */
    @Suppress("UNCHECKED_CAST")
    private fun parseAndValidateLicencia(
        snapshot: DataSnapshot,
        userId: String,
        itemKey: String,
        parseErrors: MutableList<ParseError>
    ): Licencia? {
        val map = snapshot.value as? Map<String, Any?> ?: run {
            reportFieldError(userId, itemKey, "root", "Not a Map", snapshot.value?.toString(), parseErrors)
            return null
        }

        // Mandatory fields with validation
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

        // Optional fields
        val id = (map["id"] as? Number)?.toInt() ?: 0
        val nombre = map["nombre"] as? String
        val tipoPermisoConduccion = (map["tipoPermisoConduccion"] as? Number)?.toInt() ?: -1
        val numAbonado = (map["numAbonado"] as? Number)?.toInt() ?: -1
        val numSeguro = map["numSeguro"] as? String
        val autonomia = (map["autonomia"] as? Number)?.toInt() ?: -1
        val escala = (map["escala"] as? Number)?.toInt() ?: -1
        val categoria = (map["categoria"] as? Number)?.toInt() ?: -1

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
                categoria = categoria
            )
        } catch (e: Exception) {
            // Capture any other exception from the constructor
            reportFieldError(userId, itemKey, "constructor", e.message ?: "Unknown", null, parseErrors)
            null
        }
    }

    /**
     * Reports a field error to Crashlytics and adds it to the error list
     */
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
            entity = "Licencia",
            itemKey = itemKey,
            failedField = fieldName,
            errorType = errorType,
            fieldValue = redactedValue
        )
        parseErrors.add(error)

        // Report to Crashlytics
        crashlytics.apply {
            setCustomKey("entity", "Licencia")
            setCustomKey("item_key", itemKey)
            setCustomKey("failed_field", fieldName)
            setCustomKey("error_type", errorType)
            setCustomKey("field_value", redactedValue)
            recordException(FirebaseParseException(
                "[Licencia] Field '$fieldName' failed: $errorType"
            ))
        }

        android.util.Log.e(TAG, "Parse error Licencia[$itemKey].$fieldName: $errorType (value: $redactedValue)")
    }

    /**
     * Syncs TO Firebase ← Room (Safe Merge)
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
                .child("licencias")
                .get()
                .await()

            val remoteList = mutableListOf<Licencia>()
            // We use a temporary list of errors that we don't report to avoid cluttering logs on every save
            val dummyErrors = mutableListOf<ParseError>()
            
            snapshot.children.forEach { child ->
                // We try to recover everything possible from remote
                parseAndValidateLicencia(child, userId, child.key ?: "unknown", dummyErrors)?.let {
                    remoteList.add(it)
                }
            }

            // 2. Fetch Local
            val localList = licenciaDao.getAllLicencias()

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
                .child("licencias")
                .setValue(finalList)
                .await()

            // 5. Update Local (To bring remote items we didn't have)
            if (finalList.isNotEmpty() || (localList.isNotEmpty() || remoteList.isNotEmpty())) {
                licenciaDao.replaceAll(finalList)
            }

            android.util.Log.i(TAG, "Synced ${finalList.size} licencias to Firebase (Merged Local+Remote)")

            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.log("Failed to sync licencias to Firebase: ${e.message}")
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }
}
