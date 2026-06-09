package al.ahgitdevelopment.municion.platform

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
actual fun rememberPermissionRequester(permission: AppPermission): PermissionRequester {
    val manifestPermission =
        permission.manifestPermission() ?: return AlwaysGrantedPermissionRequester
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, manifestPermission) == PackageManager.PERMISSION_GRANTED,
        )
    }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            granted = result
        }
    return remember(granted) {
        object : PermissionRequester {
            override val isGranted: Boolean = granted

            override fun request() {
                if (!granted) launcher.launch(manifestPermission)
            }
        }
    }
}

/** Manifest permission string, or null when it is not a runtime permission on this API level. */
private fun AppPermission.manifestPermission(): String? =
    when (this) {
        AppPermission.CALENDAR -> Manifest.permission.WRITE_CALENDAR
        AppPermission.NOTIFICATIONS ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.POST_NOTIFICATIONS
            } else {
                null // Pre-Android 13: notifications are allowed without a runtime grant.
            }
    }
