package al.ahgitdevelopment.municion

import android.app.Application
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

/**
 * Debug variant: the Debug provider emits a debug token to Logcat (tag `DebugAppCheckProvider`,
 * "Enter this debug secret into the allow list..."). Copy that token into
 * Firebase Console → App Check → Apps → (Android app) → ⋮ → Manage debug tokens, so this
 * device/emulator passes App Check enforcement during development.
 */
fun Application.installAppCheck() {
    FirebaseAppCheck.getInstance()
        .installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
}
