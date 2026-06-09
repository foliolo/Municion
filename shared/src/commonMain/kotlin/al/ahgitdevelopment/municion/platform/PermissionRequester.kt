package al.ahgitdevelopment.municion.platform

import androidx.compose.runtime.Composable

/** Runtime permissions Munición asks for on Android (iOS handles these via EventKit/APNs instead). */
enum class AppPermission {
    /** WRITE_CALENDAR — licence-expiry reminders. */
    CALENDAR,

    /** POST_NOTIFICATIONS — FCM notifications (Android 13+). */
    NOTIFICATIONS,
}

/** Reflects whether a permission is granted and lets the UI trigger the system request dialog. */
interface PermissionRequester {
    val isGranted: Boolean

    fun request()
}

/** Used on platforms/versions where the permission is not a runtime concern (iOS, Android < 13). */
object AlwaysGrantedPermissionRequester : PermissionRequester {
    override val isGranted: Boolean = true

    override fun request() = Unit
}

/**
 * Remembers a [PermissionRequester] for [permission]. Android wires the system permission dialog;
 * iOS returns [AlwaysGrantedPermissionRequester] (calendar access is requested by EventKit and push
 * by APNs).
 */
@Composable
expect fun rememberPermissionRequester(permission: AppPermission): PermissionRequester
