package al.ahgitdevelopment.municion.ui.forms.licencia

import al.ahgitdevelopment.municion.data.repository.LicenciaRepository
import al.ahgitdevelopment.municion.firebase.CrashReporter
import al.ahgitdevelopment.municion.firebase.CurrentUserIdProvider
import al.ahgitdevelopment.municion.ui.forms.FormUiState
import al.ahgitdevelopment.municion.util.endOfYearDdMmYyyy
import al.ahgitdevelopment.municion.util.plusYearsDdMmYyyy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LicenciaFormViewModel(
    private val repository: LicenciaRepository,
    private val currentUserId: CurrentUserIdProvider,
    private val crashReporter: CrashReporter,
) : ViewModel() {
    private val _state = MutableStateFlow(LicenciaFormState())
    val state: StateFlow<LicenciaFormState> = _state.asStateFlow()

    private val _uiState = MutableStateFlow<FormUiState>(FormUiState.Idle)
    val uiState: StateFlow<FormUiState> = _uiState.asStateFlow()

    fun initialize(licenciaId: Int?) {
        if (licenciaId == null || licenciaId <= 0) return
        viewModelScope.launch {
            repository.getLicenciaById(licenciaId)?.let { _state.value = LicenciaFormState.fromLicencia(it) }
        }
    }

    fun onTipo(value: Int) {
        _state.update { it.copy(tipoLicencia = value) }
        recalc()
    }

    fun onNumLicencia(value: String) = _state.update { it.copy(numLicencia = value, numLicenciaError = null) }

    fun onFechaExpedicion(value: String) {
        _state.update { it.copy(fechaExpedicion = value, fechaExpedicionError = null) }
        recalc()
    }

    fun onFechaCaducidad(value: String) = _state.update { it.copy(fechaCaducidad = value) }

    fun onNumAbonado(value: String) = _state.update { it.copy(numAbonado = value) }

    fun onNumSeguro(value: String) = _state.update { it.copy(numSeguro = value, numSeguroError = null) }

    fun onAutonomia(value: Int) = _state.update { it.copy(autonomia = value) }

    fun onTipoPermiso(value: Int) {
        _state.update { it.copy(tipoPermisoConducir = value) }
        recalc()
    }

    fun onEdad(value: String) {
        _state.update { it.copy(edad = value, edadError = null) }
        recalc()
    }

    fun onEscala(value: Int) = _state.update { it.copy(escala = value) }

    fun onCategoria(value: Int) = _state.update { it.copy(categoria = value) }

    fun save() {
        val s = _state.value
        var hasErrors = false
        if (s.numLicencia.isBlank()) {
            _state.update { it.copy(numLicenciaError = "Introduce el número de licencia") }
            hasErrors = true
        }
        if (s.fechaExpedicion.isBlank()) {
            _state.update { it.copy(fechaExpedicionError = "Introduce la fecha de expedición") }
            hasErrors =
                true
        }
        if (s.showNumSeguro &&
            s.numSeguro.isBlank()
        ) {
            _state.update { it.copy(numSeguroError = "Introduce el número de póliza") }
            hasErrors = true
        }
        if (s.showEdad && s.edad.isBlank()) {
            _state.update { it.copy(edadError = "Introduce la edad") }
            hasErrors = true
        }
        if (hasErrors) return

        viewModelScope.launch {
            _uiState.value = FormUiState.Saving
            val userId = currentUserId.currentUserId()
            val licencia = _state.value.toLicencia()
            val result =
                if (s.isEditing) {
                    repository
                        .updateLicencia(
                            licencia,
                            userId,
                        ).map { }
                } else {
                    repository.saveLicencia(licencia, userId).map { }
                }
            _uiState.value =
                result.fold(
                    onSuccess = { FormUiState.Saved(if (s.isEditing) "Licencia actualizada" else "Licencia guardada") },
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

    /** Recomputes the expiry date from the issue date + licence type (Spanish rules). */
    private fun recalc() {
        val s = _state.value
        if (s.fechaExpedicion.isBlank()) return
        val edad = s.edad.toIntOrNull() ?: 30
        val nueva =
            when (s.tipoLicencia) {
                0 -> "31/12/3000"
                in 1..8 -> plusYearsDdMmYyyy(s.fechaExpedicion, 5)
                in 9..11 -> endOfYearDdMmYyyy(s.fechaExpedicion)
                12 -> {
                    val years =
                        if (edad < 65) {
                            if (s.tipoPermisoConducir in 0..4) 10 else 5
                        } else {
                            if (s.tipoPermisoConducir in 0..4) 5 else 3
                        }
                    plusYearsDdMmYyyy(s.fechaExpedicion, years)
                }
                else -> null
            }
        if (nueva != null) _state.update { it.copy(fechaCaducidad = nueva) }
    }
}
