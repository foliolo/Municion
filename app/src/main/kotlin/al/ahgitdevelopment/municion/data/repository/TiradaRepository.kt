package al.ahgitdevelopment.municion.data.repository

import al.ahgitdevelopment.municion.data.local.room.dao.TiradaDao
import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
import com.google.firebase.crashlytics.FirebaseCrashlytics
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
     * Sincroniza DESDE Firebase → Room
     */
    suspend fun syncFromFirebase(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firebaseDb
                .child("users")
                .child(userId)
                .child("db")
                .child("tiradas")
                .get()
                .await()

            val firebaseTiradas = mutableListOf<Tirada>()
            snapshot.children.forEach { child ->
                child.getValue(Tirada::class.java)?.let {
                    firebaseTiradas.add(it)
                }
            }

            if (firebaseTiradas.isNotEmpty()) {
                tiradaDao.replaceAll(firebaseTiradas)
                android.util.Log.i("TiradaRepository", "Synced ${firebaseTiradas.size} tiradas from Firebase")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.log("Failed to sync tiradas from Firebase: ${e.message}")
            crashlytics.recordException(e)
            Result.failure(e)
        }
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
