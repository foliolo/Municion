package al.ahgitdevelopment.municion.ads

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * "Remove ads" entitlement source of truth.
 *
 * The default binding is [NoOpRemoveAdsManager] (ads always shown). The RevenueCat-backed
 * implementation (via the official `purchases-kmp` SDK) is wired once RevenueCat SDK keys are
 * configured (see MIGRATION_REPORT phase 7) — `Purchases.configure(appUserID=uid)`, observe the
 * `ad_free` entitlement, and reconcile with the local [AppPurchase] row + `settings/ads_removed`.
 */
interface RemoveAdsManager {
    val hasRemovedAds: StateFlow<Boolean>
    suspend fun initialize(userId: String?)
    suspend fun purchaseRemoveAds(): Result<Boolean>
    suspend fun restore(): Result<Boolean>
}

class NoOpRemoveAdsManager : RemoveAdsManager {
    private val _hasRemovedAds = MutableStateFlow(false)
    override val hasRemovedAds: StateFlow<Boolean> = _hasRemovedAds.asStateFlow()
    override suspend fun initialize(userId: String?) = Unit
    override suspend fun purchaseRemoveAds(): Result<Boolean> = Result.success(false)
    override suspend fun restore(): Result<Boolean> = Result.success(false)
}
