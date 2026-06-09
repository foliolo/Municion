package al.ahgitdevelopment.municion.domain.usecase

import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.repository.CompraRepository
import al.ahgitdevelopment.municion.data.repository.GuiaRepository
import al.ahgitdevelopment.municion.firebase.CrashReporter

/**
 * Updates a purchase, adjusting the guía's quota across the four store/range transitions.
 */
class UpdateCompraUseCase(
    private val compraRepository: CompraRepository,
    private val guiaRepository: GuiaRepository,
    private val crashReporter: CrashReporter,
) {
    suspend operator fun invoke(
        oldCompra: Compra,
        newCompra: Compra,
        userId: String?,
    ): Result<Unit> =
        try {
            val guia =
                guiaRepository.getGuiaById(newCompra.idPosGuia)
                    ?: return Result.failure(IllegalArgumentException("Guía no encontrada: ${newCompra.idPosGuia}"))

            val oldCounts = !CreateCompraUseCase.isCompraCampoTiro(oldCompra.tienda)
            val newCounts = !CreateCompraUseCase.isCompraCampoTiro(newCompra.tienda)

            val ajusteCupo =
                when {
                    oldCounts && newCounts -> {
                        val diff = newCompra.unidades - oldCompra.unidades
                        if (diff > 0 && guia.disponible() < diff) {
                            return Result.failure(
                                IllegalStateException("Cupo insuficiente. Disponible: ${guia.disponible()}, Incremento: $diff"),
                            )
                        }
                        diff
                    }
                    !oldCounts && !newCounts -> 0
                    oldCounts && !newCounts -> -oldCompra.unidades
                    else -> { // !oldCounts && newCounts
                        if (guia.disponible() < newCompra.unidades) {
                            return Result.failure(
                                IllegalStateException(
                                    "Cupo insuficiente. Disponible: ${guia.disponible()}, Requerido: ${newCompra.unidades}",
                                ),
                            )
                        }
                        newCompra.unidades
                    }
                }

            if (ajusteCupo > 0) {
                guiaRepository.incrementGastado(guia.id, ajusteCupo, userId).getOrThrow()
            } else if (ajusteCupo < 0) {
                guiaRepository.decrementGastado(guia.id, -ajusteCupo, userId).getOrThrow()
            }

            compraRepository.updateCompra(newCompra, userId).getOrThrow()
            Result.success(Unit)
        } catch (e: Exception) {
            crashReporter.recordException(e)
            Result.failure(e)
        }
}
