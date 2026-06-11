package al.ahgitdevelopment.municion.ads

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * iOS native ad pool. Delegates loading/teardown to the Swift [NativeAdBridge]; until that bridge is
 * registered (post Xcode/SPM setup) [loadAds] is a no-op and the lists show no native ads.
 */
class IosNativeAdManager : NativeAdManager {
    private val ads = MutableStateFlow<List<NativeAdHandle>>(emptyList())
    override val nativeAds: StateFlow<List<NativeAdHandle>> = ads.asStateFlow()

    override fun loadAds() {
        val loader = NativeAdBridge.loader ?: return
        loader.load(AdUnitIds.nativeAdvanced, NativeAdManager.AD_POOL_SIZE) { handle ->
            ads.update { (it + handle).takeLast(NativeAdManager.AD_POOL_SIZE) }
        }
    }

    override fun destroyAds() {
        val current = ads.value
        ads.value = emptyList()
        NativeAdBridge.loader?.destroy(current)
    }
}
