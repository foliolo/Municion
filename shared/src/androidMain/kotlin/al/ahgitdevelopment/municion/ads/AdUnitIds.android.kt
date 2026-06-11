package al.ahgitdevelopment.municion.ads

import al.ahgitdevelopment.municion.BuildConfig

// Google's official test unit ids. Used in non-release builds so development never requests live ads
// against the real unit ids (AdMob policy). Release reads the configured ids from BuildConfig.
private const val TEST_BANNER_UNIT = "ca-app-pub-3940256099942544/6300978111"
private const val TEST_NATIVE_UNIT = "ca-app-pub-3940256099942544/2247696110"

actual object AdUnitIds {
    private val isRelease = BuildConfig.BUILD_TYPE == "release"

    actual val bottomBanner: String =
        if (isRelease) BuildConfig.ADMOB_BOTTOM_BANNER_ID_ANDROID else TEST_BANNER_UNIT

    actual val nativeAdvanced: String =
        if (isRelease) BuildConfig.ADMOB_NATIVE_ADVANCED_ID_ANDROID else TEST_NATIVE_UNIT
}
