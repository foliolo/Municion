package al.ahgitdevelopment.municion.domain.usecase

import al.ahgitdevelopment.municion.auth.FirebaseAuthRepository
import al.ahgitdevelopment.municion.data.repository.CompraRepository
import al.ahgitdevelopment.municion.data.repository.GuiaRepository
import al.ahgitdevelopment.municion.data.repository.LicenciaRepository
import al.ahgitdevelopment.municion.data.repository.TiradaRepository
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/**
 * Use Case para sincronizar TODOS los datos con Firebase
 *
 * FASE 5: Sync Bidireccional Inteligente
 * - Verifica estado de autenticación antes de sincronizar
 * - Solo sincroniza si el usuario está autenticado
 * - Usuarios anónimos: sync bidireccional (local-first)
 * - Usuarios vinculados: sync completo con prioridad cloud
 * - Manejo de errores individual por entidad
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
class SyncDataUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val compraRepository: CompraRepository,
    private val licenciaRepository: LicenciaRepository,
    private val tiradaRepository: TiradaRepository,
    private val firebaseAuthRepository: FirebaseAuthRepository,
    private val crashlytics: FirebaseCrashlytics
) {

    companion object {
        private const val TAG = "SyncDataUseCase"
    }

    /**
     * Verifica si el usuario puede sincronizar
     * @return true si está autenticado (anónimo o vinculado)
     */
    fun canSync(): Boolean = firebaseAuthRepository.isAuthenticated()

    /**
     * Verifica si el usuario tiene cuenta vinculada (no anónima)
     * @return true si está vinculado con Google/Email
     */
    fun isLinked(): Boolean = firebaseAuthRepository.isLinked()

    /**
     * Obtiene el ID del usuario actual
     */
    fun getCurrentUserId(): String? = firebaseAuthRepository.getCurrentUser()?.uid

    /**
     * Sincroniza desde Firebase → Room (download)
     */
    suspend fun syncFromFirebase(userId: String): Result<SyncResult> = coroutineScope {
        try {
            val guiasDeferred = async { guiaRepository.syncFromFirebase(userId) }
            val comprasDeferred = async { compraRepository.syncFromFirebase(userId) }
            val licenciasDeferred = async { licenciaRepository.syncFromFirebase(userId) }
            val tiradasDeferred = async { tiradaRepository.syncFromFirebase(userId) }

            val guiasResult = guiasDeferred.await()
            val comprasResult = comprasDeferred.await()
            val licenciasResult = licenciasDeferred.await()
            val tiradasResult = tiradasDeferred.await()

            val successCount = listOf(guiasResult, comprasResult, licenciasResult, tiradasResult)
                .count { it.isSuccess }

            android.util.Log.i("SyncDataUseCase", "Sync from Firebase: $successCount/4 successful")

            Result.success(
                SyncResult(
                    guiasSuccess = guiasResult.isSuccess,
                    comprasSuccess = comprasResult.isSuccess,
                    licenciasSuccess = licenciasResult.isSuccess,
                    tiradasSuccess = tiradasResult.isSuccess
                )
            )
        } catch (e: Exception) {
            android.util.Log.e("SyncDataUseCase", "Error syncing from Firebase", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Sincroniza hacia Firebase ← Room (upload)
     */
    suspend fun syncToFirebase(userId: String): Result<SyncResult> = coroutineScope {
        try {
            val guiasDeferred = async { guiaRepository.syncToFirebase(userId) }
            val comprasDeferred = async { compraRepository.syncToFirebase(userId) }
            val licenciasDeferred = async { licenciaRepository.syncToFirebase(userId) }
            val tiradasDeferred = async { tiradaRepository.syncToFirebase(userId) }

            val guiasResult = guiasDeferred.await()
            val comprasResult = comprasDeferred.await()
            val licenciasResult = licenciasDeferred.await()
            val tiradasResult = tiradasDeferred.await()

            val successCount = listOf(guiasResult, comprasResult, licenciasResult, tiradasResult)
                .count { it.isSuccess }

            android.util.Log.i("SyncDataUseCase", "Sync to Firebase: $successCount/4 successful")

            Result.success(
                SyncResult(
                    guiasSuccess = guiasResult.isSuccess,
                    comprasSuccess = comprasResult.isSuccess,
                    licenciasSuccess = licenciasResult.isSuccess,
                    tiradasSuccess = tiradasResult.isSuccess
                )
            )
        } catch (e: Exception) {
            android.util.Log.e("SyncDataUseCase", "Error syncing to Firebase", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Sincronización bidireccional inteligente
     *
     * Estrategia:
     * - Si el usuario NO está autenticado: no sincroniza (solo local)
     * - Si el usuario está autenticado (anónimo o vinculado):
     *   1. Primero descarga de Firebase (por si hay cambios en otro dispositivo)
     *   2. Luego sube cambios locales a Firebase
     *
     * @return BidirectionalSyncResult con detalles de ambas operaciones
     */
    suspend fun syncBidirectional(): Result<BidirectionalSyncResult> {
        // Verificar autenticación
        if (!canSync()) {
            android.util.Log.w(TAG, "Cannot sync: user not authenticated")
            return Result.success(
                BidirectionalSyncResult(
                    downloadResult = null,
                    uploadResult = null,
                    skipped = true,
                    reason = "Usuario no autenticado"
                )
            )
        }

        val userId = getCurrentUserId()
        if (userId == null) {
            android.util.Log.w(TAG, "Cannot sync: no user ID")
            return Result.failure(Exception("No user ID available"))
        }

        return try {
            android.util.Log.i(TAG, "Starting bidirectional sync for user: $userId")

            // Paso 1: Descargar de Firebase → Room
            val downloadResult = syncFromFirebase(userId).getOrNull()
            android.util.Log.i(TAG, "Download completed: ${downloadResult?.successCount ?: 0}/4")

            // Paso 2: Subir de Room → Firebase
            val uploadResult = syncToFirebase(userId).getOrNull()
            android.util.Log.i(TAG, "Upload completed: ${uploadResult?.successCount ?: 0}/4")

            Result.success(
                BidirectionalSyncResult(
                    downloadResult = downloadResult,
                    uploadResult = uploadResult,
                    skipped = false,
                    reason = null
                )
            )
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Bidirectional sync failed", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Sincronización automática al iniciar/pausar la app
     * Solo sincroniza si hay conexión y el usuario está autenticado
     */
    suspend fun autoSync(): Result<BidirectionalSyncResult> {
        // Solo usuarios vinculados sincronizan automáticamente
        // Usuarios anónimos deben sincronizar manualmente para evitar
        // sobrescribir datos accidentalmente
        if (!isLinked()) {
            android.util.Log.d(TAG, "Auto-sync skipped: user is anonymous")
            return Result.success(
                BidirectionalSyncResult(
                    downloadResult = null,
                    uploadResult = null,
                    skipped = true,
                    reason = "Auto-sync solo para cuentas vinculadas"
                )
            )
        }

        return syncBidirectional()
    }

    data class SyncResult(
        val guiasSuccess: Boolean,
        val comprasSuccess: Boolean,
        val licenciasSuccess: Boolean,
        val tiradasSuccess: Boolean
    ) {
        val allSuccess: Boolean
            get() = guiasSuccess && comprasSuccess && licenciasSuccess && tiradasSuccess

        val successCount: Int
            get() = listOf(guiasSuccess, comprasSuccess, licenciasSuccess, tiradasSuccess)
                .count { it }
    }

    data class BidirectionalSyncResult(
        val downloadResult: SyncResult?,
        val uploadResult: SyncResult?,
        val skipped: Boolean,
        val reason: String?
    ) {
        val allSuccess: Boolean
            get() = !skipped &&
                    (downloadResult?.allSuccess ?: false) &&
                    (uploadResult?.allSuccess ?: false)

        val totalSuccessCount: Int
            get() = (downloadResult?.successCount ?: 0) + (uploadResult?.successCount ?: 0)
    }
}
