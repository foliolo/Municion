package al.ahgitdevelopment.municion.ui.forms.licencia

import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.ui.components.imagepicker.ImageState
import android.net.Uri

/**
 * Estado del formulario de Licencia.
 * 
 * Encapsula todos los campos del formulario y sus errores de validación.
 * Inmutable - cada cambio genera una nueva instancia.
 *
 * @since v3.2.2 (Form Architecture Refactor)
 * @since v3.2.4 (ImageState simplification)
 */
data class LicenciaFormState(
    // Campos del formulario
    val tipoLicencia: Int = 0,
    val numLicencia: String = "",
    val fechaExpedicion: String = "",
    val fechaCaducidad: String = "",
    val numAbonado: String = "",
    val numSeguro: String = "",
    val autonomia: Int = 0,
    val tipoPermisoConducir: Int = 0,
    val edad: String = "",
    val escala: Int = 0,
    val categoria: Int = 0,
    
    // Estado de imagen unificado
    val imageState: ImageState = ImageState.NoImage,
    
    // Metadata
    val licenciaId: Int = 0,
    val isEditing: Boolean = false,
    
    // Errores de validación
    val numLicenciaError: String? = null,
    val fechaExpedicionError: String? = null,
    val numSeguroError: String? = null,
    val edadError: String? = null
) {
    /**
     * URL de imagen a mostrar (delegado a ImageState)
     */
    val currentImageUrl: String?
        get() = imageState.displayUrl
    
    /**
     * Tiene nueva imagen seleccionada (pendiente de subir)
     */
    val hasNewImage: Boolean
        get() = imageState.hasNewImage
    
    /**
     * Valida si el formulario tiene errores
     */
    val hasErrors: Boolean
        get() = listOf(
            numLicenciaError, fechaExpedicionError, numSeguroError, edadError
        ).any { it != null }
    
    // Visibilidad de campos según tipo de licencia
    val showEscala: Boolean get() = tipoLicencia == 0
    val showFechaCaducidad: Boolean get() = tipoLicencia != 0
    val showNumAbonado: Boolean get() = tipoLicencia in 9..11
    val showNumSeguro: Boolean get() = tipoLicencia in 9..10
    val showAutonomia: Boolean get() = tipoLicencia in 9..11
    val showPermisoConducir: Boolean get() = tipoLicencia == 12
    val showEdad: Boolean get() = tipoLicencia == 12
    val showCategoria: Boolean get() = tipoLicencia == 11
    
    /**
     * Crea una instancia de Licencia desde el estado actual.
     * 
     * NOTA: fotoUrl y storagePath se establecen desde ImageState
     * después del proceso de guardado (con imagen ya subida si aplica).
     */
    fun toLicencia(tiposLicencia: List<String>, fotoUrl: String? = null, storagePath: String? = null): Licencia = Licencia(
        id = licenciaId,
        tipo = tipoLicencia,
        nombre = tiposLicencia.getOrNull(tipoLicencia),
        tipoPermisoConduccion = if (showPermisoConducir) tipoPermisoConducir else -1,
        edad = edad.toIntOrNull() ?: 30,
        fechaExpedicion = fechaExpedicion,
        fechaCaducidad = fechaCaducidad.ifBlank { "31/12/3000" },
        numLicencia = numLicencia,
        numAbonado = if (showNumAbonado) numAbonado.toIntOrNull() ?: -1 else -1,
        numSeguro = if (showNumSeguro) numSeguro else null,
        autonomia = if (showAutonomia) autonomia else -1,
        escala = if (showEscala) escala else -1,
        categoria = if (showCategoria) categoria else -1,
        fotoUrl = fotoUrl,
        storagePath = storagePath
    )
    
    /**
     * Selecciona una nueva imagen
     */
    fun selectImage(uri: Uri): LicenciaFormState = copy(
        imageState = ImageState.New(uri = uri, previousState = imageState)
    )
    
    /**
     * Elimina la imagen actual
     */
    fun removeImage(): LicenciaFormState = copy(imageState = ImageState.NoImage)
    
    /**
     * Actualiza con imagen subida exitosamente
     */
    fun withUploadedImage(url: String, storagePath: String): LicenciaFormState = copy(
        imageState = ImageState.Existing(url = url, storagePath = storagePath)
    )
    
    companion object {
        /**
         * Crea estado inicial desde una Licencia existente (edición)
         */
        fun fromLicencia(licencia: Licencia): LicenciaFormState = LicenciaFormState(
            licenciaId = licencia.id,
            tipoLicencia = licencia.tipo,
            numLicencia = licencia.numLicencia,
            fechaExpedicion = licencia.fechaExpedicion,
            fechaCaducidad = licencia.fechaCaducidad,
            numAbonado = licencia.numAbonado.takeIf { it >= 0 }?.toString() ?: "",
            numSeguro = licencia.numSeguro ?: "",
            autonomia = licencia.autonomia.takeIf { it >= 0 } ?: 0,
            tipoPermisoConducir = licencia.tipoPermisoConduccion.takeIf { it >= 0 } ?: 0,
            edad = licencia.edad.toString(),
            escala = licencia.escala.takeIf { it >= 0 } ?: 0,
            categoria = licencia.categoria.takeIf { it >= 0 } ?: 0,
            imageState = ImageState.fromEntity(licencia.fotoUrl, licencia.storagePath),
            isEditing = true
        )
        
        /**
         * Crea estado inicial vacío (creación)
         */
        fun empty(): LicenciaFormState = LicenciaFormState(
            isEditing = false
        )
    }
}

/**
 * Estados de UI del formulario
 */
sealed class LicenciaFormUiState {
    data object Idle : LicenciaFormUiState()
    data object Loading : LicenciaFormUiState()
    data class Uploading(val progress: Float) : LicenciaFormUiState()
    data class Success(val message: String) : LicenciaFormUiState()
    data class Error(val message: String) : LicenciaFormUiState()
}

/**
 * Eventos del formulario (acciones del usuario)
 */
sealed class LicenciaFormEvent {
    // Cambios de campos
    data class TipoLicenciaChanged(val value: Int) : LicenciaFormEvent()
    data class NumLicenciaChanged(val value: String) : LicenciaFormEvent()
    data class FechaExpedicionChanged(val value: String) : LicenciaFormEvent()
    data class FechaCaducidadChanged(val value: String) : LicenciaFormEvent()
    data class NumAbonadoChanged(val value: String) : LicenciaFormEvent()
    data class NumSeguroChanged(val value: String) : LicenciaFormEvent()
    data class AutonomiaChanged(val value: Int) : LicenciaFormEvent()
    data class TipoPermisoConducirChanged(val value: Int) : LicenciaFormEvent()
    data class EdadChanged(val value: String) : LicenciaFormEvent()
    data class EscalaChanged(val value: Int) : LicenciaFormEvent()
    data class CategoriaChanged(val value: Int) : LicenciaFormEvent()
    
    // Imagen
    data class ImageSelected(val uri: Uri) : LicenciaFormEvent()
    data object ImageRemoved : LicenciaFormEvent()
    
    // Acciones
    data object Save : LicenciaFormEvent()
    data object ResetErrors : LicenciaFormEvent()
}

/**
 * Efectos de navegación/UI (one-shot events)
 */
sealed class LicenciaFormEffect {
    data object NavigateBack : LicenciaFormEffect()
    data class ShowSnackbar(val message: String) : LicenciaFormEffect()
    data class ShowError(val message: String) : LicenciaFormEffect()
}
