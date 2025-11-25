package al.ahgitdevelopment.municion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import al.ahgitdevelopment.municion.domain.usecase.SyncDataUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * MainViewModel para estado compartido de la app
 *
 * FASE 3: ViewModels con Hilt
 * - Maneja autenticación Firebase
 * - Sincronización con Firebase
 * - Estado global de la app
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val syncDataUseCase: SyncDataUseCase,
    private val crashlytics: FirebaseCrashlytics
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            val user = firebaseAuth.currentUser
            if (user != null) {
                _uiState.value = MainUiState.Authenticated(user.uid)
                // Auto-sync on startup
                syncFromFirebase()
            } else {
                // DEFENSA: Intentar crear usuario anónimo en lugar de cerrar la app
                android.util.Log.w("MainViewModel", "No Firebase user found - attempting recovery")
                attemptFirebaseRecovery()
            }
        }
    }

    /**
     * Intenta recuperar la autenticación de Firebase creando un usuario anónimo.
     * Si falla, navega a Login en lugar de cerrar la app abruptamente.
     */
    private suspend fun attemptFirebaseRecovery() {
        try {
            _uiState.value = MainUiState.Loading
            val result = firebaseAuth.signInAnonymously().await()
            result.user?.let { user ->
                android.util.Log.i("MainViewModel", "Recovery successful: ${user.uid}")
                _uiState.value = MainUiState.Authenticated(user.uid)
                syncFromFirebase()
            } ?: run {
                android.util.Log.e("MainViewModel", "Recovery failed: user is null")
                _uiState.value = MainUiState.Unauthenticated
            }
        } catch (e: Exception) {
            android.util.Log.e("MainViewModel", "Recovery failed", e)
            crashlytics.recordException(e)
            _uiState.value = MainUiState.Unauthenticated
        }
    }

    fun syncFromFirebase() {
        val userId = currentUserId ?: return

        viewModelScope.launch {
            _syncState.value = SyncState.Syncing
            try {
                val result = syncDataUseCase.syncFromFirebase(userId).getOrThrow()
                _syncState.value = if (result.allSuccess) {
                    SyncState.Success(result.successCount)
                } else {
                    SyncState.PartialSuccess(result.successCount)
                }
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Sync failed", e)
                crashlytics.recordException(e)
                _syncState.value = SyncState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun syncToFirebase() {
        val userId = currentUserId ?: return

        viewModelScope.launch {
            _syncState.value = SyncState.Syncing
            try {
                val result = syncDataUseCase.syncToFirebase(userId).getOrThrow()
                _syncState.value = if (result.allSuccess) {
                    SyncState.Success(result.successCount)
                } else {
                    SyncState.PartialSuccess(result.successCount)
                }
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Sync failed", e)
                crashlytics.recordException(e)
                _syncState.value = SyncState.Error(e.message ?: "Unknown error")
            }
        }
    }

    sealed class MainUiState {
        object Loading : MainUiState()
        object Unauthenticated : MainUiState()
        data class Authenticated(val userId: String) : MainUiState()
    }

    sealed class SyncState {
        object Idle : SyncState()
        object Syncing : SyncState()
        data class Success(val count: Int) : SyncState()
        data class PartialSuccess(val count: Int) : SyncState()
        data class Error(val message: String) : SyncState()
    }
}
