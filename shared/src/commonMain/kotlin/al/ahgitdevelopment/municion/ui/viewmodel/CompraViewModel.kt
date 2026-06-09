package al.ahgitdevelopment.municion.ui.viewmodel

import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.repository.CompraRepository
import al.ahgitdevelopment.municion.domain.usecase.CreateCompraUseCase
import al.ahgitdevelopment.municion.domain.usecase.DeleteCompraUseCase
import al.ahgitdevelopment.municion.domain.usecase.UpdateCompraUseCase
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

class CompraViewModel(
    private val compraRepository: CompraRepository,
    private val createCompraUseCase: CreateCompraUseCase,
    private val updateCompraUseCase: UpdateCompraUseCase,
    private val deleteCompraUseCase: DeleteCompraUseCase,
    private val currentUserId: CurrentUserIdProvider,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    val compras: StateFlow<List<Compra>> = compraRepository.compras
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val needsAttentionCount: StateFlow<Int> = compraRepository.needsAttentionCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val _uiState = MutableStateFlow<EntityUiState>(EntityUiState.Idle)
    val uiState: StateFlow<EntityUiState> = _uiState.asStateFlow()

    fun createCompra(compra: Compra) = run("Compra creada") {
        createCompraUseCase(compra, currentUserId.currentUserId()).getOrThrow()
    }

    /** [oldCompra] is needed to compute the quota adjustment across store/range transitions. */
    fun updateCompra(oldCompra: Compra, newCompra: Compra) = run("Compra actualizada") {
        updateCompraUseCase(oldCompra, newCompra, currentUserId.currentUserId()).getOrThrow()
    }

    fun deleteCompra(compra: Compra) = run("Compra eliminada") {
        deleteCompraUseCase(compra, currentUserId.currentUserId()).getOrThrow()
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
