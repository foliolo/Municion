package al.ahgitdevelopment.municion.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * AuthViewModel - ViewModel para gestión de autenticación
 *
 * FASE 2: Auth State Management
 * - Maneja el estado de autenticación local (PIN) y Firebase
 * - Expone estados reactivos para la UI
 * - Gestiona login anónimo automático para nuevos usuarios
 *
 * @since v3.0.0 (TRACK B - Auth Modernization)
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authManager: AuthManager,
    private val firebaseAuth: FirebaseAuth,
    private val crashlytics: FirebaseCrashlytics
) : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _firebaseAuthState = MutableStateFlow<FirebaseAuthState>(FirebaseAuthState.Unknown)
    val firebaseAuthState: StateFlow<FirebaseAuthState> = _firebaseAuthState.asStateFlow()

    init {
        checkAuthState()
    }

    /**
     * Verifica el estado de autenticación al inicio
     */
    private fun checkAuthState() {
        viewModelScope.launch {
            // Verificar si hay sesión local activa
            if (authManager.hasActiveSession()) {
                _authState.value = AuthState.Authenticated
                // Verificar Firebase auth también
                checkFirebaseAuth()
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    /**
     * Verifica y configura autenticación Firebase
     * Si no hay usuario, crea uno anónimo
     */
    private fun checkFirebaseAuth() {
        viewModelScope.launch {
            val currentUser = firebaseAuth.currentUser

            if (currentUser != null) {
                _firebaseAuthState.value = if (currentUser.isAnonymous) {
                    FirebaseAuthState.Anonymous(currentUser)
                } else {
                    FirebaseAuthState.Linked(currentUser)
                }
            } else {
                // Crear usuario anónimo automáticamente
                signInAnonymously()
            }
        }
    }

    /**
     * Inicia sesión anónima en Firebase
     * Esto permite que los usuarios guarden datos sin crear cuenta
     */
    fun signInAnonymously() {
        viewModelScope.launch {
            try {
                _firebaseAuthState.value = FirebaseAuthState.Loading
                val result = firebaseAuth.signInAnonymously().await()
                result.user?.let { user ->
                    _firebaseAuthState.value = FirebaseAuthState.Anonymous(user)
                    android.util.Log.i(TAG, "Anonymous sign-in successful: ${user.uid}")
                } ?: run {
                    _firebaseAuthState.value = FirebaseAuthState.Error("Failed to create anonymous user")
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Anonymous sign-in failed", e)
                crashlytics.recordException(e)
                _firebaseAuthState.value = FirebaseAuthState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Verifica PIN y activa sesión local
     */
    fun verifyPinAndActivateSession(pin: String): Boolean {
        val isValid = authManager.verifyPin(pin)
        if (isValid) {
            _authState.value = AuthState.Authenticated
            // Iniciar Firebase auth si es necesario
            checkFirebaseAuth()
        }
        return isValid
    }

    /**
     * Configura nuevo PIN
     */
    fun setupPin(pin: String): Result<Unit> {
        val result = authManager.setupPin(pin)
        if (result.isSuccess) {
            _authState.value = AuthState.Authenticated
            // Iniciar Firebase auth para nuevo usuario
            signInAnonymously()
        }
        return result
    }

    /**
     * Cierra sesión local (no Firebase - datos permanecen)
     */
    fun closeLocalSession() {
        authManager.closeSession()
        _authState.value = AuthState.Unauthenticated
    }

    /**
     * Cierra sesión completa (local + Firebase)
     * ADVERTENCIA: Datos de usuario anónimo se pierden
     */
    fun signOutCompletely() {
        viewModelScope.launch {
            authManager.closeSession()
            firebaseAuth.signOut()
            _authState.value = AuthState.Unauthenticated
            _firebaseAuthState.value = FirebaseAuthState.SignedOut
        }
    }

    /**
     * Verifica si el usuario tiene cuenta vinculada (no anónima)
     */
    fun isAccountLinked(): Boolean {
        val user = firebaseAuth.currentUser ?: return false
        return !user.isAnonymous
    }

    /**
     * Obtiene el UID del usuario actual
     */
    fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid

    /**
     * Estados de autenticación local
     */
    sealed class AuthState {
        object Loading : AuthState()
        object Unauthenticated : AuthState()
        object Authenticated : AuthState()
        data class Error(val message: String) : AuthState()
    }

    /**
     * Estados de autenticación Firebase
     */
    sealed class FirebaseAuthState {
        object Unknown : FirebaseAuthState()
        object Loading : FirebaseAuthState()
        object SignedOut : FirebaseAuthState()
        data class Anonymous(val user: FirebaseUser) : FirebaseAuthState()
        data class Linked(val user: FirebaseUser) : FirebaseAuthState()
        data class Error(val message: String) : FirebaseAuthState()

        fun isAuthenticated(): Boolean = this is Anonymous || this is Linked
        fun isAnonymous(): Boolean = this is Anonymous
    }
}
