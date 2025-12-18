package al.ahgitdevelopment.municion.ui.forms.licencia

import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.data.repository.ImageRepository
import al.ahgitdevelopment.municion.data.repository.LicenciaRepository
import al.ahgitdevelopment.municion.ui.forms.compra.CompraFormUiState
import android.util.Log
import androidx.lifecycle.SavedStateHandle
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel para el formulario de Licencia.
 *
 * Gestiona el estado del formulario, validación, subida de imágenes y guardado.
 * Sigue el patrón MVI (Model-View-Intent) con eventos y efectos.
 *
 * @since v3.2.2 (Form Architecture Refactor)
 */
@HiltViewModel
class LicenciaFormViewModel @Inject constructor(
    private val licenciaRepository: LicenciaRepository,
    private val imageRepository: ImageRepository,
    private val firebaseAuth: FirebaseAuth,
) : ViewModel() {

    companion object {
        private const val TAG = "LicenciaFormViewModel"
    }

    // Estado del formulario
    private val _formState = MutableStateFlow(LicenciaFormState())
    val formState: StateFlow<LicenciaFormState> = _formState.asStateFlow()

    // Estado de UI (loading, error, success)
    private val _uiState = MutableStateFlow<LicenciaFormUiState>(LicenciaFormUiState.Idle)
    val uiState: StateFlow<LicenciaFormUiState> = _uiState.asStateFlow()

    // Efectos one-shot (navegación, snackbar)
    private val _effects = Channel<LicenciaFormEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    /**
     * Inicializa el ViewModel con una licencia existente (edición) o vacía (creación)
     */
    fun initialize(licencia: Licencia?) {
        if (licencia != null) {
            _formState.value = LicenciaFormState.fromLicencia(licencia)
            Log.d(TAG, "Initialized for editing: id=${licencia.id}")
        } else {
            _formState.value = LicenciaFormState.empty()
            Log.d(TAG, "Initialized for creating new licencia")
        }
    }

    /**
     * Procesa eventos del formulario
     */
    fun onEvent(event: LicenciaFormEvent) {
        when (event) {
            // Cambios de campos
            is LicenciaFormEvent.TipoLicenciaChanged -> {
                _formState.update { it.copy(tipoLicencia = event.value) }
                // Recalcular fecha de caducidad
                recalculateFechaCaducidad()
            }
            is LicenciaFormEvent.NumLicenciaChanged -> _formState.update {
                it.copy(numLicencia = event.value, numLicenciaError = null)
            }
            is LicenciaFormEvent.FechaExpedicionChanged -> {
                _formState.update { it.copy(fechaExpedicion = event.value, fechaExpedicionError = null) }
                recalculateFechaCaducidad()
            }
            is LicenciaFormEvent.FechaCaducidadChanged -> _formState.update {
                it.copy(fechaCaducidad = event.value)
            }
            is LicenciaFormEvent.NumAbonadoChanged -> _formState.update {
                it.copy(numAbonado = event.value)
            }
            is LicenciaFormEvent.NumSeguroChanged -> _formState.update {
                it.copy(numSeguro = event.value, numSeguroError = null)
            }
            is LicenciaFormEvent.AutonomiaChanged -> _formState.update {
                it.copy(autonomia = event.value)
            }
            is LicenciaFormEvent.TipoPermisoConducirChanged -> {
                _formState.update { it.copy(tipoPermisoConducir = event.value) }
                recalculateFechaCaducidad()
            }
            is LicenciaFormEvent.EdadChanged -> {
                _formState.update { it.copy(edad = event.value, edadError = null) }
                recalculateFechaCaducidad()
            }
            is LicenciaFormEvent.EscalaChanged -> _formState.update {
                it.copy(escala = event.value)
            }
            is LicenciaFormEvent.CategoriaChanged -> _formState.update {
                it.copy(categoria = event.value)
            }

            // Imagen
            is LicenciaFormEvent.ImageSelected -> _formState.update {
                it.copy(selectedImageUri = event.uri)
            }
            LicenciaFormEvent.ImageRemoved -> _formState.update {
                it.copy(selectedImageUri = null, existingImageUrl = null)
            }

            // Acciones
            LicenciaFormEvent.Save -> validateAndSave()
            LicenciaFormEvent.ResetErrors -> _formState.update {
                it.copy(
                    numLicenciaError = null,
                    fechaExpedicionError = null,
                    numSeguroError = null,
                    edadError = null
                )
            }
        }
    }

    /**
     * Recalcula la fecha de caducidad según el tipo de licencia
     */
    private fun recalculateFechaCaducidad() {
        val state = _formState.value
        val nuevaFecha = calculateFechaCaducidad(
            fechaExpedicion = state.fechaExpedicion,
            tipoLicencia = state.tipoLicencia,
            tipoPermisoConducir = state.tipoPermisoConducir,
            edadStr = state.edad
        )
        _formState.update { it.copy(fechaCaducidad = nuevaFecha) }
    }

    /**
     * Valida el formulario y guarda si es válido
     */
    private fun validateAndSave() {
        val state = _formState.value
        var hasErrors = false

        // Validar campos obligatorios
        if (state.numLicencia.isBlank()) {
            _formState.update { it.copy(numLicenciaError = "Introduce el número de licencia") }
            hasErrors = true
        }
        if (state.fechaExpedicion.isBlank()) {
            _formState.update { it.copy(fechaExpedicionError = "Introduce la fecha de expedición") }
            hasErrors = true
        }
        if (state.showNumSeguro && state.numSeguro.isBlank()) {
            _formState.update { it.copy(numSeguroError = "Introduce el número de póliza") }
            hasErrors = true
        }
        if (state.showEdad && state.edad.isBlank()) {
            _formState.update { it.copy(edadError = "Introduce la edad") }
            hasErrors = true
        }

        if (hasErrors) return

        viewModelScope.launch {
            _uiState.value = LicenciaFormUiState.Uploading(0f)
            try {
                val userId = firebaseAuth.currentUser?.uid
                    ?: throw IllegalStateException("User not authenticated")

                var finalFotoUrl = state.existingImageUrl
                var finalStoragePath = state.storagePath

                // Subir nueva imagen si se seleccionó
                if (state.hasNewImage && state.selectedImageUri != null) {
                    // Paso 1: Iniciando subida
                    // Eliminar imagen anterior si existe (en edición)
                    if (state.isEditing && state.storagePath != null) {
                        imageRepository.deleteLicenciaImage(state.storagePath)
                    }

                    _uiState.value = LicenciaFormUiState.Uploading(0.3f)

                    // Paso 2: Subiendo imagen
                    // Generar ID para la imagen
                    val imageId = if (state.isEditing) {
                        state.licenciaId.toString()
                    } else {
                        System.currentTimeMillis().toString()
                    }

                    val uploadResult = imageRepository.uploadLicenciaImage(
                        uri = state.selectedImageUri,
                        userId = userId,
                        licenciaId = imageId
                    )

                    _uiState.value = LicenciaFormUiState.Uploading(0.7f)

                    uploadResult.fold(
                        onSuccess = { result ->
                            finalFotoUrl = result.downloadUrl
                            finalStoragePath = result.storagePath
                            Log.d(TAG, "Image uploaded: $finalFotoUrl")
                        },
                        onFailure = { error ->
                            Log.e(TAG, "Failed to upload image", error)
                            _effects.send(LicenciaFormEffect.ShowError("Error al subir imagen: ${error.message}"))
                        }
                    )

                    // Paso 3: Guardando datos
                    _uiState.value = LicenciaFormUiState.Uploading(0.9f)
                } else {
                    _uiState.value = LicenciaFormUiState.Loading
                }

                // Actualizar estado con URLs de imagen
                _formState.update {
                    it.copy(
                        existingImageUrl = finalFotoUrl,
                        storagePath = finalStoragePath
                    )
                }

                // Crear licencia para guardar
                val licenciaToSave = _formState.value.toLicencia(emptyList()).copy(
                    fotoUrl = finalFotoUrl,
                    storagePath = finalStoragePath
                )

                _uiState.value = LicenciaFormUiState.Uploading(1f)

                // Guardar o actualizar
                if (state.isEditing) {
                    licenciaRepository.updateLicencia(licenciaToSave)
                    _uiState.value = LicenciaFormUiState.Success("Licencia actualizada")
                } else {
                    licenciaRepository.saveLicencia(licenciaToSave)
                    _uiState.value = LicenciaFormUiState.Success("Licencia guardada")
                }

                _effects.send(LicenciaFormEffect.NavigateBack)

            } catch (e: Exception) {
                Log.e(TAG, "Error saving licencia", e)
                _uiState.value = LicenciaFormUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Calcula la fecha de caducidad según el tipo de licencia
     */
    private fun calculateFechaCaducidad(
        fechaExpedicion: String,
        tipoLicencia: Int,
        tipoPermisoConducir: Int,
        edadStr: String
    ): String {
        if (fechaExpedicion.isBlank()) return ""

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()

        try {
            calendar.time = dateFormat.parse(fechaExpedicion) ?: return ""
        } catch (e: Exception) {
            return ""
        }

        val edad = edadStr.toIntOrNull() ?: 30

        when (tipoLicencia) {
            0 -> { // Licencia A - No caduca
                calendar.set(3000, Calendar.DECEMBER, 31)
            }
            1, 5 -> { // Licencia B, F - 5 años
                calendar.add(Calendar.YEAR, 5)
            }
            2, 3, 4, 6, 7, 8 -> { // Licencia C, D, E, AE, AER, Libre Coleccionista - 5 años
                calendar.add(Calendar.YEAR, 5)
            }
            9, 10, 11 -> { // Autonómica Caza, Pesca, Federativa - Fin de año
                calendar.set(calendar.get(Calendar.YEAR), Calendar.DECEMBER, 31)
            }
            12 -> { // Permiso de conducir
                if (edad < 65) {
                    when (tipoPermisoConducir) {
                        in 0..4 -> calendar.add(Calendar.YEAR, 10)
                        else -> calendar.add(Calendar.YEAR, 5)
                    }
                } else {
                    when (tipoPermisoConducir) {
                        in 0..4 -> calendar.add(Calendar.YEAR, 5)
                        else -> calendar.add(Calendar.YEAR, 3)
                    }
                }
            }
        }

        return dateFormat.format(calendar.time)
    }

    fun resetUiState() {
        _uiState.value = LicenciaFormUiState.Idle
    }
}
