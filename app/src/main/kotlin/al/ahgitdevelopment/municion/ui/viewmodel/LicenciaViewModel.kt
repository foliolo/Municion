package al.ahgitdevelopment.municion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.data.repository.LicenciaRepository
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
 * ViewModel para Licencias
 *
 * FASE 4: ViewModels adicionales para completar UI
 * - Observa licencias desde Repository (Flow)
 * - Operaciones CRUD
 * - UI state management
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
@HiltViewModel
class LicenciaViewModel @Inject constructor(
    private val licenciaRepository: LicenciaRepository,
    private val firebaseAuth: FirebaseAuth,
    private val crashlytics: FirebaseCrashlytics
) : ViewModel() {

    val licencias: StateFlow<List<Licencia>> = licenciaRepository.licencias
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _uiState = MutableStateFlow<LicenciaUiState>(LicenciaUiState.Idle)
    val uiState: StateFlow<LicenciaUiState> = _uiState.asStateFlow()

    fun saveLicencia(licencia: Licencia) {
        viewModelScope.launch {
            _uiState.value = LicenciaUiState.Loading
            try {
                val userId = firebaseAuth.currentUser?.uid
                licenciaRepository.saveLicencia(licencia, userId).getOrThrow()
                _uiState.value = LicenciaUiState.Success("Licencia guardada")
            } catch (e: Exception) {
                crashlytics.recordException(e)
                _uiState.value = LicenciaUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun updateLicencia(licencia: Licencia) {
        viewModelScope.launch {
            _uiState.value = LicenciaUiState.Loading
            try {
                val userId = firebaseAuth.currentUser?.uid
                licenciaRepository.updateLicencia(licencia, userId).getOrThrow()
                _uiState.value = LicenciaUiState.Success("Licencia actualizada")
            } catch (e: Exception) {
                crashlytics.recordException(e)
                _uiState.value = LicenciaUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun deleteLicencia(licencia: Licencia) {
        viewModelScope.launch {
            _uiState.value = LicenciaUiState.Loading
            try {
                val userId = firebaseAuth.currentUser?.uid
                licenciaRepository.deleteLicencia(licencia, userId).getOrThrow()
                _uiState.value = LicenciaUiState.Success("Licencia eliminada")
            } catch (e: Exception) {
                crashlytics.recordException(e)
                _uiState.value = LicenciaUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = LicenciaUiState.Idle
    }

    sealed class LicenciaUiState {
        object Idle : LicenciaUiState()
        object Loading : LicenciaUiState()
        data class Success(val message: String) : LicenciaUiState()
        data class Error(val message: String) : LicenciaUiState()
    }
}
