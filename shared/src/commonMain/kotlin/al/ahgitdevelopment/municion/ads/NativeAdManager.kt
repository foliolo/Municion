package al.ahgitdevelopment.municion.ads

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Loads and holds a small pool of native advanced ads shared across the four list screens
 * (Licencias, Guías, Compras, Tiradas). The UI collects [nativeAds] and interleaves them via
 * [itemsWithNativeAds]; an empty pool (not loaded yet, or ads removed) renders no ads.
 *
 * Lifecycle: [loadAds] is triggered once after consent + `MobileAds` init from the platform
 * bootstrap (Android: AdsBootstrap; iOS: iOSApp.swift). [destroyAds] is called when the user buys
 * the remove-ads entitlement, which clears the flow and the four lists stop showing ads reactively.
 */
interface NativeAdManager {
    val nativeAds: StateFlow<List<NativeAdHandle>>

    fun loadAds()

    fun destroyAds()

    companion object {
        // TODO: mover AD_FREQUENCY a Firebase Remote Config para ajustar la cadencia sin publicar release.
        const val AD_FREQUENCY = 3

        const val AD_POOL_SIZE = 5
    }
}

/** Fallback used in tests/previews and wherever native ads are disabled. */
class NoOpNativeAdManager : NativeAdManager {
    override val nativeAds: StateFlow<List<NativeAdHandle>> =
        MutableStateFlow<List<NativeAdHandle>>(emptyList()).asStateFlow()

    override fun loadAds() = Unit

    override fun destroyAds() = Unit
}
