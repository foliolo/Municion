package al.ahgitdevelopment.municion.ads

/**
 * Per-platform AdMob unit ids, resolved from the generated common `BuildConfig`. The Android actual
 * reads the `*_ANDROID` fields and the iOS actual reads the `*_IOS` fields, so call sites stay
 * platform-agnostic and neither platform can accidentally use the other's banner/native id.
 */
expect object AdUnitIds {
    val bottomBanner: String

    val nativeAdvanced: String
}
