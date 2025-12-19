package al.ahgitdevelopment.municion.domain.usecase

import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.repository.CompraRepository
import al.ahgitdevelopment.municion.data.repository.GuiaRepository
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject

/**
 * Use Case para actualizar una compra con manejo correcto del cupo
 *
 * FASE 3: Domain Layer - Use Cases
 *
 * LEGISLACIÓN ESPAÑOLA:
 * - Munición comprada en TIENDA: Contabiliza contra el cupo anual
 * - Munición comprada en CAMPO DE TIRO: NO contabiliza (se consume in situ)
 *
 * Casos de actualización:
 * 1. Tienda → Tienda: Ajustar diferencia de unidades
 * 2. Campo → Campo: Sin cambios en cupo
 * 3. Tienda → Campo: Liberar unidades antiguas
 * 4. Campo → Tienda: Añadir unidades nuevas (validar cupo)
 *
 * @since v3.3.0 (Campo de tiro exemption)
 */
class UpdateCompraUseCase @Inject constructor(
    private val compraRepository: CompraRepository,
    private val guiaRepository: GuiaRepository,
    private val crashlytics: FirebaseCrashlytics
) {

    companion object {
        private const val TAG = "UpdateCompraUseCase"
    }

    suspend operator fun invoke(
        oldCompra: Compra,
        newCompra: Compra,
        userId: String?
    ): Result<Unit> {
        return try {
            val guia = guiaRepository.getGuiaById(newCompra.idPosGuia)
                ?: return Result.failure(IllegalArgumentException("Guía no encontrada: ${newCompra.idPosGuia}"))

            val oldContabilizaba = !CreateCompraUseCase.isCompraCampoTiro(oldCompra.tienda)
            val newContabiliza = !CreateCompraUseCase.isCompraCampoTiro(newCompra.tienda)

            Log.d(TAG, "Updating compra: oldTienda=${oldCompra.tienda} (contabilizaba=$oldContabilizaba), " +
                    "newTienda=${newCompra.tienda} (contabiliza=$newContabiliza)")

            // Calcular ajuste de cupo según los 4 casos
            val ajusteCupo = when {
                // Caso 1: Tienda → Tienda - Ajustar diferencia
                oldContabilizaba && newContabiliza -> {
                    val diferencia = newCompra.unidades - oldCompra.unidades
                    
                    // Validar si hay cupo suficiente para el incremento
                    if (diferencia > 0) {
                        val disponible = guia.disponible()
                        if (disponible < diferencia) {
                            return Result.failure(IllegalStateException(
                                "Cupo insuficiente. Disponible: $disponible, Incremento requerido: $diferencia"
                            ))
                        }
                    }
                    diferencia
                }
                
                // Caso 2: Campo → Campo - Sin cambios
                !oldContabilizaba && !newContabiliza -> {
                    0
                }
                
                // Caso 3: Tienda → Campo - Liberar unidades antiguas
                oldContabilizaba && !newContabiliza -> {
                    -oldCompra.unidades
                }
                
                // Caso 4: Campo → Tienda - Añadir unidades nuevas
                !oldContabilizaba && newContabiliza -> {
                    // Validar cupo
                    val disponible = guia.disponible()
                    if (disponible < newCompra.unidades) {
                        return Result.failure(IllegalStateException(
                            "Cupo insuficiente. Disponible: $disponible, Requerido: ${newCompra.unidades}"
                        ))
                    }
                    newCompra.unidades
                }
                
                else -> 0 // No debería llegar aquí
            }

            // Aplicar ajuste de cupo
            if (ajusteCupo != 0) {
                if (ajusteCupo > 0) {
                    guiaRepository.incrementGastado(guia.id, ajusteCupo).getOrThrow()
                } else {
                    guiaRepository.decrementGastado(guia.id, -ajusteCupo).getOrThrow()
                }
                Log.d(TAG, "Cupo ajustado en $ajusteCupo unidades")
            }

            // Actualizar la compra
            compraRepository.updateCompra(newCompra, userId).getOrThrow()

            Log.i(TAG, "Compra updated: id=${newCompra.id}, ajuste cupo=$ajusteCupo")

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating compra", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }
}
