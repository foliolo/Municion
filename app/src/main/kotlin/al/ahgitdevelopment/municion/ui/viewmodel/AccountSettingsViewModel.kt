package al.ahgitdevelopment.municion.ui.viewmodel

import al.ahgitdevelopment.municion.auth.FirebaseAuthRepository
import al.ahgitdevelopment.municion.data.local.room.dao.SyncOperationDao
import al.ahgitdevelopment.municion.data.repository.BillingRepository
import al.ahgitdevelopment.municion.data.sync.SyncOutboxWorker
import al.ahgitdevelopment.municion.domain.usecase.ClearLocalDataUseCase
import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
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
    @ApplicationContext private val appContext: Context,
    private val firebaseAuthRepository: FirebaseAuthRepository,
    private val billingRepository: BillingRepository,
    private val clearLocalDataUseCase: ClearLocalDataUseCase,
    private val syncOperationDao: SyncOperationDao
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

    /**
     * Number of outbox operations not yet synced to Firebase. Surfaced in
     * the Settings UI so the user knows whether it's safe to sign out.
     */
    val pendingSyncCount: StateFlow<Int> = syncOperationDao.countPendingFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /**
     * Number of outbox operations that have exhausted their retries.
     * Worth surfacing because their writes are LOST from Firebase's side
     * (Room still has them) until the user retries manually.
     */
    val failedSyncCount: StateFlow<Int> = syncOperationDao.countFailedFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

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
     * Re-enqueues every FAILED outbox row for another drain pass. Used
     * by the Settings UI button after the user reviews the failed count.
     * The data is still in Room (writes were never lost locally), this
     * just gives Firebase another chance.
     */
    fun retryFailedSync() {
        viewModelScope.launch {
            val reset = syncOperationDao.resetFailedToRetry()
            Log.i(TAG, "Re-enqueued $reset FAILED outbox rows for retry")
            SyncOutboxWorker.enqueueOneShot(appContext)
        }
    }

    /**
     * Cierra sesion de Firebase y limpia datos locales.
     *
     * @param force when true, skip the outbox drain and sign out immediately.
     * The UI should set this only after the user explicitly accepts losing
     * unsynced changes.
     */
    fun signOut(force: Boolean = false) {
        viewModelScope.launch {
            try {
                if (!force) {
                    // Best-effort: kick the worker and wait up to 10s for the
                    // outbox to drain. If we time out, the caller should have
                    // already shown a confirmation dialog because
                    // [pendingSyncCount] is non-zero in the UI.
                    SyncOutboxWorker.enqueueOneShot(appContext)
                    val drained = withTimeoutOrNull(10_000) {
                        syncOperationDao.countPendingFlow().first { it == 0 }
                    }
                    if (drained == null) {
                        Log.w(TAG, "Outbox did not drain within 10s; signing out anyway")
                    }
                }
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