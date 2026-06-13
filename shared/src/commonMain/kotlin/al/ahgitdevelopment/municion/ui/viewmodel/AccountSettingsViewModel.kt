package al.ahgitdevelopment.municion.ui.viewmodel

import al.ahgitdevelopment.municion.ads.RemoveAdsManager
import al.ahgitdevelopment.municion.auth.FirebaseAuthRepository
import al.ahgitdevelopment.municion.auth.SocialAuthProvider
import al.ahgitdevelopment.municion.domain.usecase.ClearLocalDataUseCase
import al.ahgitdevelopment.municion.resources.Res
import al.ahgitdevelopment.municion.resources.settings_delete_error
import al.ahgitdevelopment.municion.resources.settings_delete_error_apple
import al.ahgitdevelopment.municion.resources.settings_purchase_error
import al.ahgitdevelopment.municion.resources.settings_purchase_not_completed
import al.ahgitdevelopment.municion.resources.settings_purchase_success
import al.ahgitdevelopment.municion.resources.settings_restore_error
import al.ahgitdevelopment.municion.resources.settings_restore_none
import al.ahgitdevelopment.municion.resources.settings_restore_success
import al.ahgitdevelopment.municion.util.ScreenshotMode
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

/**
 * Settings: shows the user profile (avatar, name, auth provider), lets the user buy/restore
 * "remove ads" (RevenueCat), sign out (clears local data), or delete the account.
 */
class AccountSettingsViewModel(
    private val authRepository: FirebaseAuthRepository,
    private val socialAuthProvider: SocialAuthProvider,
    private val clearLocalDataUseCase: ClearLocalDataUseCase,
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
    private val _purchaseMessage = MutableStateFlow<StringResource?>(null)
    val purchaseMessage: StateFlow<StringResource?> = _purchaseMessage.asStateFlow()

    /** True while account deletion runs (the Apple re-login + revocation can take a moment). */
    private val _deleteInFlight = MutableStateFlow(false)
    val deleteInFlight: StateFlow<Boolean> = _deleteInFlight.asStateFlow()

    /** One-shot message shown if deletion is cancelled or fails; cleared via [consumeDeleteMessage]. */
    private val _deleteMessage = MutableStateFlow<StringResource?>(null)
    val deleteMessage: StateFlow<StringResource?> = _deleteMessage.asStateFlow()

    init {
        loadAccountState()
    }

    fun loadAccountState() {
        if (ScreenshotMode.enabled) {
            // Fictional profile for App Store screenshots — no real personal data.
            _uiState.value =
                AccountUiState.Loaded(
                    AccountInfo(
                        email = "alex.garcia@example.com",
                        uid = "screenshot-demo",
                        displayName = "Alex García",
                        photoUrl = null,
                        isAnonymous = false,
                        provider = AuthProvider.GOOGLE,
                    ),
                )
            return
        }
        val user = authRepository.getCurrentUser()
        _uiState.value =
            if (user != null) {
                val provider =
                    when {
                        user.isAnonymous -> AuthProvider.ANONYMOUS
                        user.providerData.any { it.providerId == GOOGLE_PROVIDER_ID } -> AuthProvider.GOOGLE
                        user.providerData.any { it.providerId == APPLE_PROVIDER_ID } -> AuthProvider.APPLE
                        else -> AuthProvider.EMAIL
                    }
                AccountUiState.Loaded(
                    AccountInfo(
                        email = user.email,
                        uid = user.uid,
                        displayName = user.displayName,
                        photoUrl = user.photoURL,
                        isAnonymous = user.isAnonymous,
                        provider = provider,
                    ),
                )
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
                    onSuccess = { if (it) Res.string.settings_purchase_success else Res.string.settings_purchase_not_completed },
                    onFailure = { Res.string.settings_purchase_error },
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
                    onSuccess = { if (it) Res.string.settings_restore_success else Res.string.settings_restore_none },
                    onFailure = { Res.string.settings_restore_error },
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
                        _deleteMessage.value = Res.string.settings_delete_error_apple
                        return@launch
                    }
                }

                val deletion = authRepository.deleteAccount()
                if (deletion.isFailure) {
                    _deleteMessage.value = Res.string.settings_delete_error
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

    enum class AuthProvider { GOOGLE, APPLE, EMAIL, ANONYMOUS }

    data class AccountInfo(
        val email: String?,
        val uid: String,
        val displayName: String?,
        val photoUrl: String?,
        val isAnonymous: Boolean,
        val provider: AuthProvider,
    )

    private companion object {
        const val APPLE_PROVIDER_ID = "apple.com"
        const val GOOGLE_PROVIDER_ID = "google.com"
    }
}
