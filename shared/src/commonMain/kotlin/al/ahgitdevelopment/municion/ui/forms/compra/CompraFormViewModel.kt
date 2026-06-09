package al.ahgitdevelopment.municion.ui.forms.compra

import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.repository.CompraRepository
import al.ahgitdevelopment.municion.data.repository.GuiaRepository
import al.ahgitdevelopment.municion.domain.usecase.CreateCompraUseCase
import al.ahgitdevelopment.municion.domain.usecase.UpdateCompraUseCase
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

/**
 * ViewModel for the Compra form.
 *
 * Spanish law: ammunition bought IN A STORE counts against the annual quota; ammunition bought AT
 * THE SHOOTING RANGE ("campo de tiro") does not. The quota adjustment is handled by
 * [CreateCompraUseCase]/[UpdateCompraUseCase]; the form simply lets the user pick the [tienda].
 */
class CompraFormViewModel(
    private val compraRepository: CompraRepository,
    private val guiaRepository: GuiaRepository,
    private val createCompraUseCase: CreateCompraUseCase,
    private val updateCompraUseCase: UpdateCompraUseCase,
    private val currentUserId: CurrentUserIdProvider,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    private val _state = MutableStateFlow(CompraFormState())
    val state: StateFlow<CompraFormState> = _state.asStateFlow()

    private val _uiState = MutableStateFlow<FormUiState>(FormUiState.Idle)
    val uiState: StateFlow<FormUiState> = _uiState.asStateFlow()

    /** Original purchase (edit mode only), kept for the quota adjustment in [UpdateCompraUseCase]. */
    private var oldCompra: Compra? = null

    fun initialize(compraId: Int?, guiaId: Int) {
        viewModelScope.launch {
            val guia = guiaRepository.getGuiaById(guiaId) ?: return@launch
            if (compraId != null && compraId > 0) {
                val compra = compraRepository.getCompraById(compraId)
                if (compra != null) {
                    oldCompra = compra
                    _state.value = CompraFormState.fromCompra(compra, guia)
                    return@launch
                }
            }
            oldCompra = null
            _state.value = CompraFormState.fromGuia(guia, fecha = todayDdMmYyyy())
        }
    }

    fun onCalibre1(value: String) = _state.update { it.copy(calibre1 = value, calibre1Error = null) }
    fun onCalibre2(value: String) = _state.update { it.copy(calibre2 = value) }
    fun onShowCalibre2(value: Boolean) =
        _state.update { it.copy(showCalibre2 = value, calibre2 = if (!value) "" else it.calibre2) }
    fun onMarca(value: String) = _state.update { it.copy(marca = value, marcaError = null) }
    fun onTipo(value: String) = _state.update { it.copy(tipo = value, tipoError = null) }
    fun onPeso(value: String) = _state.update { it.copy(peso = value, pesoError = null) }
    fun onUnidades(value: String) = _state.update { it.copy(unidades = value, unidadesError = null) }
    fun onPrecio(value: String) = _state.update { it.copy(precio = value, precioError = null) }
    fun onFecha(value: String) = _state.update { it.copy(fecha = value, fechaError = null) }
    fun onTienda(value: String) = _state.update { it.copy(tienda = value, tiendaError = null) }
    fun onValoracion(value: Float) = _state.update { it.copy(valoracion = value) }

    fun save() {
        val s = _state.value
        var hasErrors = false
        if (s.calibre1.isBlank()) { _state.update { it.copy(calibre1Error = "Introduce el calibre") }; hasErrors = true }
        if (s.marca.isBlank()) { _state.update { it.copy(marcaError = "Introduce la marca") }; hasErrors = true }
        if (s.tipo.isBlank()) { _state.update { it.copy(tipoError = "Introduce el tipo de munición") }; hasErrors = true }
        if (s.peso.toIntOrNull()?.let { it <= 0 } != false) { _state.update { it.copy(pesoError = "Introduce un peso válido") }; hasErrors = true }
        if (s.unidades.toIntOrNull()?.let { it <= 0 } != false) {
            _state.update { it.copy(unidadesError = "Introduce unidades válidas") }; hasErrors = true
        } else if (s.excedeCupo) {
            _state.update { it.copy(unidadesError = "Excede el cupo disponible (${s.cupoDisponible})") }; hasErrors = true
        }
        if (s.precio.replace(",", ".").toDoubleOrNull()?.let { it < 0.0 } != false) {
            _state.update { it.copy(precioError = "Introduce un precio válido") }; hasErrors = true
        }
        if (s.fecha.isBlank()) { _state.update { it.copy(fechaError = "Introduce la fecha") }; hasErrors = true }
        if (s.tienda.isBlank()) { _state.update { it.copy(tiendaError = "Selecciona el lugar de compra") }; hasErrors = true }
        if (hasErrors) return

        viewModelScope.launch {
            _uiState.value = FormUiState.Saving
            val userId = currentUserId.currentUserId()
            val newCompra = _state.value.toCompra()
            val result = if (s.isEditing) {
                val original = oldCompra
                if (original == null) {
                    Result.failure(IllegalStateException("Compra original no encontrada"))
                } else {
                    updateCompraUseCase(original, newCompra, userId).map { }
                }
            } else {
                createCompraUseCase(newCompra, userId).map { }
            }
            _uiState.value = result.fold(
                onSuccess = { FormUiState.Saved(if (s.isEditing) "Compra actualizada" else "Compra guardada") },
                onFailure = { crashReporter.recordException(it); FormUiState.Error(it.message ?: "Error al guardar") },
            )
        }
    }

    fun resetUiState() { _uiState.value = FormUiState.Idle }
}
