package al.ahgitdevelopment.municion.domain.usecase

import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.repository.CompraRepository
import al.ahgitdevelopment.municion.data.repository.GuiaRepository
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject

/**
 * Use Case para eliminar una compra y liberar el cupo
 *
 * FASE 3: Domain Layer - Use Cases
 *
 * LEGISLACIÓN ESPAÑOLA:
 * - Solo liberar cupo si la compra era en TIENDA
 * - Las compras en CAMPO DE TIRO no afectan el cupo
 *
 * @since v3.0.0 (TRACK B Modernization)
 * @since v3.3.0 (Campo de tiro exemption)
 */
class DeleteCompraUseCase @Inject constructor(
    private val compraRepository: CompraRepository,
    private val guiaRepository: GuiaRepository,
    private val crashlytics: FirebaseCrashlytics
) {

    companion object {
        private const val TAG = "DeleteCompraUseCase"
    }

    suspend operator fun invoke(compra: Compra, userId: String?): Result<Unit> {
        return try {
            // Solo liberar cupo si la compra era en tienda (no campo de tiro)
            val contabilizabaCupo = !CreateCompraUseCase.isCompraCampoTiro(compra.tienda)
            
            if (contabilizabaCupo) {
                // 1. Liberar cupo de guía
                guiaRepository.decrementGastado(compra.idPosGuia, compra.unidades).getOrThrow()
                Log.i(TAG, "Compra deleted: id=${compra.id}, unidades liberadas=${compra.unidades}")
            } else {
                Log.i(TAG, "Compra deleted: id=${compra.id}, campo de tiro (sin liberar cupo)")
            }

            // 2. Eliminar compra
            compraRepository.deleteCompra(compra, userId).getOrThrow()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting compra", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }
}
