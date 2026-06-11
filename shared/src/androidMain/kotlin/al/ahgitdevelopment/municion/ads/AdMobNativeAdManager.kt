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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Loads a pool of native advanced ads via [AdLoader] and exposes it reactively. A single instance is
 * shared (Koin `single`) across the four list screens; [itemsWithNativeAds] cycles the pool.
 */
class AdMobNativeAdManager(
    context: Context,
    private val crashReporter: CrashReporter,
) : NativeAdManager {
    private val appContext = context.applicationContext
    private val ads = MutableStateFlow<List<NativeAdHandle>>(emptyList())
    override val nativeAds: StateFlow<List<NativeAdHandle>> = ads.asStateFlow()

    private var isLoading = false

    @RequiresPermission(Manifest.permission.INTERNET)
    override fun loadAds() {
        if (isLoading || ads.value.size >= NativeAdManager.AD_POOL_SIZE) return
        isLoading = true

        val adLoader =
            AdLoader
                .Builder(appContext, AdUnitIds.nativeAdvanced)
                .forNativeAd { nativeAd ->
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
