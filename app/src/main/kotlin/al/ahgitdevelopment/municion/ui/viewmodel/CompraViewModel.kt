package al.ahgitdevelopment.municion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.repository.CompraRepository
import al.ahgitdevelopment.municion.domain.usecase.CreateCompraUseCase
import al.ahgitdevelopment.municion.domain.usecase.DeleteCompraUseCase
import android.util.Log
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
 * ViewModel para Compras
 *
 * FASE 3: ViewModels con Hilt
 * - Observa compras desde Repository (Flow)
 * - Operaciones CRUD con validación de cupo
 * - UI state management
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
@HiltViewModel
class CompraViewModel @Inject constructor(
    private val compraRepository: CompraRepository,
    private val createCompraUseCase: CreateCompraUseCase,
    private val deleteCompraUseCase: DeleteCompraUseCase,
    private val firebaseAuth: FirebaseAuth,
    private val crashlytics: FirebaseCrashlytics
) : ViewModel() {

    // Observa TODAS las compras automáticamente
    val compras: StateFlow<List<Compra>> = compraRepository.compras
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _uiState = MutableStateFlow<CompraUiState>(CompraUiState.Idle)
    val uiState: StateFlow<CompraUiState> = _uiState.asStateFlow()

    fun createCompra(compra: Compra) {
        viewModelScope.launch {
            _uiState.value = CompraUiState.Loading
            try {
                val userId = firebaseAuth.currentUser?.uid
                createCompraUseCase(compra, userId).getOrThrow()
                _uiState.value = CompraUiState.Success("Compra creada exitosamente")
            } catch (e: Exception) {
                Log.e("CompraViewModel", "Error creating compra", e)
                crashlytics.recordException(e)
                _uiState.value = CompraUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun updateCompra(compra: Compra) {
        viewModelScope.launch {
            _uiState.value = CompraUiState.Loading
            try {
                val userId = firebaseAuth.currentUser?.uid
                compraRepository.updateCompra(compra, userId).getOrThrow()
                _uiState.value = CompraUiState.Success("Compra actualizada")
            } catch (e: Exception) {
                Log.e("CompraViewModel", "Error updating compra", e)
                crashlytics.recordException(e)
                _uiState.value = CompraUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun deleteCompra(compra: Compra) {
        viewModelScope.launch {
            _uiState.value = CompraUiState.Loading
            try {
                val userId = firebaseAuth.currentUser?.uid
                deleteCompraUseCase(compra, userId).getOrThrow()
                _uiState.value = CompraUiState.Success("Compra eliminada")
            } catch (e: Exception) {
                Log.e("CompraViewModel", "Error deleting compra", e)
                crashlytics.recordException(e)
                _uiState.value = CompraUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = CompraUiState.Idle
    }

    sealed class CompraUiState {
        object Idle : CompraUiState()
        object Loading : CompraUiState()
        data class Success(val message: String) : CompraUiState()
        data class Error(val message: String) : CompraUiState()
    }
}
