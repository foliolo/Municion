package al.ahgitdevelopment.municion.ads

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * "Remove ads" entitlement source of truth.
 *
 * The bound implementation is [al.ahgitdevelopment.municion.purchases.RevenueCatRemoveAdsManager]
 * (official `purchases-kmp` SDK; `ad_free` entitlement). It degrades to no-op behaviour until the
 * RevenueCat SDK keys are configured (see MIGRATION_REPORT §4.C). [NoOpRemoveAdsManager] remains for
 * tests/previews.
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
