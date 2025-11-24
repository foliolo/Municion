package al.ahgitdevelopment.municion.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import al.ahgitdevelopment.municion.auth.AuthManager
import al.ahgitdevelopment.municion.auth.FirebaseAuthRepository
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AccountSettingsViewModel - ViewModel para configuración de cuenta
 *
 * FASE 3: Account Linking UI
 * - Muestra estado actual de la cuenta (anónima/vinculada)
 * - Permite vincular cuenta con Google o Email
 * - Permite desvincular cuenta
 * - Gestión de PIN y biometría
 *
 * @since v3.0.0 (TRACK B - Auth Modernization)
 */
@HiltViewModel
class AccountSettingsViewModel @Inject constructor(
    private val authManager: AuthManager,
    private val firebaseAuthRepository: FirebaseAuthRepository,
    private val crashlytics: FirebaseCrashlytics
) : ViewModel() {

    companion object {
        private const val TAG = "AccountSettingsVM"
    }

    private val _uiState = MutableStateFlow<AccountUiState>(AccountUiState.Loading)
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    private val _linkingState = MutableStateFlow<LinkingState>(LinkingState.Idle)
    val linkingState: StateFlow<LinkingState> = _linkingState.asStateFlow()

    init {
        loadAccountState()
    }

    /**
     * Carga el estado actual de la cuenta
     */
    fun loadAccountState() {
        viewModelScope.launch {
            val currentUser = firebaseAuthRepository.getCurrentUser()
            val hasPinConfigured = authManager.hasPinConfigured()
            val biometricEnabled = authManager.isBiometricEnabled()
            val biometricAvailable = authManager.canAuthenticateWithBiometrics().isAvailable()

            if (currentUser != null) {
                _uiState.value = AccountUiState.Loaded(
                    accountInfo = AccountInfo(
                        isAnonymous = currentUser.isAnonymous,
                        email = currentUser.email,
                        uid = currentUser.uid,
                        displayName = currentUser.displayName
                    ),
                    securityInfo = SecurityInfo(
                        hasPinConfigured = hasPinConfigured,
                        biometricEnabled = biometricEnabled,
                        biometricAvailable = biometricAvailable
                    )
                )
            } else {
                _uiState.value = AccountUiState.NotAuthenticated
            }
        }
    }

    /**
     * Vincula la cuenta con Google
     */
    fun linkWithGoogle(idToken: String) {
        viewModelScope.launch {
            _linkingState.value = LinkingState.Linking
            try {
                val result = firebaseAuthRepository.linkWithGoogle(idToken)
                if (result.isSuccess) {
                    _linkingState.value = LinkingState.Success("Cuenta vinculada con Google")
                    loadAccountState() // Recargar estado
                } else {
                    _linkingState.value = LinkingState.Error(
                        result.exceptionOrNull()?.message ?: "Error vinculando con Google"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error linking with Google", e)
                crashlytics.recordException(e)
                _linkingState.value = LinkingState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Vincula la cuenta con Email/Password
     */
    fun linkWithEmail(email: String, password: String) {
        viewModelScope.launch {
            // Validaciones
            if (email.isBlank()) {
                _linkingState.value = LinkingState.Error("El email no puede estar vacío")
                return@launch
            }
            if (password.length < 6) {
                _linkingState.value = LinkingState.Error("La contraseña debe tener al menos 6 caracteres")
                return@launch
            }

            _linkingState.value = LinkingState.Linking
            try {
                val result = firebaseAuthRepository.linkWithEmail(email, password)
                if (result.isSuccess) {
                    _linkingState.value = LinkingState.Success("Cuenta vinculada con Email")
                    loadAccountState() // Recargar estado
                } else {
                    _linkingState.value = LinkingState.Error(
                        result.exceptionOrNull()?.message ?: "Error vinculando con Email"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error linking with Email", e)
                crashlytics.recordException(e)
                _linkingState.value = LinkingState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Cierra sesión de Firebase (mantiene datos locales)
     */
    fun signOut() {
        viewModelScope.launch {
            firebaseAuthRepository.signOut()
            authManager.closeSession()
            _uiState.value = AccountUiState.NotAuthenticated
        }
    }

    /**
     * Habilita/deshabilita autenticación biométrica
     */
    fun setBiometricEnabled(enabled: Boolean) {
        authManager.setBiometricEnabled(enabled)
        loadAccountState() // Recargar para reflejar cambio
    }

    /**
     * Cambia el PIN
     */
    fun changePin(currentPin: String, newPin: String): Result<Unit> {
        return authManager.changePin(currentPin, newPin)
    }

    /**
     * Resetea el estado de vinculación
     */
    fun resetLinkingState() {
        _linkingState.value = LinkingState.Idle
    }

    /**
     * Estados de UI
     */
    sealed class AccountUiState {
        object Loading : AccountUiState()
        object NotAuthenticated : AccountUiState()
        data class Loaded(
            val accountInfo: AccountInfo,
            val securityInfo: SecurityInfo
        ) : AccountUiState()
    }

    /**
     * Estados de vinculación
     */
    sealed class LinkingState {
        object Idle : LinkingState()
        object Linking : LinkingState()
        data class Success(val message: String) : LinkingState()
        data class Error(val message: String) : LinkingState()
    }

    /**
     * Información de cuenta
     */
    data class AccountInfo(
        val isAnonymous: Boolean,
        val email: String?,
        val uid: String,
        val displayName: String?
    ) {
        val statusText: String
            get() = if (isAnonymous) {
                "Cuenta anónima (solo este dispositivo)"
            } else {
                "Cuenta vinculada: ${email ?: displayName ?: uid}"
            }

        val canSync: Boolean
            get() = !isAnonymous
    }

    /**
     * Información de seguridad
     */
    data class SecurityInfo(
        val hasPinConfigured: Boolean,
        val biometricEnabled: Boolean,
        val biometricAvailable: Boolean
    )
}
