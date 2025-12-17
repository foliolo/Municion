package al.ahgitdevelopment.municion.ui.forms.compra

import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.data.repository.CompraRepository
import al.ahgitdevelopment.municion.data.repository.ImageRepository
import al.ahgitdevelopment.municion.domain.usecase.CreateCompraUseCase
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para el formulario de Compra.
 *
 * Gestiona el estado del formulario, validación, subida de imágenes y guardado.
 * Sigue el patrón MVI (Model-View-Intent) con eventos y efectos.
 *
 * @since v3.2.3 (Form Architecture Refactor for Compras)
 */
@HiltViewModel
class CompraFormViewModel @Inject constructor(
    private val compraRepository: CompraRepository,
    private val createCompraUseCase: CreateCompraUseCase,
    private val imageRepository: ImageRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    companion object {
        private const val TAG = "CompraFormViewModel"
    }

    // Estado del formulario
    private val _formState = MutableStateFlow(CompraFormState())
    val formState: StateFlow<CompraFormState> = _formState.asStateFlow()

    // Estado de UI (loading, error, success)
    private val _uiState = MutableStateFlow<CompraFormUiState>(CompraFormUiState.Idle)
    val uiState: StateFlow<CompraFormUiState> = _uiState.asStateFlow()

    // Efectos one-shot (navegación, snackbar)
    private val _effects = Channel<CompraFormEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    /**
     * Inicializa el ViewModel con una compra existente (edición) o desde guía (creación)
     */
    fun initialize(compra: Compra?, guia: Guia) {
        if (compra != null) {
            _formState.value = CompraFormState.fromCompra(compra, guia)
            Log.d(TAG, "Initialized for editing: id=${compra.id}")
        } else {
            _formState.value = CompraFormState.fromGuia(guia)
            Log.d(TAG, "Initialized for creating new compra for guia=${guia.id}")
        }
    }

    /**
     * Procesa eventos del formulario
     */
    fun onEvent(event: CompraFormEvent) {
        when (event) {
            // Cambios de campos
            is CompraFormEvent.Calibre1Changed -> _formState.update {
                it.copy(calibre1 = event.value, calibre1Error = null)
            }
            is CompraFormEvent.Calibre2Changed -> _formState.update {
                it.copy(calibre2 = event.value)
            }
            is CompraFormEvent.ShowCalibre2Changed -> _formState.update {
                it.copy(showCalibre2 = event.value, calibre2 = if (!event.value) "" else it.calibre2)
            }
            is CompraFormEvent.MarcaChanged -> _formState.update {
                it.copy(marca = event.value, marcaError = null)
            }
            is CompraFormEvent.TipoChanged -> _formState.update {
                it.copy(tipo = event.value, tipoError = null)
            }
            is CompraFormEvent.PesoChanged -> _formState.update {
                it.copy(peso = event.value, pesoError = null)
            }
            is CompraFormEvent.UnidadesChanged -> _formState.update {
                it.copy(unidades = event.value, unidadesError = null)
            }
            is CompraFormEvent.PrecioChanged -> _formState.update {
                it.copy(precio = event.value, precioError = null)
            }
            is CompraFormEvent.FechaChanged -> _formState.update {
                it.copy(fecha = event.value, fechaError = null)
            }
            is CompraFormEvent.TiendaChanged -> _formState.update {
                it.copy(tienda = event.value, tiendaError = null)
            }
            is CompraFormEvent.ValoracionChanged -> _formState.update {
                it.copy(valoracion = event.value)
            }

            // Imagen
            is CompraFormEvent.ImageSelected -> _formState.update {
                it.copy(selectedImageUri = event.uri)
            }
            CompraFormEvent.ImageRemoved -> _formState.update {
                it.copy(selectedImageUri = null, existingImageUrl = null)
            }

            // Acciones
            CompraFormEvent.Save -> validateAndSave()
            CompraFormEvent.ResetErrors -> _formState.update {
                it.copy(
                    calibre1Error = null,
                    marcaError = null,
                    tipoError = null,
                    pesoError = null,
                    unidadesError = null,
                    precioError = null,
                    fechaError = null,
                    tiendaError = null
                )
            }
        }
    }

    /**
     * Valida el formulario y guarda si es válido
     */
    private fun validateAndSave() {
        val state = _formState.value
        var hasErrors = false

        // Validar campos obligatorios
        if (state.calibre1.isBlank()) {
            _formState.update { it.copy(calibre1Error = "Introduce el calibre") }
            hasErrors = true
        }
        if (state.marca.isBlank()) {
            _formState.update { it.copy(marcaError = "Introduce la marca") }
            hasErrors = true
        }
        if (state.tipo.isBlank()) {
            _formState.update { it.copy(tipoError = "Introduce el tipo de munición") }
            hasErrors = true
        }
        if (state.peso.isBlank() || state.peso.toIntOrNull() == null || state.peso.toInt() <= 0) {
            _formState.update { it.copy(pesoError = "Introduce un peso válido") }
            hasErrors = true
        }
        if (state.unidades.isBlank() || state.unidades.toIntOrNull() == null || state.unidades.toInt() <= 0) {
            _formState.update { it.copy(unidadesError = "Introduce unidades válidas") }
            hasErrors = true
        } else if (state.excedeCupo) {
            _formState.update { it.copy(unidadesError = "Excede el cupo disponible (${state.cupoDisponible})") }
            hasErrors = true
        }
        if (state.precio.isBlank()) {
            _formState.update { it.copy(precioError = "Introduce el precio") }
            hasErrors = true
        }
        if (state.fecha.isBlank()) {
            _formState.update { it.copy(fechaError = "Introduce la fecha") }
            hasErrors = true
        }
        if (state.tienda.isBlank()) {
            _formState.update { it.copy(tiendaError = "Selecciona el lugar de compra") }
            hasErrors = true
        }

        if (hasErrors) return

        viewModelScope.launch {
            try {
                _uiState.value = CompraFormUiState.Loading

                val userId = firebaseAuth.currentUser?.uid
                    ?: throw IllegalStateException("User not authenticated")

                var finalFotoUrl = state.existingImageUrl
                var finalStoragePath = state.storagePath

                // Subir nueva imagen si se seleccionó
                if (state.hasNewImage && state.selectedImageUri != null) {
                    _uiState.value = CompraFormUiState.Uploading(0f)

                    // Generar ID para la imagen
                    val imageId = if (state.isEditing) {
                        state.compraId.toString()
                    } else {
                        System.currentTimeMillis().toString()
                    }

                    val uploadResult = imageRepository.uploadCompraImage(
                        uri = state.selectedImageUri,
                        userId = userId,
                        compraId = imageId
                    )

                    uploadResult.fold(
                        onSuccess = { result ->
                            finalFotoUrl = result.downloadUrl
                            finalStoragePath = result.storagePath
                            Log.d(TAG, "Image uploaded: $finalFotoUrl")
                        },
                        onFailure = { error ->
                            Log.e(TAG, "Failed to upload image", error)
                            _effects.send(CompraFormEffect.ShowError("Error al subir imagen: ${error.message}"))
                        }
                    )
                }

                // Actualizar estado con URLs de imagen
                _formState.update {
                    it.copy(
                        existingImageUrl = finalFotoUrl,
                        storagePath = finalStoragePath
                    )
                }

                // Crear compra para guardar
                val compraToSave = _formState.value.toCompra().copy(
                    fotoUrl = finalFotoUrl,
                    storagePath = finalStoragePath
                )

                // Guardar o actualizar
                if (state.isEditing) {
                    compraRepository.updateCompra(compraToSave, userId)
                    _uiState.value = CompraFormUiState.Success("Compra actualizada")
                } else {
                    createCompraUseCase(compraToSave, userId)
                    _uiState.value = CompraFormUiState.Success("Compra guardada")
                }

                _effects.send(CompraFormEffect.NavigateBack)

            } catch (e: Exception) {
                Log.e(TAG, "Error saving compra", e)
                _uiState.value = CompraFormUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = CompraFormUiState.Idle
    }
}
