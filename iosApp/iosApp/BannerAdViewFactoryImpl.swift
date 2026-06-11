import UIKit
import GoogleMobileAds
import Shared

/// Swift implementation of the Kotlin `BannerAdViewFactory` bridge: builds an adaptive `BannerView`
/// for the Compose `AdaptiveBanner` interop on iOS.
final class IOSBannerAdViewFactory: NSObject, BannerAdViewFactory {
    private var delegates: [BannerAdDelegate] = []

    func createBannerView(adUnitId: String, width: Double) -> UIView {
        let adSize = currentOrientationAnchoredAdaptiveBanner(width: CGFloat(width))
        let banner = BannerView(adSize: adSize)
        banner.adUnitID = adUnitId
        banner.rootViewController = currentRootViewController()
        let delegate = BannerAdDelegate()
        delegates.append(delegate)
        banner.delegate = delegate
        banner.load(Request())
        return banner
    }

    func getBannerHeight(width: Double) -> Double {
        Double(currentOrientationAnchoredAdaptiveBanner(width: CGFloat(width)).size.height)
    }
}

private final class BannerAdDelegate: NSObject, BannerViewDelegate {
    func bannerView(_ bannerView: BannerView, didFailToReceiveAdWithError error: Error) {
        // Banner simply stays empty on failure; nothing to surface to the user.
    }
}
