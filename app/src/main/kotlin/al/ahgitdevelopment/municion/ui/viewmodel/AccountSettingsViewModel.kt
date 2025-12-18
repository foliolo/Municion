package al.ahgitdevelopment.municion.ui.viewmodel

import al.ahgitdevelopment.municion.auth.FirebaseAuthRepository
import al.ahgitdevelopment.municion.data.repository.BillingRepository
import al.ahgitdevelopment.municion.domain.usecase.ClearLocalDataUseCase
import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
 * @updated v3.2.2 (Auth Simplification)
 */
@HiltViewModel
class AccountSettingsViewModel @Inject constructor(
    private val firebaseAuthRepository: FirebaseAuthRepository,
    private val billingRepository: BillingRepository,
    private val clearLocalDataUseCase: ClearLocalDataUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "AccountSettingsVM"
    }

    private val _uiState = MutableStateFlow<AccountUiState>(AccountUiState.Loading)
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    val isAdsRemoved: StateFlow<Boolean> = billingRepository.isAdsRemoved
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val isPurchaseAvailable: StateFlow<Boolean> = billingRepository.productDetails
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

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

    fun launchPurchaseFlow(activity: Activity) {
        billingRepository.launchBillingFlow(activity)
    }

    /**
     * Cierra sesion de Firebase y limpia datos locales.
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                Log.i(TAG, "SignOut requested - clearing local data...")
                clearLocalDataUseCase()
                Log.i(TAG, "Local data cleared.")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing local data during sign out", e)
            } finally {
                firebaseAuthRepository.signOut()
                _uiState.value = AccountUiState.NotAuthenticated
            }
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