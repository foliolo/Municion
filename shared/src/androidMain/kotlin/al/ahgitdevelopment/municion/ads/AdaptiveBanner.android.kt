package al.ahgitdevelopment.municion.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

// Google's public test banner unit; used when no real unit id is configured.
private const val TEST_BANNER_UNIT = "ca-app-pub-3940256099942544/6300978111"

@Composable
actual fun AdaptiveBanner(adUnitId: String, modifier: Modifier) {
    val unitId = adUnitId.ifBlank { TEST_BANNER_UNIT }
    AndroidView(
        modifier = modifier,
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = unitId
                loadAd(AdRequest.Builder().build())
            }
        },
    )
}
