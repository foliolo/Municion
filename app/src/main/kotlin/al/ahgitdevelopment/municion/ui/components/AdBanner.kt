package al.ahgitdevelopment.municion.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun AdBanner(
    adUnitId: String,
    modifier: Modifier = Modifier
) {
    // Recreate the AdView when adUnitId changes because setAdUnitId can only be called once.
    key(adUnitId) {
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.FULL_BANNER)
                    this.adUnitId = adUnitId
                    
                    adListener = object : com.google.android.gms.ads.AdListener() {
                        override fun onAdLoaded() {
                            super.onAdLoaded()
                            android.util.Log.d("AdBanner", "Ad loaded successfully for unit: $adUnitId")
                        }

                        override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                            super.onAdFailedToLoad(error)
                            android.util.Log.e("AdBanner", "Ad failed to load for unit: $adUnitId")
                            android.util.Log.e("AdBanner", "Error Code: ${error.code} (3 = NO_FILL)")
                            android.util.Log.e("AdBanner", "Message: ${error.message}")
                            android.util.Log.e("AdBanner", "Domain: ${error.domain}")
                            android.util.Log.e("AdBanner", "Response Info: ${error.responseInfo}")
                        }
                    }

                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}
