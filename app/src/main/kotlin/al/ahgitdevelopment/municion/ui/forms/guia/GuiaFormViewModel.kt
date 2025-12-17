package al.ahgitdevelopment.municion.ui.forms.guia

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.data.repository.GuiaRepository
import al.ahgitdevelopment.municion.data.repository.ImageRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel dedicado al formulario de Guía.
 * 
 * Implementa patrón MVI (Model-View-Intent) con:
 * - Estado centralizado (GuiaFormState)
 * - Eventos unidireccionales (GuiaFormEvent)
 * - Efectos secundarios (navegación, snackbars)
 *
 * @since v3.2.2 (Form Architecture Refactor)
 */
@HiltViewModel
class GuiaFormViewModel @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val imageRepository: ImageRepository,
    private val firebaseAuth: FirebaseAuth,
    private val crashlytics: FirebaseCrashlytics,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    // Estado del formulario
    private val _formState = MutableStateFlow(GuiaFormState())
    val formState: StateFlow<GuiaFormState> = _formState.asStateFlow()
    
    // Estado de UI (loading, error, success)
    private val _uiState = MutableStateFlow<GuiaFormUiState>(GuiaFormUiState.Idle)
    val uiState: StateFlow<GuiaFormUiState> = _uiState.asStateFlow()
    
    // Efectos secundarios (navegación, snackbars)
    private val _effect = MutableSharedFlow<GuiaFormEffect>()
    val effect: SharedFlow<GuiaFormEffect> = _effect.asSharedFlow()
    
    // Strings de error (cargados del contexto)
    private val errorFieldRequired = context.getString(R.string.error_field_required)
    private val errorValidQuota = context.getString(R.string.error_valid_quota)
    
    /**
     * Inicializa el formulario con una Guía existente (edición) o vacío (creación)
     */
    fun initialize(guia: Guia?, tipoLicenciaString: String) {
        val tipoLicenciaInt = getLicenciaTypeFromString(tipoLicenciaString)
        
        _formState.value = if (guia != null) {
            GuiaFormState.fromGuia(guia)
        } else {
            GuiaFormState.empty(tipoLicenciaInt)
        }
    }
    
    /**
     * Procesa eventos del formulario
     */
    fun onEvent(event: GuiaFormEvent) {
        when (event) {
            is GuiaFormEvent.MarcaChanged -> updateField { copy(marca = event.value, marcaError = null) }
            is GuiaFormEvent.ModeloChanged -> updateField { copy(modelo = event.value, modeloError = null) }
            is GuiaFormEvent.ApodoChanged -> updateField { copy(apodo = event.value, apodoError = null) }
            is GuiaFormEvent.TipoArmaChanged -> {
                updateField { 
                    copy(
                        tipoArma = event.value,
                        // Actualizar cupo por defecto si no es personalizado
                        cupo = if (!customCupo && !isEditing) {
                            getDefaultCupoForType(event.value).toString()
                        } else cupo
                    ) 
                }
            }
            is GuiaFormEvent.Calibre1Changed -> updateField { copy(calibre1 = event.value, calibre1Error = null) }
            is GuiaFormEvent.Calibre2Changed -> updateField { copy(calibre2 = event.value) }
            is GuiaFormEvent.ShowCalibre2Changed -> updateField { 
                copy(showCalibre2 = event.value, calibre2 = if (!event.value) "" else calibre2) 
            }
            is GuiaFormEvent.NumGuiaChanged -> updateField { copy(numGuia = event.value, numGuiaError = null) }
            is GuiaFormEvent.NumArmaChanged -> updateField { copy(numArma = event.value, numArmaError = null) }
            is GuiaFormEvent.CupoChanged -> {
                if (event.value.all { it.isDigit() }) {
                    updateField { copy(cupo = event.value, cupoError = null) }
                }
            }
            is GuiaFormEvent.CustomCupoChanged -> updateField { copy(customCupo = event.value) }
            is GuiaFormEvent.GastadoChanged -> {
                if (event.value.all { it.isDigit() }) {
                    updateField { copy(gastado = event.value) }
                }
            }
            is GuiaFormEvent.ImageSelected -> updateField { copy(selectedImageUri = event.uri) }
            is GuiaFormEvent.ImageRemoved -> handleImageRemoved()
            is GuiaFormEvent.Save -> save()
            is GuiaFormEvent.ResetErrors -> resetErrors()
        }
    }
    
    private fun updateField(update: GuiaFormState.() -> GuiaFormState) {
        _formState.update { it.update() }
    }
    
    private fun handleImageRemoved() {
        val currentState = _formState.value
        
        // Si hay imagen existente en Storage, eliminarla
        if (currentState.existingImageUrl != null && currentState.storagePath != null) {
            viewModelScope.launch {
                try {
                    imageRepository.deleteGuiaImage(currentState.storagePath)
                    
                    // Actualizar guía en DB sin imagen
                    val guiaWithoutImage = currentState.toGuia().copy(
                        fotoUrl = null,
                        storagePath = null
                    )
                    val userId = firebaseAuth.currentUser?.uid
                    guiaRepository.updateGuia(guiaWithoutImage, userId)
                    
                    _formState.update { 
                        it.copy(
                            selectedImageUri = null, 
                            existingImageUrl = null, 
                            storagePath = null
                        ) 
                    }
                } catch (e: Exception) {
                    crashlytics.recordException(e)
                    _effect.emit(GuiaFormEffect.ShowError("Error al eliminar imagen"))
                }
            }
        } else {
            // Solo limpiar la selección local
            _formState.update { it.copy(selectedImageUri = null) }
        }
    }
    
    private fun save() {
        val state = _formState.value
        
        // Validar
        val validatedState = validate(state)
        _formState.value = validatedState
        
        if (validatedState.hasErrors) {
            return
        }
        
        viewModelScope.launch {
            try {
                val userId = firebaseAuth.currentUser?.uid
                    ?: throw IllegalStateException("Usuario no autenticado")
                
                val guia = validatedState.toGuia()
                
                if (validatedState.hasNewImage) {
                    saveWithImage(guia, validatedState.selectedImageUri!!, userId)
                } else {
                    saveWithoutImage(guia, userId)
                }
                
            } catch (e: Exception) {
                crashlytics.recordException(e)
                _uiState.value = GuiaFormUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    private suspend fun saveWithImage(guia: Guia, imageUri: Uri, userId: String) {
        _uiState.value = GuiaFormUiState.Uploading(0f)
        
        // Si es edición y había imagen previa, eliminarla
        val state = _formState.value
        if (state.isEditing && state.storagePath != null) {
            imageRepository.deleteGuiaImage(state.storagePath)
        }
        
        _uiState.value = GuiaFormUiState.Uploading(0.3f)
        
        // Subir nueva imagen
        val armaId = if (guia.id > 0) guia.id.toString() else UUID.randomUUID().toString()
        val uploadResult = imageRepository.uploadGuiaImage(
            uri = imageUri,
            userId = userId,
            armaId = armaId
        ).getOrThrow()
        
        _uiState.value = GuiaFormUiState.Uploading(0.7f)
        
        // Guardar guía con URL de imagen
        val guiaWithImage = guia.copy(
            fotoUrl = uploadResult.downloadUrl,
            storagePath = uploadResult.storagePath
        )
        
        if (state.isEditing) {
            guiaRepository.updateGuia(guiaWithImage, userId).getOrThrow()
        } else {
            guiaRepository.saveGuia(guiaWithImage, userId).getOrThrow()
        }
        
        _uiState.value = GuiaFormUiState.Success(
            if (state.isEditing) "Guía actualizada" else "Guía guardada"
        )
        _effect.emit(GuiaFormEffect.NavigateBack)
    }
    
    private suspend fun saveWithoutImage(guia: Guia, userId: String) {
        _uiState.value = GuiaFormUiState.Loading
        
        val state = _formState.value
        if (state.isEditing) {
            guiaRepository.updateGuia(guia, userId).getOrThrow()
        } else {
            guiaRepository.saveGuia(guia, userId).getOrThrow()
        }
        
        _uiState.value = GuiaFormUiState.Success(
            if (state.isEditing) "Guía actualizada" else "Guía guardada"
        )
        _effect.emit(GuiaFormEffect.NavigateBack)
    }
    
    private fun validate(state: GuiaFormState): GuiaFormState {
        return state.copy(
            marcaError = if (state.marca.isBlank()) errorFieldRequired else null,
            modeloError = if (state.modelo.isBlank()) errorFieldRequired else null,
            apodoError = if (state.apodo.isBlank()) errorFieldRequired else null,
            calibre1Error = if (state.calibre1.isBlank()) errorFieldRequired else null,
            numGuiaError = if (state.numGuia.isBlank()) errorFieldRequired else null,
            numArmaError = if (state.numArma.isBlank()) errorFieldRequired else null,
            cupoError = when {
                state.cupo.isBlank() -> errorValidQuota
                state.cupo.toIntOrNull() == null -> errorValidQuota
                state.cupo.toInt() <= 0 -> errorValidQuota
                else -> null
            }
        )
    }
    
    private fun resetErrors() {
        _formState.update { 
            it.copy(
                marcaError = null,
                modeloError = null,
                apodoError = null,
                calibre1Error = null,
                numGuiaError = null,
                numArmaError = null,
                cupoError = null
            )
        }
    }
    
    fun resetUiState() {
        _uiState.value = GuiaFormUiState.Idle
    }
    
    private fun getLicenciaTypeFromString(tipoLicencia: String): Int {
        return try {
            val tipos = context.resources.getStringArray(R.array.tipo_licencias)
            tipos.indexOfFirst { it.equals(tipoLicencia, ignoreCase = true) }.takeIf { it >= 0 } ?: 0
        } catch (e: Exception) {
            0
        }
    }
    
    private fun getDefaultCupoForType(tipoArmaIndex: Int): Int {
        // Los cupos dependen del tipo de arma
        return when (tipoArmaIndex) {
            0 -> 100    // Pistola
            1 -> 100    // Revolver
            2 -> 5000   // Escopeta
            3 -> 1000   // Rifle
            else -> 100
        }
    }
}

/**
 * Efectos secundarios del formulario
 */
sealed class GuiaFormEffect {
    data object NavigateBack : GuiaFormEffect()
    data class ShowSnackbar(val message: String) : GuiaFormEffect()
    data class ShowError(val message: String) : GuiaFormEffect()
}
