package al.ahgitdevelopment.municion.ui.viewmodel

import al.ahgitdevelopment.municion.auth.FirebaseAuthRepository
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la pantalla de Login/Registro.
 *
 * Maneja:
 * - Login con email/password
 * - Registro de nueva cuenta
 * - Recuperacion de contrasena
 * - Estados de UI (loading, error, success)
 *
 * @since v3.4.0 (Auth Simplification)
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: FirebaseAuthRepository
) : ViewModel() {

    companion object {
        private const val TAG = "LoginViewModel"
        private const val MIN_PASSWORD_LENGTH = 6
    }

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Inicia sesion con email y password existentes.
     */
    fun signIn(email: String, password: String) {
        if (!validateInput(email, password)) return

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            authRepository.signInWithEmail(email.trim(), password)
                .onSuccess { user ->
                    Log.i(TAG, "Sign-in successful: ${user.uid}")
                    _uiState.value = LoginUiState.Success(user)
                }
                .onFailure { error ->
                    Log.e(TAG, "Sign-in failed", error)
                    _uiState.value = LoginUiState.Error(mapFirebaseError(error))
                }
        }
    }

    /**
     * Crea una nueva cuenta con email y password.
     */
    fun createAccount(email: String, password: String, confirmPassword: String) {
        if (!validateInput(email, password)) return

        if (password != confirmPassword) {
            _uiState.value = LoginUiState.Error("Las contrasenas no coinciden")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            authRepository.createAccount(email.trim(), password)
                .onSuccess { user ->
                    Log.i(TAG, "Account created: ${user.uid}")
                    _uiState.value = LoginUiState.Success(user)
                }
                .onFailure { error ->
                    Log.e(TAG, "Account creation failed", error)
                    _uiState.value = LoginUiState.Error(mapFirebaseError(error))
                }
        }
    }

    /**
     * Envia email de recuperacion de contrasena.
     */
    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _uiState.value = LoginUiState.Error("Introduce tu email")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _uiState.value = LoginUiState.Error("Email no valido")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            authRepository.sendPasswordResetEmail(email.trim())
                .onSuccess {
                    Log.i(TAG, "Password reset email sent to: $email")
                    _uiState.value = LoginUiState.PasswordResetSent
                }
                .onFailure { error ->
                    Log.e(TAG, "Password reset failed", error)
                    _uiState.value = LoginUiState.Error(mapFirebaseError(error))
                }
        }
    }

    /**
     * Resetea el estado de UI a Idle.
     */
    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }

    private fun validateInput(email: String, password: String): Boolean {
        when {
            email.isBlank() -> {
                _uiState.value = LoginUiState.Error("Introduce tu email")
                return false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> {
                _uiState.value = LoginUiState.Error("Email no valido")
                return false
            }
            password.isBlank() -> {
                _uiState.value = LoginUiState.Error("Introduce tu contrasena")
                return false
            }
            password.length < MIN_PASSWORD_LENGTH -> {
                _uiState.value = LoginUiState.Error("La contrasena debe tener al menos $MIN_PASSWORD_LENGTH caracteres")
                return false
            }
        }
        return true
    }

    private fun mapFirebaseError(error: Throwable): String {
        return when (error) {
            is FirebaseAuthInvalidUserException -> "No existe una cuenta con este email"
            is FirebaseAuthInvalidCredentialsException -> "Email o contrasena incorrectos"
            is FirebaseAuthWeakPasswordException -> "La contrasena es demasiado debil"
            is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con este email"
            else -> {
                val message = error.message ?: "Error desconocido"
                when {
                    message.contains("network", ignoreCase = true) -> "Error de conexion. Comprueba tu internet"
                    message.contains("blocked", ignoreCase = true) -> "Demasiados intentos. Intenta mas tarde"
                    else -> "Error: $message"
                }
            }
        }
    }

    /**
     * Estados de UI para la pantalla de login.
     */
    sealed class LoginUiState {
        object Idle : LoginUiState()
        object Loading : LoginUiState()
        data class Success(val user: FirebaseUser) : LoginUiState()
        object PasswordResetSent : LoginUiState()
        data class Error(val message: String) : LoginUiState()
    }
}