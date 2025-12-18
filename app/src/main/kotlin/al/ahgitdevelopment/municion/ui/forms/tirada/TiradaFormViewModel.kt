package al.ahgitdevelopment.municion.ui.forms.tirada

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
import al.ahgitdevelopment.municion.data.repository.TiradaRepository
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
import javax.inject.Inject

/**
 * ViewModel dedicado al formulario de Tirada.
 * 
 * Implementa patrón MVI (Model-View-Intent) con:
 * - Estado centralizado (TiradaFormState)
 * - Eventos unidireccionales (TiradaFormEvent)
 * - Efectos secundarios (navegación, snackbars)
 *
 * @since v3.2.2 (Form Architecture Refactor - MVI Pattern)
 */
@HiltViewModel
class TiradaFormViewModel @Inject constructor(
    private val tiradaRepository: TiradaRepository,
    private val firebaseAuth: FirebaseAuth,
    private val crashlytics: FirebaseCrashlytics,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    // Estado del formulario
    private val _formState = MutableStateFlow(TiradaFormState())
    val formState: StateFlow<TiradaFormState> = _formState.asStateFlow()
    
    // Estado de UI (loading, error, success)
    private val _uiState = MutableStateFlow<TiradaFormUiState>(TiradaFormUiState.Idle)
    val uiState: StateFlow<TiradaFormUiState> = _uiState.asStateFlow()
    
    // Efectos secundarios (navegación, snackbars)
    private val _effect = MutableSharedFlow<TiradaFormEffect>()
    val effect: SharedFlow<TiradaFormEffect> = _effect.asSharedFlow()
    
    // Strings de error (cargados del contexto)
    private val errorFieldRequired = context.getString(R.string.error_field_required)
    
    /**
     * Inicializa el formulario con una Tirada existente (edición) o vacío (creación)
     */
    fun initialize(tirada: Tirada?, currentDate: String) {
        _formState.value = if (tirada != null) {
            TiradaFormState.fromTirada(tirada)
        } else {
            TiradaFormState.empty(currentDate)
        }
    }
    
    /**
     * Procesa eventos del formulario
     */
    fun onEvent(event: TiradaFormEvent) {
        when (event) {
            is TiradaFormEvent.DescripcionChanged -> updateField { 
                copy(descripcion = event.value, descripcionError = null) 
            }
            is TiradaFormEvent.LocalizacionChanged -> updateField { 
                copy(localizacion = event.value) 
            }
            is TiradaFormEvent.CategoriaChanged -> updateField { 
                copy(categoria = event.value) 
            }
            is TiradaFormEvent.ModalidadChanged -> {
                val newMaxPuntuacion = Tirada.getMaxPuntuacion(event.value).toFloat()
                updateField { 
                    copy(
                        modalidad = event.value,
                        // Ajustar puntuación si excede el nuevo máximo
                        puntuacion = if (puntuacion > newMaxPuntuacion) newMaxPuntuacion else puntuacion
                    ) 
                }
            }
            is TiradaFormEvent.FechaChanged -> updateField { 
                copy(fecha = event.value, fechaError = null) 
            }
            is TiradaFormEvent.PuntuacionChanged -> {
                val maxPuntuacion = _formState.value.maxPuntuacion
                updateField { 
                    copy(puntuacion = event.value.coerceIn(0f, maxPuntuacion)) 
                }
            }
            is TiradaFormEvent.Save -> save()
            is TiradaFormEvent.ResetErrors -> resetErrors()
        }
    }
    
    private fun updateField(update: TiradaFormState.() -> TiradaFormState) {
        _formState.update { it.update() }
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
                _uiState.value = TiradaFormUiState.Loading
                
                val userId = firebaseAuth.currentUser?.uid
                val tirada = validatedState.toTirada()
                
                if (validatedState.isEditing) {
                    tiradaRepository.updateTirada(tirada, userId).getOrThrow()
                    _uiState.value = TiradaFormUiState.Success(
                        context.getString(R.string.tirada_updated)
                    )
                } else {
                    tiradaRepository.saveTirada(tirada, userId).getOrThrow()
                    _uiState.value = TiradaFormUiState.Success(
                        context.getString(R.string.tirada_saved)
                    )
                }
                
                _effect.emit(TiradaFormEffect.NavigateBack)
                
            } catch (e: Exception) {
                crashlytics.recordException(e)
                _uiState.value = TiradaFormUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    private fun validate(state: TiradaFormState): TiradaFormState {
        return state.copy(
            descripcionError = if (state.descripcion.isBlank()) errorFieldRequired else null,
            fechaError = if (state.fecha.isBlank()) errorFieldRequired else null
        )
    }
    
    private fun resetErrors() {
        _formState.update { 
            it.copy(
                descripcionError = null,
                fechaError = null
            )
        }
    }
    
    fun resetUiState() {
        _uiState.value = TiradaFormUiState.Idle
    }
}

/**
 * Efectos secundarios del formulario
 */
sealed class TiradaFormEffect {
    data object NavigateBack : TiradaFormEffect()
    data class ShowSnackbar(val message: String) : TiradaFormEffect()
    data class ShowError(val message: String) : TiradaFormEffect()
}
