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
 * Repository para Licencia
 *
 * FASE 2.4: Repository Pattern
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
     * Observa TODAS las licencias
     */
    val licencias: Flow<List<Licencia>> = licenciaDao.getAllLicenciasFlow()

    /**
     * Observa licencias por tipo
     */
    fun getLicenciasByTipo(tipo: Int): Flow<List<Licencia>> {
        return licenciaDao.getLicenciasByTipoFlow(tipo)
    }

    /**
     * Obtiene una licencia por ID
     */
    suspend fun getLicenciaById(id: Int): Licencia? = withContext(Dispatchers.IO) {
        licenciaDao.getLicenciaById(id)
    }

    /**
     * Obtiene licencia por número
     */
    suspend fun getLicenciaByNumero(numLicencia: String): Licencia? = withContext(Dispatchers.IO) {
        licenciaDao.getLicenciaByNumero(numLicencia)
    }

    /**
     * Verifica si existe una licencia
     */
    suspend fun existsLicencia(numLicencia: String): Boolean = withContext(Dispatchers.IO) {
        licenciaDao.existsLicencia(numLicencia)
    }

    /**
     * Guarda una licencia
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
     * Actualiza una licencia
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
     * Elimina una licencia
     */
    suspend fun deleteLicencia(licencia: Licencia, userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            licenciaDao.delete(licencia)

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
            // Configurar userId en Crashlytics para tracking
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
     * Parsea y valida una Licencia campo por campo, reportando errores específicos
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

        // Campos obligatorios con validación
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

        // Campos opcionales
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
            // Captura cualquier otra excepción del constructor
            reportFieldError(userId, itemKey, "constructor", e.message ?: "Unknown", null, parseErrors)
            null
        }
    }

    /**
     * Reporta un error de campo a Crashlytics y lo agrega a la lista de errores
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

        // Reportar a Crashlytics
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
     * Sincroniza HACIA Firebase ← Room
     */
    suspend fun syncToFirebase(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val localLicencias = licenciaDao.getAllLicencias()

            firebaseDb
                .child("users")
                .child(userId)
                .child("db")
                .child("licencias")
                .setValue(localLicencias)
                .await()

            android.util.Log.i("LicenciaRepository", "Synced ${localLicencias.size} licencias to Firebase")

            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.log("Failed to sync licencias to Firebase: ${e.message}")
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }
}
