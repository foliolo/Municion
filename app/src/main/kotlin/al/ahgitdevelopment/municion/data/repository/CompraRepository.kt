package al.ahgitdevelopment.municion.data.repository

import al.ahgitdevelopment.municion.data.local.room.dao.CompraDao
import al.ahgitdevelopment.municion.data.local.room.entities.Compra
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
 * Repository for Compra
 *
 * PHASE 2.4: Repository Pattern
 * - Single source of truth: Room is the main source
 * - Bidirectional sync with Firebase
 * - Offline-first: data always available from Room
 * - Flow to observe changes reactively
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
@Singleton
class CompraRepository @Inject constructor(
    private val compraDao: CompraDao,
    private val firebaseDb: DatabaseReference,
    private val crashlytics: FirebaseCrashlytics
) {

    companion object {
        private const val TAG = "CompraRepository"
    }

    /**
     * Observes ALL purchases (Room is source of truth)
     */
    val compras: Flow<List<Compra>> = compraDao.getAllComprasFlow()

    /**
     * Observes purchases for a specific guide
     */
    fun getComprasByGuia(guiaId: Int): Flow<List<Compra>> {
        return compraDao.getComprasByGuiaFlow(guiaId)
    }

    /**
     * Gets a purchase by ID
     */
    suspend fun getCompraById(id: Int): Compra? = withContext(Dispatchers.IO) {
        compraDao.getCompraById(id)
    }

    /**
     * Saves a purchase (local + sync to Firebase)
     */
    suspend fun saveCompra(compra: Compra, userId: String? = null): Result<Long> = withContext(Dispatchers.IO) {
        try {
            // 1. Save to Room (source of truth)
            val id = compraDao.insert(compra)

            // 2. Sync to Firebase if userId is present
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
     * Updates a purchase
     */
    suspend fun updateCompra(compra: Compra, userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            compraDao.update(compra)

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
     * Deletes a purchase
     */
    suspend fun deleteCompra(compra: Compra, userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            compraDao.delete(compra)

            userId?.let {
                syncToFirebase(it, deletedId = compra.id)
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
                .child("compras")
                .get()
                .await()

            val totalInFirebase = snapshot.childrenCount.toInt()
            android.util.Log.d(TAG, "Firebase snapshot: $totalInFirebase compras")

            val firebaseCompras = mutableListOf<Compra>()
            val parseErrors = mutableListOf<ParseError>()

            snapshot.children.forEach { child ->
                val itemKey = child.key ?: "unknown"
                val result = parseAndValidateCompra(child, itemKey, parseErrors)
                result?.let { firebaseCompras.add(it) }
            }

            if (firebaseCompras.isNotEmpty()) {
                compraDao.replaceAll(firebaseCompras)
                android.util.Log.i(TAG, "Synced ${firebaseCompras.size} compras from Firebase (${parseErrors.size} errors)")
            } else if (totalInFirebase > 0) {
                android.util.Log.w(TAG, "Firebase has $totalInFirebase compras but 0 parsed successfully")
            } else {
                android.util.Log.d(TAG, "No compras in Firebase")
            }

            val hasLocalData = compraDao.getCount() > 0

            Result.success(SyncResultWithErrors(
                success = true,
                syncedCount = firebaseCompras.size,
                totalInFirebase = totalInFirebase,
                parseErrors = parseErrors,
                hasLocalData = hasLocalData
            ))
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Sync failed: ${e.message}", e)
            crashlytics.log("Failed to sync compras from Firebase: ${e.message}")
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseAndValidateCompra(
        snapshot: DataSnapshot,
        itemKey: String,
        parseErrors: MutableList<ParseError>
    ): Compra? {
        val map = snapshot.value as? Map<String, Any?> ?: run {
            reportFieldError(itemKey, "root", "Not a Map", snapshot.value?.toString(), parseErrors)
            return null
        }

        // Mandatory fields
        val idPosGuia = (map["idPosGuia"] as? Number)?.toInt() ?: run {
            reportFieldError(itemKey, "idPosGuia", "Missing or invalid", map["idPosGuia"]?.toString(), parseErrors)
            return null
        }

        val calibre1 = (map["calibre1"] as? String)?.takeIf { it.isNotBlank() } ?: run {
            reportFieldError(itemKey, "calibre1", "Blank or missing", map["calibre1"]?.toString(), parseErrors)
            return null
        }

        val unidades = (map["unidades"] as? Number)?.toInt() ?: run {
            reportFieldError(itemKey, "unidades", "Missing or invalid", map["unidades"]?.toString(), parseErrors)
            return null
        }
        if (unidades <= 0) {
            reportFieldError(itemKey, "unidades", "Must be > 0", unidades.toString(), parseErrors)
            return null
        }

        val precio = (map["precio"] as? Number)?.toDouble() ?: run {
            reportFieldError(itemKey, "precio", "Missing or invalid", map["precio"]?.toString(), parseErrors)
            return null
        }
        if (precio < 0) {
            reportFieldError(itemKey, "precio", "Must be >= 0", precio.toString(), parseErrors)
            return null
        }

        val fecha = (map["fecha"] as? String)?.takeIf { it.isNotBlank() } ?: run {
            reportFieldError(itemKey, "fecha", "Blank or missing", map["fecha"]?.toString(), parseErrors)
            return null
        }

        val tipo = (map["tipo"] as? String)?.takeIf { it.isNotBlank() } ?: run {
            reportFieldError(itemKey, "tipo", "Blank or missing", map["tipo"]?.toString(), parseErrors)
            return null
        }

        val peso = (map["peso"] as? Number)?.toInt() ?: run {
            reportFieldError(itemKey, "peso", "Missing or invalid", map["peso"]?.toString(), parseErrors)
            return null
        }
        if (peso <= 0) {
            reportFieldError(itemKey, "peso", "Must be > 0", peso.toString(), parseErrors)
            return null
        }

        val marca = (map["marca"] as? String)?.takeIf { it.isNotBlank() } ?: run {
            reportFieldError(itemKey, "marca", "Blank or missing", map["marca"]?.toString(), parseErrors)
            return null
        }

        // Optional fields
        val id = (map["id"] as? Number)?.toInt() ?: 0
        val calibre2 = map["calibre2"] as? String
        val tienda = map["tienda"] as? String
        val valoracion = (map["valoracion"] as? Number)?.toFloat() ?: 0f
        val imagePath = map["imagePath"] as? String

        return try {
            Compra(
                id = id,
                idPosGuia = idPosGuia,
                calibre1 = calibre1,
                calibre2 = calibre2,
                unidades = unidades,
                precio = precio,
                fecha = fecha,
                tipo = tipo,
                peso = peso,
                marca = marca,
                tienda = tienda,
                valoracion = valoracion.coerceIn(0f, 5f),
                imagePath = imagePath
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
            entity = "Compra",
            itemKey = itemKey,
            failedField = fieldName,
            errorType = errorType,
            fieldValue = redactedValue
        )
        parseErrors.add(error)

        crashlytics.apply {
            setCustomKey("entity", "Compra")
            setCustomKey("item_key", itemKey)
            setCustomKey("failed_field", fieldName)
            setCustomKey("error_type", errorType)
            setCustomKey("field_value", redactedValue)
            recordException(FirebaseParseException("[Compra] Field '$fieldName' failed: $errorType"))
        }

        android.util.Log.e(TAG, "Parse error Compra[$itemKey].$fieldName: $errorType (value: $redactedValue)")
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
                .child("compras")
                .get()
                .await()

            val remoteList = mutableListOf<Compra>()
            // We use a temporary list of errors that we don't report to avoid cluttering logs on every save
            val dummyErrors = mutableListOf<ParseError>()
            
            snapshot.children.forEach { child ->
                // We try to recover everything possible from remote
                parseAndValidateCompra(child, child.key ?: "unknown", dummyErrors)?.let {
                    remoteList.add(it)
                }
            }

            // 2. Fetch Local
            val localList = compraDao.getAllCompras()

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
                .child("compras")
                .setValue(finalList)
                .await()

            // 5. Update Local (To bring remote items we didn't have)
            if (finalList.isNotEmpty() || (localList.isNotEmpty() || remoteList.isNotEmpty())) {
                compraDao.replaceAll(finalList)
            }

            android.util.Log.i(TAG, "Synced ${finalList.size} compras to Firebase (Merged Local+Remote)")

            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.log("Failed to sync compras to Firebase: ${e.message}")
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Detects purchases with corrupt data (weight = 0)
     */
    suspend fun detectCorruptedCompras(): List<Compra> = withContext(Dispatchers.IO) {
        compraDao.getComprasWithCorruptedPeso()
    }

    /**
     * Counts purchases for a guide
     */
    suspend fun countComprasByGuia(guiaId: Int): Int = withContext(Dispatchers.IO) {
        compraDao.countComprasByGuia(guiaId)
    }
}