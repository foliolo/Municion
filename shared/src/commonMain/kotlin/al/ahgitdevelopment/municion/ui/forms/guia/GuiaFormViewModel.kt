package al.ahgitdevelopment.municion.ui.forms.guia

import al.ahgitdevelopment.municion.data.repository.GuiaRepository
import al.ahgitdevelopment.municion.firebase.CrashReporter
import al.ahgitdevelopment.municion.firebase.CurrentUserIdProvider
import al.ahgitdevelopment.municion.ui.forms.FormUiState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GuiaFormViewModel(
    private val repository: GuiaRepository,
    private val currentUserId: CurrentUserIdProvider,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    private val _state = MutableStateFlow(GuiaFormState())
    val state: StateFlow<GuiaFormState> = _state.asStateFlow()

    private val _uiState = MutableStateFlow<FormUiState>(FormUiState.Idle)
    val uiState: StateFlow<FormUiState> = _uiState.asStateFlow()

    fun initialize(guiaId: Int?, tipoLicencia: Int) {
        if (guiaId == null || guiaId <= 0) {
            _state.value = GuiaFormState(tipoLicencia = tipoLicencia, isEditing = false)
            return
        }
        viewModelScope.launch {
            repository.getGuiaById(guiaId)?.let { _state.value = GuiaFormState.fromGuia(it) }
                ?: run { _state.value = GuiaFormState(tipoLicencia = tipoLicencia, isEditing = false) }
        }
    }

    fun onMarca(value: String) = _state.update { it.copy(marca = value, marcaError = null) }
    fun onModelo(value: String) = _state.update { it.copy(modelo = value, modeloError = null) }
    fun onApodo(value: String) = _state.update { it.copy(apodo = value, apodoError = null) }

    fun onTipoArma(value: Int) = _state.update {
        it.copy(
            tipoArma = value,
            // Refresh the default quota unless the user opted into a custom value (or is editing).
            cupo = if (!it.customCupo && !it.isEditing) defaultCupoForType(value).toString() else it.cupo,
        )
    }

    fun onCalibre1(value: String) = _state.update { it.copy(calibre1 = value, calibre1Error = null) }
    fun onCalibre2(value: String) = _state.update { it.copy(calibre2 = value) }
    fun onShowCalibre2(value: Boolean) = _state.update {
        it.copy(showCalibre2 = value, calibre2 = if (!value) "" else it.calibre2)
    }

    fun onNumGuia(value: String) = _state.update { it.copy(numGuia = value, numGuiaError = null) }
    fun onNumArma(value: String) = _state.update { it.copy(numArma = value, numArmaError = null) }

    fun onCustomCupo(value: Boolean) = _state.update { it.copy(customCupo = value) }
    fun onCupo(value: String) {
        if (value.all { it.isDigit() }) _state.update { it.copy(cupo = value, cupoError = null) }
    }

    fun onGastado(value: String) {
        if (value.all { it.isDigit() }) _state.update { it.copy(gastado = value) }
    }

    fun save() {
        val s = _state.value
        var hasErrors = false
        if (s.marca.isBlank()) { _state.update { it.copy(marcaError = "Introduce la marca") }; hasErrors = true }
        if (s.modelo.isBlank()) { _state.update { it.copy(modeloError = "Introduce el modelo") }; hasErrors = true }
        if (s.apodo.isBlank()) { _state.update { it.copy(apodoError = "Introduce el apodo") }; hasErrors = true }
        if (s.calibre1.isBlank()) { _state.update { it.copy(calibre1Error = "Introduce el calibre") }; hasErrors = true }
        if (s.numGuia.isBlank()) { _state.update { it.copy(numGuiaError = "Introduce el número de guía") }; hasErrors = true }
        if (s.numArma.isBlank()) { _state.update { it.copy(numArmaError = "Introduce el número de arma") }; hasErrors = true }
        if ((s.cupo.toIntOrNull() ?: 0) <= 0) { _state.update { it.copy(cupoError = "Introduce un cupo válido") }; hasErrors = true }
        if (hasErrors) return

        viewModelScope.launch {
            _uiState.value = FormUiState.Saving
            val userId = currentUserId.currentUserId()
            val guia = _state.value.toGuia()
            val result = if (s.isEditing) {
                repository.updateGuia(guia, userId).map { }
            } else {
                repository.saveGuia(guia, userId).map { }
            }
            _uiState.value = result.fold(
                onSuccess = { FormUiState.Saved(if (s.isEditing) "Guía actualizada" else "Guía guardada") },
                onFailure = { crashReporter.recordException(it); FormUiState.Error(it.message ?: "Error al guardar") },
            )
        }
    }

    fun resetUiState() { _uiState.value = FormUiState.Idle }

    /** Default annual quota suggested per weapon-type index (matches the `tipo_armas` array order). */
    private fun defaultCupoForType(tipoArmaIndex: Int): Int = when (tipoArmaIndex) {
        0 -> 100 // Pistola
        1 -> 5000 // Escopeta
        2 -> 1000 // Rifle
        3 -> 100 // Revólver
        4 -> 100 // Avancarga
        else -> 100
    }
}
