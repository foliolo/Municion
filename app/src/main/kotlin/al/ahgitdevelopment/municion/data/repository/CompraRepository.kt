package al.ahgitdevelopment.municion.data.repository

import al.ahgitdevelopment.municion.data.local.room.dao.CompraDao
import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import com.google.firebase.crashlytics.FirebaseCrashlytics
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
     * Sincroniza DESDE Firebase → Room (download)
     */
    suspend fun syncFromFirebase(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firebaseDb
                .child("users")
                .child(userId)
                .child("db")
                .child("compras")
                .get()
                .await()

            val firebaseCompras = mutableListOf<Compra>()
            snapshot.children.forEach { child ->
                child.getValue(Compra::class.java)?.let {
                    firebaseCompras.add(it)
                }
            }

            if (firebaseCompras.isNotEmpty()) {
                compraDao.replaceAll(firebaseCompras)
                android.util.Log.i("CompraRepository", "Synced ${firebaseCompras.size} compras from Firebase")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            crashlytics.log("Failed to sync compras from Firebase: ${e.message}")
            crashlytics.recordException(e)
            Result.failure(e)
        }
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
