package al.ahgitdevelopment.municion.ads

import al.ahgitdevelopment.municion.firebase.CrashReporter
import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Loads a pool of native advanced ads via [AdLoader] and exposes it reactively. A single instance is
 * shared (Koin `single`) across the four list screens; [itemsWithNativeAds] cycles the pool.
 *
 * This manager is the single owner of the ad-free gating: it observes
 * [RemoveAdsManager.hasRemovedAds] and drops the pool the moment the entitlement activates, refuses
 * to load while ad-free, and discards in-flight [AdLoader] deliveries that arrive after the purchase
 * (the load is a network call, so results can land after the entitlement flipped — the original
 * source of "ads keep showing after buying remove-ads").
 */
class AdMobNativeAdManager(
    context: Context,
    private val removeAdsManager: RemoveAdsManager,
    private val crashReporter: CrashReporter,
) : NativeAdManager {
    private val appContext = context.applicationContext
    private val ads = MutableStateFlow<List<NativeAdHandle>>(emptyList())
    override val nativeAds: StateFlow<List<NativeAdHandle>> = ads.asStateFlow()

    // App-lifetime singleton: the scope is never cancelled on purpose.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var isLoading = false

    init {
        scope.launch {
            removeAdsManager.hasRemovedAds.collect { removed ->
                if (removed) destroyAds()
            }
        }
    }

    @RequiresPermission(Manifest.permission.INTERNET)
    override fun loadAds() {
        if (removeAdsManager.hasRemovedAds.value) return
        if (isLoading || ads.value.size >= NativeAdManager.AD_POOL_SIZE) return
        isLoading = true

        val adLoader =
            AdLoader
                .Builder(appContext, AdUnitIds.nativeAdvanced)
                .forNativeAd { nativeAd ->
                    // The user may have bought remove-ads while this request was in flight:
                    // discard the delivery instead of repopulating the (already destroyed) pool.
                    if (removeAdsManager.hasRemovedAds.value) {
                        nativeAd.destroy()
                        isLoading = false
                        return@forNativeAd
                    }
                    ads.update { (it + nativeAd).takeLast(NativeAdManager.AD_POOL_SIZE) }
                    if (ads.value.size >= NativeAdManager.AD_POOL_SIZE) isLoading = false
                }.withAdListener(
                    object : AdListener() {
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            isLoading = false
                            crashReporter.log("AdMob native failed: ${error.code} ${error.message}")
                        }
                    },
                ).build()

        adLoader.loadAds(AdRequest.Builder().build(), NativeAdManager.AD_POOL_SIZE)
    }

    override fun destroyAds() {
        val current = ads.value
        ads.value = emptyList()
        current.forEach { (it as? NativeAd)?.destroy() }
        isLoading = false
    }
}
