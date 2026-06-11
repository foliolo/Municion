package al.ahgitdevelopment.municion.ads

import al.ahgitdevelopment.municion.firebase.CrashReporter
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import org.koin.compose.koinInject

/**
 * Lifecycle-aware adaptive anchored banner. Sized to the current screen width, resumed/paused with
 * the host lifecycle and destroyed on dispose. Load failures are logged to Crashlytics.
 */
@Composable
actual fun AdaptiveBanner(modifier: Modifier) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val crashReporter = koinInject<CrashReporter>()
    val widthDp = configuration.screenWidthDp

    val adView =
        remember(widthDp) {
            AdView(context).apply {
                setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp))
                adUnitId = AdUnitIds.bottomBanner
                adListener =
                    object : AdListener() {
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            crashReporter.log("AdMob banner failed: ${error.code} ${error.message}")
                        }
                    }
                loadAd(AdRequest.Builder().build())
            }
        }

    DisposableEffect(lifecycleOwner, adView) {
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> adView.resume()
                    Lifecycle.Event.ON_PAUSE -> adView.pause()
                    else -> Unit
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            adView.destroy()
        }
    }

    AndroidView(modifier = modifier.fillMaxWidth(), factory = { adView })
}
