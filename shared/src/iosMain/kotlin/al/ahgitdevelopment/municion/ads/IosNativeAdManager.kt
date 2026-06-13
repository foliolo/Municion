package al.ahgitdevelopment.municion.ads

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * iOS native ad pool. Delegates loading/teardown to the Swift [NativeAdBridge]; until that bridge is
 * registered (post Xcode/SPM setup) [loadAds] is a no-op and the lists show no native ads.
 *
 * Like the Android implementation, this manager is the single owner of the ad-free gating: it
 * observes [RemoveAdsManager.hasRemovedAds], drops the pool when the entitlement activates, refuses
 * to load while ad-free, and discards in-flight loader deliveries that arrive after the purchase.
 */
class IosNativeAdManager(
    private val removeAdsManager: RemoveAdsManager,
) : NativeAdManager {
    private val ads = MutableStateFlow<List<NativeAdHandle>>(emptyList())
    override val nativeAds: StateFlow<List<NativeAdHandle>> = ads.asStateFlow()

    // App-lifetime singleton: the scope is never cancelled on purpose.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        scope.launch {
            removeAdsManager.hasRemovedAds.collect { removed ->
                if (removed) destroyAds()
            }
        }
    }

    override fun loadAds() {
        if (removeAdsManager.hasRemovedAds.value) return
        val loader = NativeAdBridge.loader ?: return
        loader.load(AdUnitIds.nativeAdvanced, NativeAdManager.AD_POOL_SIZE) { handle ->
            // The user may have bought remove-ads while this request was in flight:
            // discard the delivery instead of repopulating the (already destroyed) pool.
            if (removeAdsManager.hasRemovedAds.value) {
                NativeAdBridge.loader?.destroy(listOf(handle))
            } else {
                ads.update { (it + handle).takeLast(NativeAdManager.AD_POOL_SIZE) }
            }
        }
    }

    override fun destroyAds() {
        val current = ads.value
        ads.value = emptyList()
        NativeAdBridge.loader?.destroy(current)
    }
}
