package al.ahgitdevelopment.municion.ads

import al.ahgitdevelopment.municion.firebase.CrashReporter
import android.app.Activity
import com.google.android.gms.ads.MobileAds
import org.koin.mp.KoinPlatform
import java.util.concurrent.atomic.AtomicBoolean

/**
 * App-startup ads wiring (called from MainActivity.onCreate — UMP needs an Activity).
 *
 * Flow: skip entirely if the user removed ads → gather UMP consent → once ads can be requested,
 * initialize MobileAds (once). The banner and the native ad pool load themselves afterwards (the
 * list screens trigger [NativeAdManager.loadAds] once they compose, by which point MobileAds is up).
 */
object AdsBootstrap {
    private val initialized = AtomicBoolean(false)

    fun gatherConsentAndInitializeAds(activity: Activity) {
        val koin = KoinPlatform.getKoin()
        val removeAds = koin.get<RemoveAdsManager>()
        if (removeAds.hasRemovedAds.value) return

        val crashReporter = koin.get<CrashReporter>()
        val consentManager = GoogleMobileAdsConsentManager.getInstance(activity)
        consentManager.gatherConsent(activity) { error ->
            error?.let { crashReporter.log("UMP consent error: ${it.errorCode} ${it.message}") }
            if (consentManager.canRequestAds && initialized.compareAndSet(false, true)) {
                MobileAds.initialize(activity) { _ -> }
            }
        }
    }
}
