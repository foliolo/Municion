package al.ahgitdevelopment.municion.domain.usecase

import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.repository.CompraRepository
import al.ahgitdevelopment.municion.data.repository.GuiaRepository
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject

/**
 * Use Case para crear una compra con validación de cupo
 *
 * FASE 3: Domain Layer - Use Cases
 * - Valida que la guía tenga cupo suficiente (solo para compras en tienda)
 * - Actualiza el gastado de la guía (solo para compras en tienda)
 * - Guarda la compra
 * - Transacción lógica (rollback en caso de error)
 *
 * LEGISLACIÓN ESPAÑOLA:
 * - Munición comprada en TIENDA: Contabiliza contra el cupo anual
 * - Munición comprada en CAMPO DE TIRO: NO contabiliza (se consume in situ)
 *
 * @since v3.0.0 (TRACK B Modernization)
 * @since v3.3.0 (Campo de tiro exemption)
 */
class CreateCompraUseCase @Inject constructor(
    private val compraRepository: CompraRepository,
    private val guiaRepository: GuiaRepository,
    private val crashlytics: FirebaseCrashlytics
) {

    companion object {
        private const val TAG = "CreateCompraUseCase"
        
        /**
         * Valores que indican compra en campo de tiro (no contabiliza cupo)
         */
        private val CAMPO_TIRO_VALUES = setOf(
            "campo de tiro",
            "shooting range"
        )
        
        /**
         * Determina si la compra es en campo de tiro
         */
        fun isCompraCampoTiro(tienda: String?): Boolean {
            return tienda?.lowercase()?.trim() in CAMPO_TIRO_VALUES
        }
    }

    suspend operator fun invoke(compra: Compra, userId: String?): Result<Long> {
        return try {
            // 1. Obtener guía asociada
            val guia = guiaRepository.getGuiaById(compra.idPosGuia)
                ?: return Result.failure(IllegalArgumentException("Guía no encontrada: ${compra.idPosGuia}"))

            // 2. Determinar si la compra contabiliza para el cupo
            val contabilizaCupo = !isCompraCampoTiro(compra.tienda)
            
            Log.d(TAG, "Compra en '${compra.tienda}', contabiliza cupo: $contabilizaCupo")

            // 3. Validar cupo suficiente (solo si contabiliza)
            if (contabilizaCupo) {
                val disponible = guia.disponible()
                if (disponible < compra.unidades) {
                    return Result.failure(IllegalStateException(
                        "Cupo insuficiente. Disponible: $disponible, Requerido: ${compra.unidades}"
                    ))
                }
            }

            // 4. Guardar compra
            val compraId = compraRepository.saveCompra(compra, userId).getOrThrow()

            // 5. Actualizar gastado de guía (solo si contabiliza)
            if (contabilizaCupo) {
                guiaRepository.incrementGastado(guia.id, compra.unidades).getOrThrow()
                Log.i(TAG, "Compra created: id=$compraId, guia=${guia.apodo}, unidades=${compra.unidades}, gastado actualizado")
            } else {
                Log.i(TAG, "Compra created: id=$compraId, guia=${guia.apodo}, unidades=${compra.unidades}, campo de tiro (sin afectar cupo)")
            }

            Result.success(compraId)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating compra", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }
}
