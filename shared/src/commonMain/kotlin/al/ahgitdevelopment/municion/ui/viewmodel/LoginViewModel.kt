package al.ahgitdevelopment.municion.ui.viewmodel

import al.ahgitdevelopment.municion.auth.FirebaseAuthRepository
import al.ahgitdevelopment.municion.util.isValidEmail
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Login / sign-up / password-reset for the auth screen. */
class LoginViewModel(
    private val authRepository: FirebaseAuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun signIn(email: String, password: String) {
        if (!validateInput(email, password)) return
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            authRepository.signInWithEmail(email.trim(), password)
                .onSuccess { _uiState.value = LoginUiState.Success(it) }
                .onFailure { _uiState.value = LoginUiState.Error(mapError(it)) }
        }
    }

    fun createAccount(email: String, password: String, confirmPassword: String) {
        if (!validateInput(email, password)) return
        if (password != confirmPassword) {
            _uiState.value = LoginUiState.Error("Las contraseñas no coinciden")
            return
        }
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            authRepository.createAccount(email.trim(), password)
                .onSuccess { _uiState.value = LoginUiState.Success(it) }
                .onFailure { _uiState.value = LoginUiState.Error(mapError(it)) }
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank() || !isValidEmail(email)) {
            _uiState.value = LoginUiState.Error("Email no válido")
            return
        }
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            authRepository.sendPasswordResetEmail(email.trim())
                .onSuccess { _uiState.value = LoginUiState.PasswordResetSent }
                .onFailure { _uiState.value = LoginUiState.Error(mapError(it)) }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }

    private fun validateInput(email: String, password: String): Boolean {
        val error = when {
            email.isBlank() -> "Introduce tu email"
            !isValidEmail(email) -> "Email no válido"
            password.isBlank() -> "Introduce tu contraseña"
            password.length < MIN_PASSWORD_LENGTH -> "La contraseña debe tener al menos $MIN_PASSWORD_LENGTH caracteres"
            else -> null
        }
        return if (error != null) {
            _uiState.value = LoginUiState.Error(error)
            false
        } else {
            true
        }
    }

    sealed class LoginUiState {
        data object Idle : LoginUiState()
        data object Loading : LoginUiState()
        data class Success(val user: FirebaseUser) : LoginUiState()
        data object PasswordResetSent : LoginUiState()
        data class Error(val message: String) : LoginUiState()
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
    }
}

/** Best-effort mapping of GitLive/Firebase auth error messages to Spanish UI text. */
internal fun mapError(error: Throwable): String {
    val msg = error.message.orEmpty()
    return when {
        msg.contains("no user record", true) || msg.contains("user-not-found", true) ->
            "No existe una cuenta con este email"
        msg.contains("password is invalid", true) || msg.contains("wrong-password", true) ||
            msg.contains("credential is incorrect", true) || msg.contains("invalid-credential", true) ->
            "Email o contraseña incorrectos"
        msg.contains("weak", true) -> "La contraseña es demasiado débil"
        msg.contains("already in use", true) || msg.contains("email-already", true) ->
            "Ya existe una cuenta con este email"
        msg.contains("network", true) -> "Error de conexión. Comprueba tu internet"
        msg.contains("blocked", true) -> "Demasiados intentos. Inténtalo más tarde"
        else -> "Error: ${msg.ifBlank { "desconocido" }}"
    }
}
