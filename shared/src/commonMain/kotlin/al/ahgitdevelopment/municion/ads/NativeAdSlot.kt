package al.ahgitdevelopment.municion.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Renders a single native advanced ad as a list-style card (icon/media + headline + body + CTA, with
 * the mandatory "Ad" attribution and AdChoices). Android wires a real `NativeAdView`; iOS bridges to
 * a Swift `NativeAdViewFactory`.
 */
@Composable
expect fun NativeAdSlot(
    adHandle: NativeAdHandle,
    modifier: Modifier = Modifier,
)
