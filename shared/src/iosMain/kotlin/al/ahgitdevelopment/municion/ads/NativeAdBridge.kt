package al.ahgitdevelopment.municion.ads

import platform.UIKit.UIView

/** Loads native ads through the Swift Google Mobile Ads SDK and hands each `GADNativeAd` back as a handle. */
interface NativeAdLoader {
    fun load(
        adUnitId: String,
        count: Int,
        onLoaded: (handle: NativeAdHandle) -> Unit,
    )

    fun destroy(handles: List<NativeAdHandle>)
}

/** Builds the iOS native ad view (`GADNativeAdView`) for a previously loaded handle. */
interface NativeAdViewFactory {
    fun createNativeAdView(handle: NativeAdHandle): UIView
}

object NativeAdBridge {
    var loader: NativeAdLoader? = null
    var viewFactory: NativeAdViewFactory? = null
}
