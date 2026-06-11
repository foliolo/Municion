package al.ahgitdevelopment.municion.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Persistent bottom banner shown above the bottom navigation on every screen except the auth flow.
 * Android renders a lifecycle-aware adaptive AdMob banner; iOS bridges to a Swift `BannerAdViewFactory`.
 * The unit id is resolved per platform via [AdUnitIds.bottomBanner].
 */
@Composable
expect fun AdaptiveBanner(modifier: Modifier = Modifier)
