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

/** Mandatory migration: link an existing anonymous account to email/password (keeps uid + data). */
class MigrationViewModel(
    private val authRepository: FirebaseAuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MigrationUiState>(MigrationUiState.Idle)
    val uiState: StateFlow<MigrationUiState> = _uiState.asStateFlow()

    fun linkAccount(email: String, password: String, confirmPassword: String) {
        if (!validateInput(email, password, confirmPassword)) return
        viewModelScope.launch {
            _uiState.value = MigrationUiState.Loading
            authRepository.linkWithEmail(email.trim(), password)
                .onSuccess { _uiState.value = MigrationUiState.Success(it) }
                .onFailure { _uiState.value = MigrationUiState.Error(mapError(it)) }
        }
    }

    fun resetState() {
        _uiState.value = MigrationUiState.Idle
    }

    private fun validateInput(email: String, password: String, confirmPassword: String): Boolean {
        val error = when {
            email.isBlank() -> "Introduce tu email"
            !isValidEmail(email) -> "Email no válido"
            password.isBlank() -> "Introduce tu contraseña"
            password.length < MIN_PASSWORD_LENGTH -> "La contraseña debe tener al menos $MIN_PASSWORD_LENGTH caracteres"
            password != confirmPassword -> "Las contraseñas no coinciden"
            else -> null
        }
        return if (error != null) {
            _uiState.value = MigrationUiState.Error(error)
            false
        } else {
            true
        }
    }

    sealed class MigrationUiState {
        data object Idle : MigrationUiState()
        data object Loading : MigrationUiState()
        data class Success(val user: FirebaseUser) : MigrationUiState()
        data class Error(val message: String) : MigrationUiState()
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
    }
}
