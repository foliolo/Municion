import UIKit
import GoogleMobileAds
import AppTrackingTransparency
import Shared

/// App-level ads wiring for iOS. Registers the Kotlin↔Swift bridges at launch and, once the UI is on
/// screen, gathers UMP consent → requests ATT → starts the Mobile Ads SDK. The native ad pool and the
/// banner load themselves afterwards (the list screens trigger `NativeAdManager.loadAds`).
final class AdsCoordinator {
    static let shared = AdsCoordinator()
    private var started = false

    /// Bridge registration is VC-independent, so it runs in `iOSApp.init()`.
    func registerBridges() {
        BannerAdBridge.shared.factory = IOSBannerAdViewFactory()
        NativeAdBridge.shared.loader = IOSNativeAdLoader()
        NativeAdBridge.shared.viewFactory = IOSNativeAdViewFactory()
        ConsentBridge.shared.bridge = IOSPrivacyConsentBridge()
    }

    /// Consent/ATT need a presenting view controller, so this runs from `ContentView.onAppear`.
    func startIfNeeded() {
        guard !started else { return }
        started = true
        ConsentCoordinator.gatherConsent { [weak self] canRequestAds in
            guard canRequestAds else { return }
            self?.requestTrackingThenStart()
        }
    }

    private func requestTrackingThenStart() {
        let start = { MobileAds.shared.start(completionHandler: nil) }
        if #available(iOS 14, *) {
            ATTrackingManager.requestTrackingAuthorization { _ in
                DispatchQueue.main.async { start() }
            }
        } else {
            start()
        }
    }
}
