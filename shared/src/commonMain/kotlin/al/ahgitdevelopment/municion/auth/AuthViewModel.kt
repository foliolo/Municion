package al.ahgitdevelopment.municion.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Drives the auth gate. Reacts to GitLive's [FirebaseAuth.authStateChanged] flow so logout/login
 * propagate immediately without manual listener management.
 */
class AuthViewModel(
    private val auth: FirebaseAuth,
) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        viewModelScope.launch {
            auth.authStateChanged.collect { user -> updateAuthState(user) }
        }
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun isAccountLinked(): Boolean = auth.currentUser?.let { !it.isAnonymous } ?: false

    private fun updateAuthState(user: FirebaseUser?) {
        _authState.value =
            when {
                user == null -> AuthState.NotAuthenticated
                user.isAnonymous -> AuthState.RequiresMigration(user)
                else -> AuthState.Authenticated(user)
            }
    }

    sealed class AuthState {
        data object Loading : AuthState()

        data object NotAuthenticated : AuthState()

        data class RequiresMigration(
            val user: FirebaseUser,
        ) : AuthState()

        data class Authenticated(
            val user: FirebaseUser,
        ) : AuthState()

        data class Error(
            val message: String,
        ) : AuthState()
    }
}
