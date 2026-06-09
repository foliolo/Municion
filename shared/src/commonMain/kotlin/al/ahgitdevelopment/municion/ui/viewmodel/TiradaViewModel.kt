package al.ahgitdevelopment.municion.ui.viewmodel

import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
import al.ahgitdevelopment.municion.data.repository.TiradaRepository
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

class TiradaViewModel(
    private val tiradaRepository: TiradaRepository,
    private val currentUserId: CurrentUserIdProvider,
    private val crashReporter: CrashReporter,
) : ViewModel() {
    val tiradas: StateFlow<List<Tirada>> =
        tiradaRepository.tiradas
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val needsAttentionCount: StateFlow<Int> =
        tiradaRepository.needsAttentionCount
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val _uiState = MutableStateFlow<EntityUiState>(EntityUiState.Idle)
    val uiState: StateFlow<EntityUiState> = _uiState.asStateFlow()

    fun saveTirada(tirada: Tirada) =
        run("Tirada guardada") {
            tiradaRepository.saveTirada(tirada, currentUserId.currentUserId()).getOrThrow()
        }

    fun updateTirada(tirada: Tirada) =
        run("Tirada actualizada") {
            tiradaRepository.updateTirada(tirada, currentUserId.currentUserId()).getOrThrow()
        }

    fun deleteTirada(tirada: Tirada) =
        run("Tirada eliminada") {
            tiradaRepository.deleteTirada(tirada, currentUserId.currentUserId()).getOrThrow()
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
