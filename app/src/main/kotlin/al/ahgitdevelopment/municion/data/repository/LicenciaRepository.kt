package al.ahgitdevelopment.municion.data.repository

import al.ahgitdevelopment.municion.data.local.room.dao.LicenciaDao
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import com.google.firebase.crashlytics.FirebaseCrashlytics
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
     * Sincroniza DESDE Firebase → Room
     */
    suspend fun syncFromFirebase(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firebaseDb
                .child("users")
                .child(userId)
                .child("db")
                .child("licencias")
                .get()
                .await()

            val firebaseLicencias = mutableListOf<Licencia>()
            snapshot.children.forEach { child ->
                child.getValue(Licencia::class.java)?.let {
                    firebaseLicencias.add(it)
                }
            }

            if (firebaseLicencias.isNotEmpty()) {
                licenciaDao.replaceAll(firebaseLicencias)
                android.util.Log.i("LicenciaRepository", "Synced ${firebaseLicencias.size} licencias from Firebase")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.log("Failed to sync licencias from Firebase: ${e.message}")
            crashlytics.recordException(e)
            Result.failure(e)
        }
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
