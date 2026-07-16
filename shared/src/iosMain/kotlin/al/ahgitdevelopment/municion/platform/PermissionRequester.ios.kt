package al.ahgitdevelopment.municion.platform

import androidx.compose.runtime.Composable
import platform.UIKit.UIApplication
import platform.UIKit.registerForRemoteNotifications
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

// Calendar access is requested on demand by EventKit (IosCalendarManager); notifications are
// requested here, right after authentication (see MainScreen).
@Composable
actual fun rememberPermissionRequester(permission: AppPermission): PermissionRequester =
    when (permission) {
        AppPermission.CALENDAR -> AlwaysGrantedPermissionRequester
        AppPermission.NOTIFICATIONS -> IosPushPermissionRequester
    }

/**
 * Requests the iOS notification permission and, once granted, registers with APNs so Firebase
 * Messaging receives the device token (handled in AppDelegate.swift).
 *
 * [isGranted] is reported as `false` because iOS exposes the authorization status asynchronously;
 * this makes MainScreen call [request] after login. [request] is safe to call repeatedly — iOS
 * shows the system prompt only the first time and returns the existing decision afterwards.
 */
private object IosPushPermissionRequester : PermissionRequester {
    override val isGranted: Boolean = false

    override fun request() {
        val options = UNAuthorizationOptionAlert or UNAuthorizationOptionBadge or UNAuthorizationOptionSound
        UNUserNotificationCenter
            .currentNotificationCenter()
            .requestAuthorizationWithOptions(options) { granted, _ ->
                if (granted) {
                    dispatch_async(dispatch_get_main_queue()) {
                        UIApplication.sharedApplication.registerForRemoteNotifications()
                    }
                }
            }
    }
}
