package al.ahgitdevelopment.municion.ui.forms.compra

import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import android.net.Uri

/**
 * Estado del formulario de Compra.
 * 
 * Encapsula todos los campos del formulario y sus errores de validación.
 * Inmutable - cada cambio genera una nueva instancia.
 *
 * @since v3.2.3 (Form Architecture Refactor for Compras)
 */
data class CompraFormState(
    // Campos del formulario
    val calibre1: String = "",
    val calibre2: String = "",
    val showCalibre2: Boolean = false,
    val marca: String = "",
    val tipo: String = "",
    val peso: String = "",
    val unidades: String = "",
    val precio: String = "",
    val fecha: String = "",
    val tienda: String = "",
    val valoracion: Float = 0f,
    
    // Estado de imagen
    val selectedImageUri: Uri? = null,
    val existingImageUrl: String? = null,
    val storagePath: String? = null,
    
    // Metadata
    val compraId: Int = 0,
    val guiaId: Int = 0,
    val cupoDisponible: Int = 0,
    val cupoTotal: Int = 0,
    val isEditing: Boolean = false,
    
    // Errores de validación
    val calibre1Error: String? = null,
    val marcaError: String? = null,
    val tipoError: String? = null,
    val pesoError: String? = null,
    val unidadesError: String? = null,
    val precioError: String? = null,
    val fechaError: String? = null,
    val tiendaError: String? = null
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
            calibre1Error, marcaError, tipoError, pesoError,
            unidadesError, precioError, fechaError, tiendaError
        ).any { it != null }
    
    /**
     * Verifica si las unidades exceden el cupo disponible
     */
    val excedeCupo: Boolean
        get() {
            val unidadesInt = unidades.toIntOrNull() ?: 0
            return unidadesInt > cupoDisponible
        }
    
    /**
     * Crea una instancia de Compra desde el estado actual
     */
    fun toCompra(): Compra = Compra(
        id = compraId,
        idPosGuia = guiaId,
        calibre1 = calibre1,
        calibre2 = if (showCalibre2) calibre2 else null,
        marca = marca,
        tipo = tipo,
        peso = peso.toIntOrNull() ?: 0,
        unidades = unidades.toIntOrNull() ?: 0,
        precio = precio.replace(",", ".").toDoubleOrNull() ?: 0.0,
        fecha = fecha,
        tienda = tienda,
        valoracion = valoracion,
        fotoUrl = existingImageUrl,
        storagePath = storagePath
    )
    
    companion object {
        /**
         * Crea estado inicial desde una Compra existente (edición)
         */
        fun fromCompra(compra: Compra, guia: Guia): CompraFormState = CompraFormState(
            compraId = compra.id,
            guiaId = guia.id,
            calibre1 = compra.calibre1,
            calibre2 = compra.calibre2 ?: "",
            showCalibre2 = !compra.calibre2.isNullOrBlank(),
            marca = compra.marca,
            tipo = compra.tipo,
            peso = compra.peso.toString(),
            unidades = compra.unidades.toString(),
            precio = compra.precio.toString(),
            fecha = compra.fecha,
            tienda = compra.tienda ?: "",
            valoracion = compra.valoracion,
            existingImageUrl = compra.fotoUrl,
            storagePath = compra.storagePath,
            cupoDisponible = guia.disponible() + compra.unidades, // Al editar, sumar las unidades actuales
            cupoTotal = guia.cupo,
            isEditing = true
        )
        
        /**
         * Crea estado inicial vacío (creación) con datos de la guía
         */
        fun fromGuia(guia: Guia): CompraFormState = CompraFormState(
            guiaId = guia.id,
            calibre1 = guia.calibre1,
            calibre2 = guia.calibre2 ?: "",
            showCalibre2 = !guia.calibre2.isNullOrBlank(),
            cupoDisponible = guia.disponible(),
            cupoTotal = guia.cupo,
            isEditing = false
        )
    }
}

/**
 * Estados de UI del formulario
 */
sealed class CompraFormUiState {
    data object Idle : CompraFormUiState()
    data object Loading : CompraFormUiState()
    data class Uploading(val progress: Float) : CompraFormUiState()
    data class Success(val message: String) : CompraFormUiState()
    data class Error(val message: String) : CompraFormUiState()
}

/**
 * Eventos del formulario (acciones del usuario)
 */
sealed class CompraFormEvent {
    // Cambios de campos
    data class Calibre1Changed(val value: String) : CompraFormEvent()
    data class Calibre2Changed(val value: String) : CompraFormEvent()
    data class ShowCalibre2Changed(val value: Boolean) : CompraFormEvent()
    data class MarcaChanged(val value: String) : CompraFormEvent()
    data class TipoChanged(val value: String) : CompraFormEvent()
    data class PesoChanged(val value: String) : CompraFormEvent()
    data class UnidadesChanged(val value: String) : CompraFormEvent()
    data class PrecioChanged(val value: String) : CompraFormEvent()
    data class FechaChanged(val value: String) : CompraFormEvent()
    data class TiendaChanged(val value: String) : CompraFormEvent()
    data class ValoracionChanged(val value: Float) : CompraFormEvent()
    
    // Imagen
    data class ImageSelected(val uri: Uri) : CompraFormEvent()
    data object ImageRemoved : CompraFormEvent()
    
    // Acciones
    data object Save : CompraFormEvent()
    data object ResetErrors : CompraFormEvent()
}

/**
 * Efectos de navegación/UI (one-shot events)
 */
sealed class CompraFormEffect {
    data object NavigateBack : CompraFormEffect()
    data class ShowSnackbar(val message: String) : CompraFormEffect()
    data class ShowError(val message: String) : CompraFormEffect()
}
