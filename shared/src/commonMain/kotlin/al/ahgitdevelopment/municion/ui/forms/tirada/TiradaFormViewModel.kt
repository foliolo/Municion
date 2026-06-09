package al.ahgitdevelopment.municion.ui.forms.tirada

import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
import al.ahgitdevelopment.municion.data.repository.TiradaRepository
import al.ahgitdevelopment.municion.firebase.CrashReporter
import al.ahgitdevelopment.municion.firebase.CurrentUserIdProvider
import al.ahgitdevelopment.municion.ui.forms.FormUiState
import al.ahgitdevelopment.municion.util.todayDdMmYyyy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TiradaFormViewModel(
    private val repository: TiradaRepository,
    private val currentUserId: CurrentUserIdProvider,
    private val crashReporter: CrashReporter,
) : ViewModel() {
    private val _state = MutableStateFlow(TiradaFormState(fecha = todayDdMmYyyy()))
    val state: StateFlow<TiradaFormState> = _state.asStateFlow()

    private val _uiState = MutableStateFlow<FormUiState>(FormUiState.Idle)
    val uiState: StateFlow<FormUiState> = _uiState.asStateFlow()

    fun initialize(tiradaId: Int?) {
        if (tiradaId == null || tiradaId <= 0) return
        viewModelScope.launch {
            repository.getTiradaById(tiradaId)?.let { _state.value = TiradaFormState.fromTirada(it) }
        }
    }

    fun onDescripcion(value: String) = _state.update { it.copy(descripcion = value, descripcionError = null) }

    fun onLocalizacion(value: String) = _state.update { it.copy(localizacion = value) }

    fun onCategoria(value: String) = _state.update { it.copy(categoria = value) }

    fun onFecha(value: String) = _state.update { it.copy(fecha = value, fechaError = null) }

    fun onPuntuacion(value: String) = _state.update { it.copy(puntuacion = value.filter { c -> c.isDigit() }) }

    fun onModalidad(value: String) =
        _state.update {
            // Re-coerce score into the new modality's range.
            val max = Tirada.getMaxPuntuacion(value)
            val coerced = (it.puntuacion.toIntOrNull() ?: 0).coerceIn(0, max)
            it.copy(modalidad = value, puntuacion = coerced.toString())
        }

    fun save() {
        val s = _state.value
        var hasErrors = false
        if (s.descripcion.isBlank()) {
            _state.update { it.copy(descripcionError = "Introduce la descripción") }
            hasErrors = true
        }
        if (s.fecha.isBlank()) {
            _state.update { it.copy(fechaError = "Introduce la fecha") }
            hasErrors = true
        }
        if (hasErrors) return

        viewModelScope.launch {
            _uiState.value = FormUiState.Saving
            val userId = currentUserId.currentUserId()
            val tirada = _state.value.toTirada()
            val result =
                if (s.isEditing) {
                    repository.updateTirada(tirada, userId).map { }
                } else {
                    repository.saveTirada(tirada, userId).map { }
                }
            _uiState.value =
                result.fold(
                    onSuccess = { FormUiState.Saved(if (s.isEditing) "Tirada actualizada" else "Tirada guardada") },
                    onFailure = {
                        crashReporter.recordException(it)
                        FormUiState.Error(it.message ?: "Error al guardar")
                    },
                )
        }
    }

    fun resetUiState() {
        _uiState.value = FormUiState.Idle
    }
}
