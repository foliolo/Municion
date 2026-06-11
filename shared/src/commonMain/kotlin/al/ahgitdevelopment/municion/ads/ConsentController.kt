package al.ahgitdevelopment.municion.ads

import androidx.compose.runtime.Composable

/**
 * Remembers the consent / privacy-options entry point for the current platform. Android bridges to
 * the UMP [GoogleMobileAdsConsentManager]; iOS bridges to the Swift UMP implementation. Returns a
 * disabled [ConsentOptionsState] (never available) where consent management is not wired.
 */
@Composable
expect fun rememberConsentOptions(): ConsentOptionsState
