package al.ahgitdevelopment.municion.domain.usecase

import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.repository.CompraRepository
import al.ahgitdevelopment.municion.data.repository.GuiaRepository
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject

/**
 * Use Case para eliminar una compra y liberar el cupo
 *
 * FASE 3: Domain Layer - Use Cases
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
class DeleteCompraUseCase @Inject constructor(
    private val compraRepository: CompraRepository,
    private val guiaRepository: GuiaRepository,
    private val crashlytics: FirebaseCrashlytics
) {

    suspend operator fun invoke(compra: Compra, userId: String?): Result<Unit> {
        return try {
            // 1. Liberar cupo de guía
            guiaRepository.decrementGastado(compra.idPosGuia, compra.unidades).getOrThrow()

            // 2. Eliminar compra
            compraRepository.deleteCompra(compra, userId).getOrThrow()

            android.util.Log.i("DeleteCompraUseCase", "Compra deleted: id=${compra.id}, unidades liberadas=${compra.unidades}")

            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("DeleteCompraUseCase", "Error deleting compra", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }
}
