import SwiftUI
import FirebaseCore
import FirebaseDatabase
import FirebaseAppCheck
import FirebaseAnalytics

// App Check provider: App Attest (iOS 14+) with a DeviceCheck fallback.
final class MunicionAppCheckProviderFactory: NSObject, AppCheckProviderFactory {
    func createProvider(with app: FirebaseApp) -> AppCheckProvider? {
        if #available(iOS 14.0, *) {
            return AppAttestProvider(app: app)
        } else {
            return DeviceCheckProvider(app: app)
        }
    }
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    init() {
        // App Check factory MUST be set before FirebaseApp.configure().
        #if DEBUG
        AppCheck.setAppCheckProviderFactory(AppCheckDebugProviderFactory())
        #else
        AppCheck.setAppCheckProviderFactory(MunicionAppCheckProviderFactory())
        #endif

        FirebaseApp.configure()

        // Realtime Database offline persistence — must be enabled before the first
        // database reference is taken (GitLive on the Kotlin side wraps this same
        // Database.database() singleton).
        Database.database().isPersistenceEnabled = true

        Analytics.setAnalyticsCollectionEnabled(true)

        // Register the Kotlin↔Swift ad bridges (VC-independent). The consent/ATT/MobileAds.start
        // flow is kicked off later from ContentView.onAppear, once a presenting VC exists.
        // Screenshot mode (fastlane snapshot) skips ads entirely so no consent/ATT dialog or ad
        // content shows up in App Store screenshots. It is a debug-only QA path: release builds
        // never read the launch argument and always initialize ads.
        #if DEBUG
        let isScreenshotMode = ProcessInfo.processInfo.arguments.contains("-screenshotMode")
        #else
        let isScreenshotMode = false
        #endif
        if !isScreenshotMode {
            AdsCoordinator.shared.registerBridges()
        }
        SocialAuthCoordinator.shared.registerBridges()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
