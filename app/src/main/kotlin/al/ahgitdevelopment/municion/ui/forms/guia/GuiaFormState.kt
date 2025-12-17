package al.ahgitdevelopment.municion.ui.forms.guia

import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import android.net.Uri

/**
 * Estado del formulario de Guía.
 * 
 * Encapsula todos los campos del formulario y sus errores de validación.
 * Inmutable - cada cambio genera una nueva instancia.
 *
 * @since v3.2.2 (Form Architecture Refactor)
 */
data class GuiaFormState(
    // Campos del formulario
    val marca: String = "",
    val modelo: String = "",
    val apodo: String = "",
    val tipoArma: Int = 0,
    val calibre1: String = "",
    val calibre2: String = "",
    val showCalibre2: Boolean = false,
    val numGuia: String = "",
    val numArma: String = "",
    val cupo: String = "",
    val gastado: String = "0",
    val customCupo: Boolean = false,
    
    // Estado de imagen
    val selectedImageUri: Uri? = null,
    val existingImageUrl: String? = null,
    val storagePath: String? = null,
    
    // Metadata
    val guiaId: Int = 0,
    val tipoLicencia: Int = 0,
    val isEditing: Boolean = false,
    
    // Errores de validación
    val marcaError: String? = null,
    val modeloError: String? = null,
    val apodoError: String? = null,
    val calibre1Error: String? = null,
    val numGuiaError: String? = null,
    val numArmaError: String? = null,
    val cupoError: String? = null
) {
    /**
     * URL de imagen a mostrar: nueva selección > existente > null
     */
    val currentImageUrl: String?
        get() = selectedImageUri?.toString() ?: existingImageUrl
    
    /**
     * Tiene nueva imagen seleccionada (pendiente de subir)
     */
    val hasNewImage: Boolean
        get() = selectedImageUri != null
    
    /**
     * Valida si el formulario tiene errores
     */
    val hasErrors: Boolean
        get() = listOf(
            marcaError, modeloError, apodoError, calibre1Error,
            numGuiaError, numArmaError, cupoError
        ).any { it != null }
    
    /**
     * Crea una instancia de Guia desde el estado actual
     */
    fun toGuia(): Guia = Guia(
        id = guiaId,
        tipoLicencia = tipoLicencia,
        marca = marca,
        modelo = modelo,
        apodo = apodo,
        tipoArma = tipoArma,
        calibre1 = calibre1,
        calibre2 = if (showCalibre2) calibre2 else null,
        numGuia = numGuia,
        numArma = numArma,
        cupo = cupo.toIntOrNull() ?: 0,
        gastado = gastado.toIntOrNull() ?: 0,
        fotoUrl = existingImageUrl,
        storagePath = storagePath
    )
    
    companion object {
        /**
         * Crea estado inicial desde una Guía existente (edición)
         */
        fun fromGuia(guia: Guia): GuiaFormState = GuiaFormState(
            guiaId = guia.id,
            tipoLicencia = guia.tipoLicencia,
            marca = guia.marca,
            modelo = guia.modelo,
            apodo = guia.apodo,
            tipoArma = guia.tipoArma,
            calibre1 = guia.calibre1,
            calibre2 = guia.calibre2 ?: "",
            showCalibre2 = !guia.calibre2.isNullOrBlank(),
            numGuia = guia.numGuia,
            numArma = guia.numArma,
            cupo = guia.cupo.toString(),
            gastado = guia.gastado.toString(),
            existingImageUrl = guia.fotoUrl,
            storagePath = guia.storagePath,
            isEditing = true
        )
        
        /**
         * Crea estado inicial vacío (creación)
         */
        fun empty(tipoLicencia: Int = 0): GuiaFormState = GuiaFormState(
            tipoLicencia = tipoLicencia,
            isEditing = false
        )
    }
}

/**
 * Estados de UI del formulario
 */
sealed class GuiaFormUiState {
    data object Idle : GuiaFormUiState()
    data object Loading : GuiaFormUiState()
    data class Uploading(val progress: Float) : GuiaFormUiState()
    data class Success(val message: String) : GuiaFormUiState()
    data class Error(val message: String) : GuiaFormUiState()
}

/**
 * Eventos del formulario (acciones del usuario)
 */
sealed class GuiaFormEvent {
    // Cambios de campos
    data class MarcaChanged(val value: String) : GuiaFormEvent()
    data class ModeloChanged(val value: String) : GuiaFormEvent()
    data class ApodoChanged(val value: String) : GuiaFormEvent()
    data class TipoArmaChanged(val value: Int) : GuiaFormEvent()
    data class Calibre1Changed(val value: String) : GuiaFormEvent()
    data class Calibre2Changed(val value: String) : GuiaFormEvent()
    data class ShowCalibre2Changed(val value: Boolean) : GuiaFormEvent()
    data class NumGuiaChanged(val value: String) : GuiaFormEvent()
    data class NumArmaChanged(val value: String) : GuiaFormEvent()
    data class CupoChanged(val value: String) : GuiaFormEvent()
    data class CustomCupoChanged(val value: Boolean) : GuiaFormEvent()
    data class GastadoChanged(val value: String) : GuiaFormEvent()
    
    // Imagen
    data class ImageSelected(val uri: Uri) : GuiaFormEvent()
    data object ImageRemoved : GuiaFormEvent()
    
    // Acciones
    data object Save : GuiaFormEvent()
    data object ResetErrors : GuiaFormEvent()
}
