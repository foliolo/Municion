package al.ahgitdevelopment.municion.data.repository

import al.ahgitdevelopment.municion.data.local.room.dao.CompraDao
import al.ahgitdevelopment.municion.data.local.room.entities.Compra
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
 * Repository for Compra
 *
 * Uses per-entity Firebase writes instead of full-list sync.
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
@Singleton
class CompraRepository @Inject constructor(
    private val compraDao: CompraDao,
    private val syncHelper: FirebaseSyncHelper,
    private val crashlytics: FirebaseCrashlytics
) {

    companion object {
        private const val TAG = "CompraRepository"
        private const val ENTITY_PATH = "compras"
    }

    val compras: Flow<List<Compra>> = compraDao.getAllComprasFlow()

    fun getComprasByGuia(guiaId: Int): Flow<List<Compra>> {
        return compraDao.getComprasByGuiaFlow(guiaId)
    }

    suspend fun getCompraById(id: Int): Compra? = withContext(Dispatchers.IO) {
        compraDao.getCompraById(id)
    }

    suspend fun saveCompra(compra: Compra, userId: String? = null): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val stamped = compra.copy(updatedAt = System.currentTimeMillis())
            val id = compraDao.insert(stamped)
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

    suspend fun updateCompra(compra: Compra, userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val stamped = compra.copy(updatedAt = System.currentTimeMillis())
            compraDao.update(stamped)

            userId?.let {
                syncHelper.writeEntity(it, ENTITY_PATH, stamped.id, stamped.toFirebaseMap())
            }

            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    suspend fun deleteCompra(compra: Compra, userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            compraDao.delete(compra)

            userId?.let {
                syncHelper.deleteEntity(it, ENTITY_PATH, compra.id)
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

            val localTimestamps = compraDao.getAllTimestamps()
            val localIds = localTimestamps.keys

            val parseErrors = mutableListOf<ParseError>()

            val diffResult = syncHelper.syncFromFirebaseWithDiff(
                userId = userId,
                entityPath = ENTITY_PATH,
                localIds = localIds,
                localTimestamps = localTimestamps,
                parser = { key, value ->
                    parseCompraFromMap(key, value, parseErrors)
                },
                getId = { it.id },
                getUpdatedAt = { it.updatedAt },
                upsert = { compraDao.insert(it) },
                deleteLocal = { compraDao.deleteById(it) }
            )

            val hasLocalData = compraDao.getCount() > 0

            Result.success(SyncResultWithErrors(
                success = diffResult.success,
                syncedCount = diffResult.upserted,
                totalInFirebase = diffResult.totalInFirebase,
                parseErrors = parseErrors,
                hasLocalData = hasLocalData
            ))
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed: ${e.message}", e)
            crashlytics.log("Failed to sync compras from Firebase: ${e.message}")
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Full sync TO Firebase (all compras as map). Used for auto-fix only.
     */
    suspend fun syncToFirebase(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val allCompras = compraDao.getAllCompras()
            val entityMaps = allCompras.associate { it.id.toString() to it.toFirebaseMap() }
            syncHelper.fullSyncToFirebase(userId, ENTITY_PATH, entityMaps)
        } catch (e: Exception) {
            crashlytics.log("Failed to full-sync compras to Firebase: ${e.message}")
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    suspend fun detectCorruptedCompras(): List<Compra> = withContext(Dispatchers.IO) {
        compraDao.getComprasWithCorruptedPeso()
    }

    suspend fun countComprasByGuia(guiaId: Int): Int = withContext(Dispatchers.IO) {
        compraDao.countComprasByGuia(guiaId)
    }

    // --- Parsing ---

    @Suppress("UNCHECKED_CAST")
    private fun parseCompraFromMap(
        itemKey: String,
        value: Any?,
        parseErrors: MutableList<ParseError>
    ): Compra? {
        val map = value as? Map<String, Any?> ?: run {
            reportFieldError(itemKey, "root", "Not a Map", value?.toString(), parseErrors)
            return null
        }

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

        val id = (map["id"] as? Number)?.toInt() ?: 0
        val calibre2 = map["calibre2"] as? String
        val tienda = map["tienda"] as? String
        val valoracion = (map["valoracion"] as? Number)?.toFloat() ?: 0f
        val imagePath = map["imagePath"] as? String
        val fotoUrl = map["fotoUrl"] as? String
        val storagePath = map["storagePath"] as? String
        val updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()

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
                imagePath = imagePath,
                fotoUrl = fotoUrl,
                storagePath = storagePath,
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
            entity = "Compra",
            itemKey = itemKey,
            failedField = fieldName,
            errorType = errorType,
            fieldValue = redactedValue
        ))

        crashlytics.apply {
            setCustomKey("entity", "Compra")
            setCustomKey("item_key", itemKey)
            setCustomKey("failed_field", fieldName)
            setCustomKey("error_type", errorType)
            setCustomKey("field_value", redactedValue)
            recordException(FirebaseParseException("[Compra] Field '$fieldName' failed: $errorType"))
        }

        Log.e(TAG, "Parse error Compra[$itemKey].$fieldName: $errorType (value: $redactedValue)")
    }
}
