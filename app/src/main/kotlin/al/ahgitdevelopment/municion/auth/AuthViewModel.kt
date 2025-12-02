package al.ahgitdevelopment.municion.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AuthViewModel - ViewModel para gestion de autenticacion
 *
 * v3.4.0: Auth Simplification
 * - Eliminado: Login anonimo automatico, PIN local, sesion local
 * - Nuevo flujo: Usuario debe registrarse con email/password desde el inicio
 * - Usuarios anonimos existentes deben migrar (MigrationScreen)
 * - Usa AuthStateListener para reaccionar automaticamente a cambios de auth
 *
 * @since v3.0.0 (TRACK B - Auth Modernization)
 * @updated v3.4.0 (Auth Simplification)
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val clearLocalDataUseCase: al.ahgitdevelopment.municion.domain.usecase.ClearLocalDataUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    /**
     * Listener que reacciona automaticamente a cambios de autenticacion.
     * Esto evita race conditions en logout - cuando Firebase completa signOut,
     * este listener actualiza el estado inmediatamente.
     */
    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        val currentUser = auth.currentUser
        Log.i(TAG, "AuthStateListener triggered - user: ${currentUser?.uid}")
        updateAuthState(currentUser)
    }

    init {
        // Registrar listener para cambios de auth
        firebaseAuth.addAuthStateListener(authStateListener)
        // Verificar estado inicial
        updateAuthState(firebaseAuth.currentUser)
    }

    override fun onCleared() {
        super.onCleared()
        // Limpiar listener al destruir el ViewModel
        firebaseAuth.removeAuthStateListener(authStateListener)
    }

    /**
     * Actualiza el estado basado en el usuario actual.
     * Llamado por AuthStateListener o manualmente.
     */
    private fun updateAuthState(currentUser: FirebaseUser?) {
        _authState.value = when {
            currentUser == null -> {
                Log.i(TAG, "No user authenticated")
                AuthState.NotAuthenticated
            }
            currentUser.isAnonymous -> {
                Log.i(TAG, "Anonymous user detected - requires migration")
                AuthState.RequiresMigration(currentUser)
            }
            else -> {
                Log.i(TAG, "User authenticated: ${currentUser.email}")
                AuthState.Authenticated(currentUser)
            }
        }
    }

    /**
     * Verifica el estado de autenticacion manualmente.
     * Normalmente no es necesario llamar esto - AuthStateListener lo hace automaticamente.
     */
    fun checkAuthState() {
        updateAuthState(firebaseAuth.currentUser)
    }

    /**
     * Cierra sesion de Firebase y limpia base de datos local.
     * El AuthStateListener actualizara el estado automaticamente cuando complete.
     */
    fun signOut() {
        Log.i(TAG, "Signing out...")
        
        // Lanzar coroutine para limpiar DB antes de hacer signOut de Firebase
        // Nota: Idealmente esperariamos a que termine, pero signOut es sincrono en UI.
        // Usamos viewModelScope para asegurar que se ejecute.
        viewModelScope.launch {
            try {
                clearLocalDataUseCase()
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing data on logout", e)
            } finally {
                firebaseAuth.signOut()
                // No es necesario actualizar _authState aqui - AuthStateListener lo hara
            }
        }
    }

    /**
     * Obtiene el UID del usuario actual.
     */
    fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid

    /**
     * Verifica si el usuario tiene cuenta vinculada (no anonima).
     */
    fun isAccountLinked(): Boolean {
        val user = firebaseAuth.currentUser ?: return false
        return !user.isAnonymous
    }

    /**
     * Estados de autenticacion simplificados.
     *
     * @since v3.4.0 (Auth Simplification)
     */
    sealed class AuthState {
        /** Estado inicial mientras se verifica */
        object Loading : AuthState()

        /** Usuario no autenticado - mostrar LoginScreen */
        object NotAuthenticated : AuthState()

        /** Usuario anonimo existente - mostrar MigrationScreen */
        data class RequiresMigration(val user: FirebaseUser) : AuthState()

        /** Usuario autenticado con email - mostrar contenido principal */
        data class Authenticated(val user: FirebaseUser) : AuthState()

        /** Error de autenticacion */
        data class Error(val message: String) : AuthState()
    }
}
