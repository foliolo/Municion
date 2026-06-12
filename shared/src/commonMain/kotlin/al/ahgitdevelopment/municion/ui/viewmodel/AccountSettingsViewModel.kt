package al.ahgitdevelopment.municion.ui.viewmodel

import al.ahgitdevelopment.municion.ads.RemoveAdsManager
import al.ahgitdevelopment.municion.auth.FirebaseAuthRepository
import al.ahgitdevelopment.municion.auth.SocialAuthProvider
import al.ahgitdevelopment.municion.data.local.room.dao.SyncOperationDao
import al.ahgitdevelopment.municion.data.sync.SyncScheduler
import al.ahgitdevelopment.municion.domain.usecase.ClearLocalDataUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Account settings: shows account info + sync health, and lets the user retry failed syncs,
 * force a sync, sign out (clears local data), delete the account, or buy/restore "remove ads"
 * (RevenueCat).
 */
class AccountSettingsViewModel(
    private val authRepository: FirebaseAuthRepository,
    private val socialAuthProvider: SocialAuthProvider,
    private val clearLocalDataUseCase: ClearLocalDataUseCase,
    private val syncOperationDao: SyncOperationDao,
    private val syncScheduler: SyncScheduler,
    private val removeAdsManager: RemoveAdsManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow<AccountUiState>(AccountUiState.Loading)
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    /** Whether the user owns the ad-free entitlement (mirrors RevenueCat). */
    val hasRemovedAds: StateFlow<Boolean> = removeAdsManager.hasRemovedAds

    /** True while a purchase/restore is in flight (disables the buttons). */
    private val _purchaseInFlight = MutableStateFlow(false)
    val purchaseInFlight: StateFlow<Boolean> = _purchaseInFlight.asStateFlow()

    /** One-shot user-facing message after a purchase/restore; cleared via [consumePurchaseMessage]. */
    private val _purchaseMessage = MutableStateFlow<String?>(null)
    val purchaseMessage: StateFlow<String?> = _purchaseMessage.asStateFlow()

    /** True while account deletion runs (the Apple re-login + revocation can take a moment). */
    private val _deleteInFlight = MutableStateFlow(false)
    val deleteInFlight: StateFlow<Boolean> = _deleteInFlight.asStateFlow()

    /** One-shot message shown if deletion is cancelled or fails; cleared via [consumeDeleteMessage]. */
    private val _deleteMessage = MutableStateFlow<String?>(null)
    val deleteMessage: StateFlow<String?> = _deleteMessage.asStateFlow()

    val pendingSyncCount: StateFlow<Int> =
        syncOperationDao
            .countPendingFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val failedSyncCount: StateFlow<Int> =
        syncOperationDao
            .countFailedFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    init {
        loadAccountState()
    }

    fun loadAccountState() {
        val user = authRepository.getCurrentUser()
        _uiState.value =
            if (user != null) {
                AccountUiState.Loaded(AccountInfo(email = user.email, uid = user.uid, isAnonymous = user.isAnonymous))
            } else {
                AccountUiState.NotAuthenticated
            }
    }

    fun purchaseRemoveAds() {
        if (_purchaseInFlight.value) return
        viewModelScope.launch {
            _purchaseInFlight.value = true
            _purchaseMessage.value =
                removeAdsManager.purchaseRemoveAds().fold(
                    onSuccess = { if (it) "¡Anuncios eliminados!" else "Compra no completada" },
                    onFailure = { "No se pudo completar la compra" },
                )
            _purchaseInFlight.value = false
        }
    }

    fun restorePurchases() {
        if (_purchaseInFlight.value) return
        viewModelScope.launch {
            _purchaseInFlight.value = true
            _purchaseMessage.value =
                removeAdsManager.restore().fold(
                    onSuccess = { if (it) "Compra restaurada" else "No se encontraron compras" },
                    onFailure = { "No se pudo restaurar la compra" },
                )
            _purchaseInFlight.value = false
        }
    }

    fun consumePurchaseMessage() {
        _purchaseMessage.value = null
    }

    fun consumeDeleteMessage() {
        _deleteMessage.value = null
    }

    fun forceSync() = syncScheduler.requestImmediateDrain()

    fun retryFailedSync() {
        viewModelScope.launch {
            syncOperationDao.resetFailedToRetry()
            syncScheduler.requestImmediateDrain()
        }
    }

    /** Clears local data and signs out. The auth flow then routes back to Login. */
    fun signOut() {
        viewModelScope.launch {
            clearLocalDataUseCase()
            authRepository.signOut()
            _uiState.value = AccountUiState.NotAuthenticated
        }
    }

    /**
     * Deletes the account permanently. For users who signed in with Apple we first force a fresh
     * Sign in with Apple to (a) satisfy Firebase's "recent login" requirement for deletion and
     * (b) revoke the Apple token (Apple's account-deletion requirement). Cloud data (Realtime
     * Database + Storage) is wiped server-side by the "Delete User Data" Firebase extension once
     * the auth user is removed.
     */
    fun deleteAccount() {
        if (_deleteInFlight.value) return
        viewModelScope.launch {
            _deleteInFlight.value = true
            try {
                val user = authRepository.getCurrentUser()
                val isAppleUser = user?.providerData?.any { it.providerId == APPLE_PROVIDER_ID } == true

                if (isAppleUser) {
                    val reauth = socialAuthProvider.reauthenticateAndRevokeApple()
                    if (reauth.isFailure) {
                        _deleteMessage.value =
                            "No se pudo eliminar la cuenta. Vuelve a iniciar sesión con Apple e inténtalo de nuevo."
                        return@launch
                    }
                }

                val deletion = authRepository.deleteAccount()
                if (deletion.isFailure) {
                    _deleteMessage.value = "No se pudo eliminar la cuenta. Inténtalo de nuevo."
                    return@launch
                }

                clearLocalDataUseCase()
                _uiState.value = AccountUiState.NotAuthenticated
            } finally {
                _deleteInFlight.value = false
            }
        }
    }

    sealed class AccountUiState {
        data object Loading : AccountUiState()

        data object NotAuthenticated : AccountUiState()

        data class Loaded(
            val accountInfo: AccountInfo,
        ) : AccountUiState()
    }

    data class AccountInfo(
        val email: String?,
        val uid: String,
        val isAnonymous: Boolean = false,
    ) {
        val statusText: String
            get() =
                when {
                    isAnonymous -> "Requiere migración"
                    !email.isNullOrBlank() -> email
                    else -> uid.take(8) + "…"
                }
    }

    private companion object {
        const val APPLE_PROVIDER_ID = "apple.com"
    }
}
