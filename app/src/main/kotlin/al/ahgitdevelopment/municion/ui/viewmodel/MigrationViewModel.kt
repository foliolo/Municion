package al.ahgitdevelopment.municion.ui.viewmodel

import al.ahgitdevelopment.municion.auth.FirebaseAuthRepository
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
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
 * ViewModel para la pantalla de Migracion obligatoria.
 *
 * Maneja la vinculacion de cuentas anonimas existentes con email/password.
 * El usuario no puede cancelar ni saltar esta pantalla.
 *
 * @since v3.4.0 (Auth Simplification)
 */
@HiltViewModel
class MigrationViewModel @Inject constructor(
    private val authRepository: FirebaseAuthRepository
) : ViewModel() {

    companion object {
        private const val TAG = "MigrationViewModel"
        private const val MIN_PASSWORD_LENGTH = 6
    }

    private val _uiState = MutableStateFlow<MigrationUiState>(MigrationUiState.Idle)
    val uiState: StateFlow<MigrationUiState> = _uiState.asStateFlow()

    /**
     * Vincula la cuenta anonima actual con email/password.
     * Mantiene el mismo UID y todos los datos existentes.
     */
    fun linkAccount(email: String, password: String, confirmPassword: String) {
        if (!validateInput(email, password, confirmPassword)) return

        viewModelScope.launch {
            _uiState.value = MigrationUiState.Loading

            authRepository.linkWithEmail(email.trim(), password)
                .onSuccess { user ->
                    Log.i(TAG, "Account linked successfully: ${user.uid}")
                    _uiState.value = MigrationUiState.Success(user)
                }
                .onFailure { error ->
                    Log.e(TAG, "Account linking failed", error)
                    _uiState.value = MigrationUiState.Error(mapFirebaseError(error))
                }
        }
    }

    /**
     * Resetea el estado de UI a Idle.
     */
    fun resetState() {
        _uiState.value = MigrationUiState.Idle
    }

    private fun validateInput(email: String, password: String, confirmPassword: String): Boolean {
        when {
            email.isBlank() -> {
                _uiState.value = MigrationUiState.Error("Introduce tu email")
                return false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> {
                _uiState.value = MigrationUiState.Error("Email no valido")
                return false
            }
            password.isBlank() -> {
                _uiState.value = MigrationUiState.Error("Introduce tu contrasena")
                return false
            }
            password.length < MIN_PASSWORD_LENGTH -> {
                _uiState.value = MigrationUiState.Error("La contrasena debe tener al menos $MIN_PASSWORD_LENGTH caracteres")
                return false
            }
            password != confirmPassword -> {
                _uiState.value = MigrationUiState.Error("Las contrasenas no coinciden")
                return false
            }
        }
        return true
    }

    private fun mapFirebaseError(error: Throwable): String {
        return when (error) {
            is FirebaseAuthInvalidCredentialsException -> "Credenciales no validas"
            is FirebaseAuthWeakPasswordException -> "La contrasena es demasiado debil"
            is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con este email. Contacta soporte para migrar tus datos."
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
     * Estados de UI para la pantalla de migracion.
     */
    sealed class MigrationUiState {
        object Idle : MigrationUiState()
        object Loading : MigrationUiState()
        data class Success(val user: FirebaseUser) : MigrationUiState()
        data class Error(val message: String) : MigrationUiState()
    }
}