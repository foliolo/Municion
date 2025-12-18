package al.ahgitdevelopment.municion.ui.navigation

import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes usando Kotlinx Serialization.
 *
 * Migración completada: Ahora se pasan objetos completos en lugar de solo IDs,
 * eliminando la necesidad de lookups en ViewModels y eliminando race conditions.
 *
 * @since v3.2.0 (Object-Based Navigation Migration)
 */
sealed interface Route

// ========== AUTENTICACIÓN ==========

/**
 * Pantalla de Login/Registro.
 * Primera pantalla para usuarios no autenticados.
 *
 * @since v3.4.0 (Auth Simplification)
 */
@Serializable
data object Login : Route

/**
 * Pantalla de Migración obligatoria.
 * Para usuarios anónimos existentes que deben vincular su cuenta.
 *
 * @since v3.4.0 (Auth Simplification)
 */
@Serializable
data object Migration : Route

// ========== TABS PRINCIPALES ==========
@Serializable
data object Licencias : Route

@Serializable
data object Guias : Route

@Serializable
data object Compras : Route

@Serializable
data object Tiradas : Route

@Serializable
data object Settings : Route

// ========== FORMULARIOS ==========

/**
 * Formulario de Licencia.
 * @param licencia Objeto completo para editar, null para crear nueva
 */
@Serializable
data class LicenciaForm(val licencia: Licencia? = null) : Route

/**
 * Formulario de Guía.
 * @param guia Objeto completo para editar, null para crear nueva
 * @param tipoLicencia Tipo de licencia (solo necesario para crear nueva)
 */
@Serializable
data class GuiaForm(
    val guia: Guia? = null,
    val tipoLicencia: String = ""
) : Route

/**
 * Formulario de Compra.
 * @param compra Objeto completo para editar, null para crear nueva
 * @param guia Guía asociada (necesaria para validación de cupo, null no debería pasar)
 */
@Serializable
data class CompraForm(
    val compra: Compra? = null,
    val guia: Guia? = null
) : Route

/**
 * Formulario de Tirada.
 * @param tirada Objeto completo para editar, null para crear nueva
 */
@Serializable
data class TiradaForm(val tirada: Tirada? = null) : Route

