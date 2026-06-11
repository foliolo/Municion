package al.ahgitdevelopment.municion.ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.useContents
import platform.UIKit.UIScreen

/**
 * Adaptive banner via the Swift [BannerAdViewFactory] bridge. Renders nothing until the bridge is
 * registered from `iOSApp.swift` (after the Google Mobile Ads SPM package is added).
 */
@Composable
actual fun AdaptiveBanner(modifier: Modifier) {
    val factory = BannerAdBridge.factory ?: return
    val screenWidth = remember { UIScreen.mainScreen.bounds.useContents { size.width } }
    val bannerHeight = remember(screenWidth) { factory.getBannerHeight(screenWidth) }

    UIKitView(
        factory = { factory.createBannerView(AdUnitIds.bottomBanner, screenWidth) },
        modifier = modifier.fillMaxWidth().height(bannerHeight.dp),
    )
}
