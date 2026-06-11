package al.ahgitdevelopment.municion.ads

/**
 * Opaque handle to a platform native ad object — Android `com.google.android.gms.ads.nativead.NativeAd`,
 * iOS `GADNativeAd` (carried across the Swift bridge as `Any`). Common code never inspects it; it only
 * holds the pool and hands each handle back to [NativeAdSlot] for rendering.
 */
typealias NativeAdHandle = Any
