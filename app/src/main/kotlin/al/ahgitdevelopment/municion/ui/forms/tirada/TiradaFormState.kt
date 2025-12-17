package al.ahgitdevelopment.municion.ui.forms.tirada

import al.ahgitdevelopment.municion.data.local.room.entities.Tirada

/**
 * Estado del formulario de Tirada.
 * 
 * Encapsula todos los campos del formulario y sus errores de validación.
 * Inmutable - cada cambio genera una nueva instancia.
 *
 * @since v3.2.2 (Form Architecture Refactor - MVI Pattern)
 */
data class TiradaFormState(
    // Campos del formulario
    val descripcion: String = "",
    val localizacion: String = "",
    val categoria: String = "",
    val modalidad: String = Tirada.MODALIDAD_PRECISION,
    val fecha: String = "",
    val puntuacion: Float = 0f,
    
    // Metadata
    val tiradaId: Int = 0,
    val isEditing: Boolean = false,
    
    // Errores de validación
    val descripcionError: String? = null,
    val fechaError: String? = null
) {
    /**
     * Puntuación máxima según la modalidad actual
     */
    val maxPuntuacion: Float
        get() = Tirada.getMaxPuntuacion(modalidad).toFloat()
    
    /**
     * Verifica si es modalidad IPSC
     */
    val isIpsc: Boolean
        get() = modalidad == Tirada.MODALIDAD_IPSC
    
    /**
     * Sufijo de puntuación según modalidad
     */
    val scoreSuffix: String
        get() = if (isIpsc) "%" else "pts"
    
    /**
     * Valida si el formulario tiene errores
     */
    val hasErrors: Boolean
        get() = listOf(descripcionError, fechaError).any { it != null }
    
    /**
     * Crea una instancia de Tirada desde el estado actual
     */
    fun toTirada(): Tirada = Tirada(
        id = tiradaId,
        descripcion = descripcion,
        localizacion = localizacion.ifBlank { null },
        categoria = categoria.ifBlank { null },
        modalidad = modalidad.ifBlank { null },
        fecha = fecha,
        puntuacion = puntuacion.toInt().coerceIn(0, maxPuntuacion.toInt())
    )
    
    companion object {
        /**
         * Crea estado inicial desde una Tirada existente (edición)
         */
        fun fromTirada(tirada: Tirada): TiradaFormState = TiradaFormState(
            tiradaId = tirada.id,
            descripcion = tirada.descripcion,
            localizacion = tirada.localizacion ?: "",
            categoria = tirada.categoria ?: "",
            modalidad = tirada.modalidad ?: Tirada.MODALIDAD_PRECISION,
            fecha = tirada.fecha,
            puntuacion = tirada.puntuacion.toFloat(),
            isEditing = true
        )
        
        /**
         * Crea estado inicial vacío (creación)
         */
        fun empty(currentDate: String): TiradaFormState = TiradaFormState(
            fecha = currentDate,
            isEditing = false
        )
    }
}

/**
 * Estados de UI del formulario
 */
sealed class TiradaFormUiState {
    data object Idle : TiradaFormUiState()
    data object Loading : TiradaFormUiState()
    data class Success(val message: String) : TiradaFormUiState()
    data class Error(val message: String) : TiradaFormUiState()
}

/**
 * Eventos del formulario (acciones del usuario)
 */
sealed class TiradaFormEvent {
    // Cambios de campos
    data class DescripcionChanged(val value: String) : TiradaFormEvent()
    data class LocalizacionChanged(val value: String) : TiradaFormEvent()
    data class CategoriaChanged(val value: String) : TiradaFormEvent()
    data class ModalidadChanged(val value: String) : TiradaFormEvent()
    data class FechaChanged(val value: String) : TiradaFormEvent()
    data class PuntuacionChanged(val value: Float) : TiradaFormEvent()
    
    // Acciones
    data object Save : TiradaFormEvent()
    data object ResetErrors : TiradaFormEvent()
}
