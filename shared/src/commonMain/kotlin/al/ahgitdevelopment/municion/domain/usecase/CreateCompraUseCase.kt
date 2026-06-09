package al.ahgitdevelopment.municion.domain.usecase

import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.repository.CompraRepository
import al.ahgitdevelopment.municion.data.repository.GuiaRepository
import al.ahgitdevelopment.municion.firebase.CrashReporter

/**
 * Creates a purchase, validating and consuming the parent guía's quota.
 *
 * Spanish law: ammunition bought IN A STORE counts against the annual quota; ammunition
 * bought AT THE SHOOTING RANGE (campo de tiro) does not (consumed on site).
 */
class CreateCompraUseCase(
    private val compraRepository: CompraRepository,
    private val guiaRepository: GuiaRepository,
    private val crashReporter: CrashReporter,
) {
    companion object {
        private val CAMPO_TIRO_VALUES = setOf("campo de tiro", "shooting range")

        fun isCompraCampoTiro(tienda: String?): Boolean = tienda?.lowercase()?.trim() in CAMPO_TIRO_VALUES
    }

    suspend operator fun invoke(
        compra: Compra,
        userId: String?,
    ): Result<Long> =
        try {
            val guia =
                guiaRepository.getGuiaById(compra.idPosGuia)
                    ?: return Result.failure(IllegalArgumentException("Guía no encontrada: ${compra.idPosGuia}"))

            val contabilizaCupo = !isCompraCampoTiro(compra.tienda)
            if (contabilizaCupo && guia.disponible() < compra.unidades) {
                return Result.failure(
                    IllegalStateException("Cupo insuficiente. Disponible: ${guia.disponible()}, Requerido: ${compra.unidades}"),
                )
            }

            val compraId = compraRepository.saveCompra(compra, userId).getOrThrow()
            if (contabilizaCupo) {
                guiaRepository.incrementGastado(guia.id, compra.unidades, userId).getOrThrow()
            }
            Result.success(compraId)
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(e)
        }
}
