package al.ahgitdevelopment.municion.domain.usecase

import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.repository.CompraRepository
import al.ahgitdevelopment.municion.data.repository.GuiaRepository
import al.ahgitdevelopment.municion.firebase.CrashReporter

/** Deletes a purchase, releasing the guía's quota only if it was a store purchase. */
class DeleteCompraUseCase(
    private val compraRepository: CompraRepository,
    private val guiaRepository: GuiaRepository,
    private val crashReporter: CrashReporter,
) {
    suspend operator fun invoke(
        compra: Compra,
        userId: String?,
    ): Result<Unit> =
        try {
            if (!CreateCompraUseCase.isCompraCampoTiro(compra.tienda)) {
                guiaRepository.decrementGastado(compra.idPosGuia, compra.unidades, userId).getOrThrow()
            }
            compraRepository.deleteCompra(compra, userId).getOrThrow()
            Result.success(Unit)
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(e)
        }
}
