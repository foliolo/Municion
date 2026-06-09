package al.ahgitdevelopment.municion.platform

import androidx.compose.runtime.Composable

// iOS requests calendar access through EventKit (IosCalendarManager) and notifications through APNs,
// so there is no Compose-level runtime permission to manage here.
@Composable
actual fun rememberPermissionRequester(permission: AppPermission): PermissionRequester = AlwaysGrantedPermissionRequester
