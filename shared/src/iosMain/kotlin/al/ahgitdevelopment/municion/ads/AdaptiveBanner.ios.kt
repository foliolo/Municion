package al.ahgitdevelopment.municion.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * No-op until the iOS banner is wired through a Swift `BannerAdViewFactory` bridge (GADBannerView
 * via the GoogleMobileAds SPM package added in Xcode) — see MIGRATION_REPORT phase 7.
 */
@Composable
actual fun AdaptiveBanner(
    adUnitId: String,
    modifier: Modifier,
) {
    // Intentionally empty on iOS for now.
}
