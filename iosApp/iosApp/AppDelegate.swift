import UIKit
import FirebaseMessaging
import UserNotifications

/// iOS push wiring, attached via `@UIApplicationDelegateAdaptor` in `iOSApp`.
///
/// The permission prompt and APNs registration are triggered AFTER login from the shared module
/// (`PermissionRequester.ios.kt`: `requestAuthorization` + `registerForRemoteNotifications`). This
/// delegate only reacts to the resulting system callbacks: it feeds the APNs token to Firebase
/// Messaging and displays notifications.
///
/// Ordering: Firebase is configured in `iOSApp.init()` (App Check + `FirebaseApp.configure`). We do
/// NOT touch `Messaging` at launch — the first `Messaging` call happens in
/// `didRegisterForRemoteNotificationsWithDeviceToken`, which can only fire after the post-login
/// `registerForRemoteNotifications()`, so Firebase is guaranteed to be configured by then. Only the
/// (Firebase-independent) `UNUserNotificationCenter` delegate is set at launch.
final class AppDelegate: NSObject, UIApplicationDelegate, MessagingDelegate, UNUserNotificationCenterDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        return true
    }

    // APNs device token -> Firebase Messaging. Reached only post-login, so Firebase is configured.
    func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        Messaging.messaging().delegate = self
        Messaging.messaging().apnsToken = deviceToken
    }

    func application(
        _ application: UIApplication,
        didFailToRegisterForRemoteNotificationsWithError error: Error
    ) {
        NSLog("APNs registration failed: %@", error.localizedDescription)
    }

    // FCM registration token. Console/topic campaigns don't need it server-side; logged for testing.
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        NSLog("FCM registration token: %@", fcmToken ?? "nil")
    }

    // Show notifications while the app is in the foreground.
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([.banner, .badge, .sound])
    }

    // Handle a tap on a delivered notification.
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        completionHandler()
    }
}
