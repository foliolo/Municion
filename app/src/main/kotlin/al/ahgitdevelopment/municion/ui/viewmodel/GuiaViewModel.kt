package al.ahgitdevelopment.municion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.data.repository.GuiaRepository
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
 * ViewModel para Guías
 *
 * FASE 3: ViewModels con Hilt
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
@HiltViewModel
class GuiaViewModel @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val firebaseAuth: FirebaseAuth,
    private val crashlytics: FirebaseCrashlytics
) : ViewModel() {

    val guias: StateFlow<List<Guia>> = guiaRepository.guias
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _uiState = MutableStateFlow<GuiaUiState>(GuiaUiState.Idle)
    val uiState: StateFlow<GuiaUiState> = _uiState.asStateFlow()

    fun saveGuia(guia: Guia) {
        viewModelScope.launch {
            _uiState.value = GuiaUiState.Loading
            try {
                val userId = firebaseAuth.currentUser?.uid
                guiaRepository.saveGuia(guia, userId).getOrThrow()
                _uiState.value = GuiaUiState.Success("Guía guardada")
            } catch (e: Exception) {
                crashlytics.recordException(e)
                _uiState.value = GuiaUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun updateGuia(guia: Guia) {
        viewModelScope.launch {
            _uiState.value = GuiaUiState.Loading
            try {
                val userId = firebaseAuth.currentUser?.uid
                guiaRepository.updateGuia(guia, userId).getOrThrow()
                _uiState.value = GuiaUiState.Success("Guía actualizada")
            } catch (e: Exception) {
                crashlytics.recordException(e)
                _uiState.value = GuiaUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun deleteGuia(guia: Guia) {
        viewModelScope.launch {
            _uiState.value = GuiaUiState.Loading
            try {
                val userId = firebaseAuth.currentUser?.uid
                guiaRepository.deleteGuia(guia, userId).getOrThrow()
                _uiState.value = GuiaUiState.Success("Guía eliminada")
            } catch (e: Exception) {
                crashlytics.recordException(e)
                _uiState.value = GuiaUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = GuiaUiState.Idle
    }

    sealed class GuiaUiState {
        object Idle : GuiaUiState()
        object Loading : GuiaUiState()
        data class Success(val message: String) : GuiaUiState()
        data class Error(val message: String) : GuiaUiState()
    }
}
