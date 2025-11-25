package al.ahgitdevelopment.municion.ui.navigation

/**
 * Rutas de navegación string-based para Munición.
 *
 * @since v3.0.0 (Compose Migration)
 */
object Routes {
    // ========== TABS PRINCIPALES ==========
    const val LICENCIAS = "licencias"
    const val GUIAS = "guias"
    const val COMPRAS = "compras"
    const val TIRADAS = "tiradas"
    const val SETTINGS = "settings"

    // ========== FORMULARIOS ==========
    const val LICENCIA_FORM = "licenciaForm"
    const val GUIA_FORM = "guiaForm"
    const val COMPRA_FORM = "compraForm"
    const val TIRADA_FORM = "tiradaForm"

    // ========== CONSTRUCCIÓN DE RUTAS CON PARÁMETROS ==========

    /**
     * Ruta de formulario de licencia
     * @param licenciaId ID opcional para edición
     */
    fun licenciaForm(licenciaId: Int? = null): String {
        return if (licenciaId != null) {
            "$LICENCIA_FORM/$licenciaId"
        } else {
            LICENCIA_FORM
        }
    }

    /**
     * Ruta de formulario de guía
     * @param tipoLicencia Tipo de licencia (requerido para crear)
     * @param guiaId ID opcional para edición
     */
    fun guiaForm(tipoLicencia: String = "", guiaId: Int? = null): String {
        return if (guiaId != null) {
            "$GUIA_FORM/$tipoLicencia/$guiaId"
        } else {
            "$GUIA_FORM/$tipoLicencia"
        }
    }

    /**
     * Ruta de formulario de compra
     * @param guiaId ID de la guía (requerido)
     * @param cupoDisponible Cupo disponible
     * @param cupoTotal Cupo total
     * @param compraId ID opcional para edición
     */
    fun compraForm(guiaId: Int, cupoDisponible: Int, cupoTotal: Int, compraId: Int? = null): String {
        return if (compraId != null) {
            "$COMPRA_FORM/$guiaId/$cupoDisponible/$cupoTotal/$compraId"
        } else {
            "$COMPRA_FORM/$guiaId/$cupoDisponible/$cupoTotal"
        }
    }

    /**
     * Ruta de formulario de tirada
     * @param tiradaId ID opcional para edición
     */
    fun tiradaForm(tiradaId: Int? = null): String {
        return if (tiradaId != null) {
            "$TIRADA_FORM/$tiradaId"
        } else {
            TIRADA_FORM
        }
    }
}

// Mantener Route sealed interface por compatibilidad temporal
sealed interface Route {
    data object Licencias : Route
    data object Guias : Route
    data object Compras : Route
    data object Tiradas : Route
    data object Settings : Route
    data class LicenciaForm(val licenciaId: Int? = null) : Route
    data class GuiaForm(val tipoLicencia: String = "", val guiaId: Int? = null) : Route
    data class CompraForm(val guiaId: Int, val compraId: Int? = null, val cupoDisponible: Int = 0, val cupoTotal: Int = 0) : Route
    data class TiradaForm(val tiradaId: Int? = null) : Route
}
