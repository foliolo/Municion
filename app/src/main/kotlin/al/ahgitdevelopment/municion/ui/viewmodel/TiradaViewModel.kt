package al.ahgitdevelopment.municion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
import al.ahgitdevelopment.municion.data.repository.TiradaRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para Tiradas
 *
 * FASE 4: ViewModels adicionales para completar UI
 * - Observa tiradas desde Repository (Flow)
 * - Operaciones CRUD
 * - UI state management
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
@HiltViewModel
class TiradaViewModel @Inject constructor(
    private val tiradaRepository: TiradaRepository,
    private val firebaseAuth: FirebaseAuth,
    private val crashlytics: FirebaseCrashlytics
) : ViewModel() {

    val tiradas: StateFlow<List<Tirada>> = tiradaRepository.tiradas
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _uiState = MutableStateFlow<TiradaUiState>(TiradaUiState.Idle)
    val uiState: StateFlow<TiradaUiState> = _uiState.asStateFlow()

    fun saveTirada(tirada: Tirada) {
        viewModelScope.launch {
            _uiState.value = TiradaUiState.Loading
            try {
                val userId = firebaseAuth.currentUser?.uid
                tiradaRepository.saveTirada(tirada, userId).getOrThrow()
                _uiState.value = TiradaUiState.Success("Tirada guardada")
            } catch (e: Exception) {
                crashlytics.recordException(e)
                _uiState.value = TiradaUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun updateTirada(tirada: Tirada) {
        viewModelScope.launch {
            _uiState.value = TiradaUiState.Loading
            try {
                val userId = firebaseAuth.currentUser?.uid
                tiradaRepository.updateTirada(tirada, userId).getOrThrow()
                _uiState.value = TiradaUiState.Success("Tirada actualizada")
            } catch (e: Exception) {
                crashlytics.recordException(e)
                _uiState.value = TiradaUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun deleteTirada(tirada: Tirada) {
        viewModelScope.launch {
            _uiState.value = TiradaUiState.Loading
            try {
                val userId = firebaseAuth.currentUser?.uid
                tiradaRepository.deleteTirada(tirada, userId).getOrThrow()
                _uiState.value = TiradaUiState.Success("Tirada eliminada")
            } catch (e: Exception) {
                crashlytics.recordException(e)
                _uiState.value = TiradaUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = TiradaUiState.Idle
    }

    sealed class TiradaUiState {
        object Idle : TiradaUiState()
        object Loading : TiradaUiState()
        data class Success(val message: String) : TiradaUiState()
        data class Error(val message: String) : TiradaUiState()
    }
}
