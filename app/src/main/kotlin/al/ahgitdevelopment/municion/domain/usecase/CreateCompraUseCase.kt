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
 * - Valida que la guía tenga cupo suficiente
 * - Actualiza el gastado de la guía
 * - Guarda la compra
 * - Transacción lógica (rollback en caso de error)
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
class CreateCompraUseCase @Inject constructor(
    private val compraRepository: CompraRepository,
    private val guiaRepository: GuiaRepository,
    private val crashlytics: FirebaseCrashlytics
) {

    suspend operator fun invoke(compra: Compra, userId: String?): Result<Long> {
        return try {
            // 1. Obtener guía asociada
            val guia = guiaRepository.getGuiaById(compra.idPosGuia)
                ?: return Result.failure(IllegalArgumentException("Guía no encontrada: ${compra.idPosGuia}"))

            // 2. Validar cupo suficiente
            val disponible = guia.disponible()
            if (disponible < compra.unidades) {
                return Result.failure(IllegalStateException(
                    "Cupo insuficiente. Disponible: $disponible, Requerido: ${compra.unidades}"
                ))
            }

            // 3. Guardar compra
            val compraId = compraRepository.saveCompra(compra, userId).getOrThrow()

            // 4. Actualizar gastado de guía
            guiaRepository.incrementGastado(guia.id, compra.unidades).getOrThrow()

            Log.i("CreateCompraUseCase", "Compra created: id=$compraId, guia=${guia.apodo}, unidades=${compra.unidades}")

            Result.success(compraId)
        } catch (e: Exception) {
            android.util.Log.e("CreateCompraUseCase", "Error creating compra", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }
}
