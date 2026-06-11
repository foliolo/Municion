package al.ahgitdevelopment.municion.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberConsentOptions(): ConsentOptionsState {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val manager = remember(context) { GoogleMobileAdsConsentManager.getInstance(context) }
    return ConsentOptionsState(
        isAvailable = activity != null && manager.isPrivacyOptionsRequired,
        show = { activity?.let { manager.showPrivacyOptionsForm(it) } },
    )
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
