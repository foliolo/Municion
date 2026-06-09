package al.ahgitdevelopment.municion.ui.viewmodel

import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.data.repository.GuiaRepository
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

/**
 * Image upload (Firebase Storage) is wired in phase 7 once the multiplatform image pipeline
 * (FileKit + ImageRepository) lands; this version covers the CRUD + licencia selection flow.
 */
class GuiaViewModel(
    private val guiaRepository: GuiaRepository,
    private val licenciaRepository: LicenciaRepository,
    private val currentUserId: CurrentUserIdProvider,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    val guias: StateFlow<List<Guia>> = guiaRepository.guias
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val needsAttentionCount: StateFlow<Int> = guiaRepository.needsAttentionCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Licencias available to associate when creating a guía. */
    val licencias: StateFlow<List<Licencia>> = licenciaRepository.licencias
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _uiState = MutableStateFlow<EntityUiState>(EntityUiState.Idle)
    val uiState: StateFlow<EntityUiState> = _uiState.asStateFlow()

    fun saveGuia(guia: Guia) = run("Guía guardada") {
        guiaRepository.saveGuia(guia, currentUserId.currentUserId()).getOrThrow()
    }

    fun updateGuia(guia: Guia) = run("Guía actualizada") {
        guiaRepository.updateGuia(guia, currentUserId.currentUserId()).getOrThrow()
    }

    fun deleteGuia(guia: Guia) = run("Guía eliminada") {
        guiaRepository.deleteGuia(guia, currentUserId.currentUserId()).getOrThrow()
    }

    fun resetUiState() {
        _uiState.value = EntityUiState.Idle
    }

    private fun run(successMessage: String, block: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.value = EntityUiState.Loading
            _uiState.value = try {
                block()
                EntityUiState.Success(successMessage)
            } catch (e: Exception) {
                crashReporter.recordException(e)
                EntityUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}
