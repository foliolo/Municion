package al.ahgitdevelopment.municion.ui.viewmodel

import al.ahgitdevelopment.municion.ads.RemoveAdsManager
import al.ahgitdevelopment.municion.domain.usecase.SyncDataUseCase
import al.ahgitdevelopment.municion.firebase.CrashReporter
import al.ahgitdevelopment.municion.firebase.CurrentUserIdProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Drives data sync from the main screen. Ads gating ([showAds]) is wired to RevenueCat in
 * phase 7; until then ads are shown by default.
 */
class MainViewModel(
    private val syncDataUseCase: SyncDataUseCase,
    private val currentUserId: CurrentUserIdProvider,
    private val crashReporter: CrashReporter,
    private val removeAdsManager: RemoveAdsManager,
) : ViewModel() {
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    /** Show ads unless the user has the remove-ads entitlement. */
    val showAds: StateFlow<Boolean> =
        removeAdsManager.hasRemovedAds
            .map { !it }
            .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val userId: String? get() = currentUserId.currentUserId()

    fun initSync() {
        if (userId != null) {
            viewModelScope.launch { removeAdsManager.initialize(userId) }
            syncFromFirebase()
        }
    }

    fun syncFromFirebase() {
        val uid = userId ?: return
        viewModelScope.launch {
            _syncState.value = SyncState.Syncing
            _syncState.value =
                try {
                    val result = syncDataUseCase.syncFromFirebaseWithAutoFix(uid).getOrThrow()
                    when {
                        result.allSuccess && !result.hasParseErrors -> SyncState.Success(result.successCount)
                        result.allSuccess -> SyncState.SuccessWithParseErrors(result.successCount, result.allParseErrors.size)
                        else -> SyncState.PartialSuccess(result.successCount)
                    }
                } catch (e: Exception) {
                    crashReporter.recordException(e)
                    SyncState.Error(e.message ?: "Error de sincronización")
                }
        }
    }

    /** Manual "force sync": trigger an outbox drain. */
    fun forceSync() {
        syncDataUseCase.syncToFirebase()
    }

    sealed class SyncState {
        data object Idle : SyncState()

        data object Syncing : SyncState()

        data class Success(
            val count: Int,
        ) : SyncState()

        data class SuccessWithParseErrors(
            val count: Int,
            val parseErrorCount: Int,
        ) : SyncState()

        data class PartialSuccess(
            val count: Int,
        ) : SyncState()

        data class Error(
            val message: String,
        ) : SyncState()
    }
}
