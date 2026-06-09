package al.ahgitdevelopment.municion.ui.viewmodel

import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.data.repository.LicenciaRepository
import al.ahgitdevelopment.municion.firebase.CrashReporter
import al.ahgitdevelopment.municion.firebase.CurrentUserIdProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LicenciaViewModel(
    private val licenciaRepository: LicenciaRepository,
    private val currentUserId: CurrentUserIdProvider,
    private val crashReporter: CrashReporter,
) : ViewModel() {
    val licencias: StateFlow<List<Licencia>> =
        licenciaRepository.licencias
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val needsAttentionCount: StateFlow<Int> =
        licenciaRepository.needsAttentionCount
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val _uiState = MutableStateFlow<EntityUiState>(EntityUiState.Idle)
    val uiState: StateFlow<EntityUiState> = _uiState.asStateFlow()

    fun saveLicencia(licencia: Licencia) =
        run("Licencia guardada") {
            licenciaRepository.saveLicencia(licencia, currentUserId.currentUserId()).getOrThrow()
        }

    fun updateLicencia(licencia: Licencia) =
        run("Licencia actualizada") {
            licenciaRepository.updateLicencia(licencia, currentUserId.currentUserId()).getOrThrow()
        }

    fun deleteLicencia(licencia: Licencia) =
        run("Licencia eliminada") {
            licenciaRepository.deleteLicencia(licencia, currentUserId.currentUserId()).getOrThrow()
        }

    fun resetUiState() {
        _uiState.value = EntityUiState.Idle
    }

    private fun run(
        successMessage: String,
        block: suspend () -> Unit,
    ) {
        viewModelScope.launch {
            _uiState.value = EntityUiState.Loading
            _uiState.value =
                try {
                    block()
                    EntityUiState.Success(successMessage)
                } catch (e: Exception) {
                    crashReporter.recordException(e)
                    EntityUiState.Error(e.message ?: "Error desconocido")
                }
        }
    }
}
