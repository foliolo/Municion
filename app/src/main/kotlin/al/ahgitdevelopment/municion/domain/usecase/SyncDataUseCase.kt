package al.ahgitdevelopment.municion.domain.usecase

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
 * FASE 3: Domain Layer - Use Cases
 * - Sync paralelo de todas las entidades
 * - Manejo de errores individual por entidad
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
class SyncDataUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val compraRepository: CompraRepository,
    private val licenciaRepository: LicenciaRepository,
    private val tiradaRepository: TiradaRepository,
    private val crashlytics: FirebaseCrashlytics
) {

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
}
