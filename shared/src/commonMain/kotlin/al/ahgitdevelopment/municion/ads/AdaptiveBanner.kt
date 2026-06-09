package al.ahgitdevelopment.municion.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Bottom banner ad. Android renders a real AdMob AdView; iOS is a no-op until the Swift bridge. */
@Composable
expect fun AdaptiveBanner(adUnitId: String, modifier: Modifier = Modifier)
