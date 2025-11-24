package al.ahgitdevelopment.municion.data.repository

import al.ahgitdevelopment.municion.data.local.room.dao.GuiaDao
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository para Guia
 *
 * FASE 2.4: Repository Pattern
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
@Singleton
class GuiaRepository @Inject constructor(
    private val guiaDao: GuiaDao,
    private val firebaseDb: DatabaseReference,
    private val crashlytics: FirebaseCrashlytics
) {

    /**
     * Observa TODAS las guías
     */
    val guias: Flow<List<Guia>> = guiaDao.getAllGuiasFlow()

    /**
     * Observa guías por tipo de licencia
     */
    fun getGuiasByTipoLicencia(tipoLicencia: Int): Flow<List<Guia>> {
        return guiaDao.getGuiasByTipoLicenciaFlow(tipoLicencia)
    }

    /**
     * Obtiene una guía por ID
     */
    suspend fun getGuiaById(id: Int): Guia? = withContext(Dispatchers.IO) {
        guiaDao.getGuiaById(id)
    }

    /**
     * Guarda una guía
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
     * Actualiza una guía
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
     * Elimina una guía
     */
    suspend fun deleteGuia(guia: Guia, userId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            guiaDao.delete(guia)

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
     * Actualiza el gastado de una guía
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
     * Incrementa el cupo gastado
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
     * Decrementa el cupo gastado (para rollback)
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
     * Resetea el cupo gastado de TODAS las guías (para año nuevo)
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
     * Obtiene guías con cupo agotado
     */
    suspend fun getGuiasConCupoAgotado(): List<Guia> = withContext(Dispatchers.IO) {
        guiaDao.getGuiasConCupoAgotado()
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
                .child("guias")
                .get()
                .await()

            val firebaseGuias = mutableListOf<Guia>()
            snapshot.children.forEach { child ->
                child.getValue(Guia::class.java)?.let {
                    firebaseGuias.add(it)
                }
            }

            if (firebaseGuias.isNotEmpty()) {
                guiaDao.replaceAll(firebaseGuias)
                android.util.Log.i("GuiaRepository", "Synced ${firebaseGuias.size} guias from Firebase")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.log("Failed to sync guias from Firebase: ${e.message}")
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Sincroniza HACIA Firebase ← Room
     */
    suspend fun syncToFirebase(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val localGuias = guiaDao.getAllGuias()

            firebaseDb
                .child("users")
                .child(userId)
                .child("db")
                .child("guias")
                .setValue(localGuias)
                .await()

            android.util.Log.i("GuiaRepository", "Synced ${localGuias.size} guias to Firebase")

            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.log("Failed to sync guias to Firebase: ${e.message}")
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }
}
