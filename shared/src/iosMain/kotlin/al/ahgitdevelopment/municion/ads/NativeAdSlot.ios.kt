package al.ahgitdevelopment.municion.ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView

// Fixed card height (media 150 + content + paddings); the Swift view lays out to fill it.
private const val NATIVE_AD_HEIGHT_DP = 248

/**
 * Renders a native ad via the Swift [NativeAdViewFactory] bridge. No-op until the bridge is
 * registered from `iOSApp.swift`.
 */
@Composable
actual fun NativeAdSlot(
    adHandle: NativeAdHandle,
    modifier: Modifier,
) {
    val factory = NativeAdBridge.viewFactory ?: return
    UIKitView(
        factory = { factory.createNativeAdView(adHandle) },
        modifier = modifier.fillMaxWidth().height(NATIVE_AD_HEIGHT_DP.dp),
    )
}
