package al.ahgitdevelopment.municion.ads

import platform.UIKit.UIView

/**
 * Swift-implemented factory for the iOS banner. Registered from `iOSApp.swift` before `MobileAds`
 * starts; until then [BannerAdBridge.factory] is null and [AdaptiveBanner] renders nothing.
 */
interface BannerAdViewFactory {
    fun createBannerView(
        adUnitId: String,
        width: Double,
    ): UIView

    fun getBannerHeight(width: Double): Double
}

object BannerAdBridge {
    var factory: BannerAdViewFactory? = null
}
