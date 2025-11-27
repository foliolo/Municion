package al.ahgitdevelopment.municion.ui.navigation.navtypes

import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
import kotlinx.serialization.serializer

/**
 * Custom NavTypes para las 4 entidades principales de Munición.
 *
 * Cada NavType:
 * - Maneja serialización JSON type-safe
 * - Valida datos durante deserialización
 * - Reporta errores a Crashlytics
 * - Soporta valores nullable (para formularios de creación)
 *
 * @since v3.3.0 (NavType Architecture Migration)
 */

/**
 * NavType para Licencia (nullable para crear nueva licencia)
 */
object LicenciaNavType : BaseNavType<Licencia>(
    isNullableAllowed = true,
    serializer = serializer<Licencia>(),
    type = Licencia::class
) {
    override fun validateEntity(entity: Licencia) {
        // Validaciones adicionales si son necesarias
        // Las validaciones básicas ya están en Licencia.init {}

        // Validar formato de fecha
        require(entity.getFechaExpedicionDate() != null) {
            "Licencia ${entity.id}: fechaExpedicion inválida: ${entity.fechaExpedicion}"
        }
        require(entity.getFechaCaducidadDate() != null) {
            "Licencia ${entity.id}: fechaCaducidad inválida: ${entity.fechaCaducidad}"
        }
    }
}

/**
 * NavType para Guia (nullable para crear nueva guía)
 */
object GuiaNavType : BaseNavType<Guia>(
    isNullableAllowed = true,
    serializer = serializer<Guia>(),
    type = Guia::class
) {
    override fun validateEntity(entity: Guia) {
        // Validar cupo lógico
        require(entity.gastado <= entity.cupo) {
            "Guia ${entity.id}: gastado (${entity.gastado}) > cupo (${entity.cupo})"
        }
        require(entity.gastado >= 0) {
            "Guia ${entity.id}: gastado no puede ser negativo: ${entity.gastado}"
        }
    }
}

/**
 * NavType para Compra (nullable para crear nueva compra)
 */
object CompraNavType : BaseNavType<Compra>(
    isNullableAllowed = true,
    serializer = serializer<Compra>(),
    type = Compra::class
) {
    override fun validateEntity(entity: Compra) {
        // Validar campos numéricos
        require(entity.unidades > 0) {
            "Compra ${entity.id}: unidades debe ser > 0, got: ${entity.unidades}"
        }
        require(entity.precio >= 0) {
            "Compra ${entity.id}: precio no puede ser negativo: ${entity.precio}"
        }

        // Validar fecha
        require(entity.getFechaDate() != null) {
            "Compra ${entity.id}: fecha inválida: ${entity.fecha}"
        }
    }
}

/**
 * NavType para Tirada (nullable para crear nueva tirada)
 */
object TiradaNavType : BaseNavType<Tirada>(
    isNullableAllowed = true,
    serializer = serializer<Tirada>(),
    type = Tirada::class
) {
    override fun validateEntity(entity: Tirada) {
        // Validar fecha
        require(entity.getFechaDate() != null) {
            "Tirada ${entity.id}: fecha inválida: ${entity.fecha}"
        }

        // Validaciones ya en Tirada.init {} (puntuacion 0-600)
    }
}
