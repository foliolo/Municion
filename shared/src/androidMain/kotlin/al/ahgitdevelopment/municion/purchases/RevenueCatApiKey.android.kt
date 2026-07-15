package al.ahgitdevelopment.municion.purchases

import al.ahgitdevelopment.municion.shared.BuildConfig

// Play Store key. Debug builds prefer the sandbox/test key when one is configured.
actual fun revenueCatApiKey(): String {
    val isRelease = BuildConfig.BUILD_TYPE.equals("release", ignoreCase = true)
    val key = if (isRelease) BuildConfig.REVENUECAT_PLAY_SDK_KEY else BuildConfig.REVENUECAT_PLAY_SDK_KEY_TEST
    return key.ifBlank { BuildConfig.REVENUECAT_PLAY_SDK_KEY }
}
