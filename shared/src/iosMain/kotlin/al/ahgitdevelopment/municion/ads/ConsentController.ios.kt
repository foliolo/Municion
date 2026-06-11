package al.ahgitdevelopment.municion.ads

import androidx.compose.runtime.Composable

@Composable
actual fun rememberConsentOptions(): ConsentOptionsState {
    val bridge = ConsentBridge.bridge
    return ConsentOptionsState(
        isAvailable = bridge?.isPrivacyOptionsRequired == true,
        show = { bridge?.showPrivacyOptionsForm() },
    )
}
