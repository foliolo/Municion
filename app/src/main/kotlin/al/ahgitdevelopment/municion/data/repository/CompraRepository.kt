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
 * Repository para Compra
 *
 * FASE 2.4: Repository Pattern
 * - Single source of truth: Room es la fuente principal
 * - Sincronización bidireccional con Firebase
 * - Offline-first: datos siempre disponibles desde Room
 * - Flow para observar cambios reactivamente
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
     * Observa TODAS las compras (Room es source of truth)
     */
    val compras: Flow<List<Compra>> = compraDao.getAllComprasFlow()

    /**
     * Observa compras de una guía específica
     */
    fun getComprasByGuia(guiaId: Int): Flow<List<Compra>> {
        return compraDao.getComprasByGuiaFlow(guiaId)
    }

    /**
     * Obtiene una compra por ID
     */
    suspend fun getCompraById(id: Int): Compra? = withContext(Dispatchers.IO) {
        compraDao.getCompraById(id)
    }

    /**
     * Guarda una compra (local + sync a Firebase)
     */
    suspend fun saveCompra(compra: Compra, userId: String? = null): Result<Long> = withContext(Dispatchers.IO) {
        try {
            // 1. Guardar en Room (source of truth)
            val id = compraDao.insert(compra)

            // 2. Sync a Firebase si hay userId
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
     * Actualiza una compra
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
     * Elimina una compra
     */
    suspend fun deleteCompra(compra: Compra, userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            compraDao.delete(compra)

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
     * Sincroniza DESDE Firebase → Room con parseo manual y reporte a Crashlytics
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

        // Campos obligatorios
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

        // Campos opcionales
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
     * Sincroniza HACIA Firebase ← Room (upload)
     */
    suspend fun syncToFirebase(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val localCompras = compraDao.getAllCompras()

            firebaseDb
                .child("users")
                .child(userId)
                .child("db")
                .child("compras")
                .setValue(localCompras)
                .await()

            android.util.Log.i("CompraRepository", "Synced ${localCompras.size} compras to Firebase")

            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.log("Failed to sync compras to Firebase: ${e.message}")
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Detecta compras con datos corruptos (peso = 0)
     */
    suspend fun detectCorruptedCompras(): List<Compra> = withContext(Dispatchers.IO) {
        compraDao.getComprasWithCorruptedPeso()
    }

    /**
     * Cuenta compras de una guía
     */
    suspend fun countComprasByGuia(guiaId: Int): Int = withContext(Dispatchers.IO) {
        compraDao.countComprasByGuia(guiaId)
    }
}
