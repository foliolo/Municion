package al.ahgitdevelopment.municion.ui.forms.licencia

import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.data.repository.ImageRepository
import al.ahgitdevelopment.municion.data.repository.LicenciaRepository
import al.ahgitdevelopment.municion.ui.components.imagepicker.ImageState
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
 * @since v3.2.4 (ImageState simplification)
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
            Log.d(TAG, "Initializing with licencia: id=${licencia.id}, fotoUrl=${licencia.fotoUrl}, storagePath=${licencia.storagePath}")
            _formState.value = LicenciaFormState.fromLicencia(licencia)
            Log.d(TAG, "Initialized for editing: id=${licencia.id}, imageState=${_formState.value.imageState}, currentImageUrl=${_formState.value.currentImageUrl}")
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

            // Imagen - usando el nuevo ImageState
            is LicenciaFormEvent.ImageSelected -> _formState.update {
                it.selectImage(event.uri)
            }
            LicenciaFormEvent.ImageRemoved -> _formState.update {
                it.removeImage()
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

                // Procesar imagen y obtener URLs finales
                val (finalFotoUrl, finalStoragePath) = processImageState(state, userId)

                _uiState.value = LicenciaFormUiState.Uploading(0.9f)

                // Crear licencia para guardar
                val licenciaToSave = state.toLicencia(
                    tiposLicencia = emptyList(),
                    fotoUrl = finalFotoUrl,
                    storagePath = finalStoragePath
                )

                Log.d(TAG, "Saving licencia with fotoUrl=$finalFotoUrl, storagePath=$finalStoragePath")

                _uiState.value = LicenciaFormUiState.Uploading(1f)

                // Guardar o actualizar (pasar userId para sincronizar con Firebase)
                if (state.isEditing) {
                    licenciaRepository.updateLicencia(licenciaToSave, userId)
                    _uiState.value = LicenciaFormUiState.Success("Licencia actualizada")
                } else {
                    licenciaRepository.saveLicencia(licenciaToSave, userId)
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
     * Procesa el ImageState y retorna (fotoUrl, storagePath) finales.
     *
     * - NoImage: retorna (null, null)
     * - Existing: retorna la URL y path existentes
     * - New: sube la imagen, elimina la anterior si existía, retorna nuevas URLs
     */
    private suspend fun processImageState(
        state: LicenciaFormState,
        userId: String
    ): Pair<String?, String?> {
        return when (val imageState = state.imageState) {
            is ImageState.NoImage -> {
                Pair(null, null)
            }

            is ImageState.Existing -> {
                // Imagen existente, no hay cambios - preservar URLs
                Log.d(TAG, "Preserving existing image: ${imageState.url}")
                Pair(imageState.url, imageState.storagePath)
            }

            is ImageState.New -> {
                // Nueva imagen - subir a Firebase
                _uiState.value = LicenciaFormUiState.Uploading(0.1f)

                // Eliminar imagen anterior si existe
                val previousStoragePath = imageState.existingStoragePath
                if (previousStoragePath != null) {
                    Log.d(TAG, "Deleting previous image: $previousStoragePath")
                    imageRepository.deleteLicenciaImage(previousStoragePath)
                }

                _uiState.value = LicenciaFormUiState.Uploading(0.3f)

                // Generar ID para la imagen
                val imageId = if (state.isEditing) {
                    state.licenciaId.toString()
                } else {
                    System.currentTimeMillis().toString()
                }

                // Subir nueva imagen
                val uploadResult = imageRepository.uploadLicenciaImage(
                    uri = imageState.uri,
                    userId = userId,
                    licenciaId = imageId
                )

                _uiState.value = LicenciaFormUiState.Uploading(0.7f)

                uploadResult.fold(
                    onSuccess = { result ->
                        Log.d(TAG, "Image uploaded successfully: ${result.downloadUrl}")
                        Pair(result.downloadUrl, result.storagePath)
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to upload image", error)
                        _effects.send(LicenciaFormEffect.ShowError("Error al subir imagen: ${error.message}"))
                        // En caso de error, preservar imagen anterior si existía
                        when (val prev = imageState.previousState) {
                            is ImageState.Existing -> Pair(prev.url, prev.storagePath)
                            else -> Pair(null, null)
                        }
                    }
                )
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
