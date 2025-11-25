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
 * Repository para Tirada
 *
 * FASE 2.4: Repository Pattern
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
     * Observa TODAS las tiradas
     */
    val tiradas: Flow<List<Tirada>> = tiradaDao.getAllTiradasFlow()

    /**
     * Obtiene una tirada por ID
     */
    suspend fun getTiradaById(id: Int): Tirada? = withContext(Dispatchers.IO) {
        tiradaDao.getTiradaById(id)
    }

    /**
     * Obtiene tiradas con puntuación
     */
    suspend fun getTiradasConPuntuacion(): List<Tirada> = withContext(Dispatchers.IO) {
        tiradaDao.getTiradasConPuntuacion()
    }

    /**
     * Calcula estadísticas
     */
    suspend fun getEstadisticas(): TiradaEstadisticas = withContext(Dispatchers.IO) {
        TiradaEstadisticas(
            total = tiradaDao.countTiradas(),
            promedio = tiradaDao.getPromedioPuntuacion() ?: 0f,
            mejor = tiradaDao.getMejorPuntuacion() ?: 0f
        )
    }

    /**
     * Guarda una tirada
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
     * Actualiza una tirada
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
     * Elimina una tirada
     */
    suspend fun deleteTirada(tirada: Tirada, userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            tiradaDao.delete(tirada)

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

        // Campos obligatorios
        val descripcion = (map["descripcion"] as? String)?.takeIf { it.isNotBlank() } ?: run {
            reportFieldError(itemKey, "descripcion", "Blank or missing", map["descripcion"]?.toString(), parseErrors)
            return null
        }

        val fecha = (map["fecha"] as? String)?.takeIf { it.isNotBlank() } ?: run {
            reportFieldError(itemKey, "fecha", "Blank or missing", map["fecha"]?.toString(), parseErrors)
            return null
        }

        // Campos opcionales
        val id = (map["id"] as? Number)?.toInt() ?: 0
        val rango = map["rango"] as? String
        val puntuacion = (map["puntuacion"] as? Number)?.toInt() ?: 0

        // Validar rango de puntuacion
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
     * Sincroniza HACIA Firebase ← Room
     */
    suspend fun syncToFirebase(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val localTiradas = tiradaDao.getAllTiradas()

            firebaseDb
                .child("users")
                .child(userId)
                .child("db")
                .child("tiradas")
                .setValue(localTiradas)
                .await()

            android.util.Log.i("TiradaRepository", "Synced ${localTiradas.size} tiradas to Firebase")

            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.log("Failed to sync tiradas to Firebase: ${e.message}")
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Data class para estadísticas de tiradas
     */
    data class TiradaEstadisticas(
        val total: Int,
        val promedio: Float,
        val mejor: Float
    )
}
