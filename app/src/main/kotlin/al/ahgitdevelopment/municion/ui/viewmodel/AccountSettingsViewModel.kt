package al.ahgitdevelopment.municion.ui.viewmodel

import al.ahgitdevelopment.municion.auth.FirebaseAuthRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AccountSettingsViewModel - ViewModel para configuracion de cuenta
 *
 * v3.4.0: Auth Simplification
 * - Eliminado: AuthManager, PIN, biometrics, vinculacion
 * - Solo muestra info de cuenta y permite cerrar sesion
 *
 * @since v3.0.0 (TRACK B - Auth Modernization)
 * @updated v3.4.0 (Auth Simplification)
 */
@HiltViewModel
class AccountSettingsViewModel @Inject constructor(
    private val firebaseAuthRepository: FirebaseAuthRepository
) : ViewModel() {

    companion object {
        private const val TAG = "AccountSettingsVM"
    }

    private val _uiState = MutableStateFlow<AccountUiState>(AccountUiState.Loading)
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    init {
        loadAccountState()
    }

    /**
     * Carga el estado actual de la cuenta.
     */
    fun loadAccountState() {
        viewModelScope.launch {
            val currentUser = firebaseAuthRepository.getCurrentUser()

            if (currentUser != null) {
                _uiState.value = AccountUiState.Loaded(
                    accountInfo = AccountInfo(
                        email = currentUser.email,
                        uid = currentUser.uid,
                        displayName = currentUser.displayName,
                        isAnonymous = currentUser.isAnonymous
                    )
                )
            } else {
                _uiState.value = AccountUiState.NotAuthenticated
            }
        }
    }

    /**
     * Cierra sesion de Firebase.
     */
    fun signOut() {
        viewModelScope.launch {
            firebaseAuthRepository.signOut()
            _uiState.value = AccountUiState.NotAuthenticated
        }
    }

    /**
     * Estados de UI.
     */
    sealed class AccountUiState {
        object Loading : AccountUiState()
        object NotAuthenticated : AccountUiState()
        data class Loaded(val accountInfo: AccountInfo) : AccountUiState()
    }

    /**
     * Informacion de cuenta.
     */
    data class AccountInfo(
        val email: String?,
        val uid: String,
        val displayName: String?,
        val isAnonymous: Boolean = false
    ) {
        val statusText: String
            get() = when {
                isAnonymous -> "Requiere migracion"
                email != null -> email
                displayName != null -> displayName
                else -> uid.take(8) + "..."
            }
    }
}