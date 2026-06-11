package al.ahgitdevelopment.municion

import android.app.Application
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

/**
 * Release variant: attest the app with Play Integrity. Requires the app's release SHA-256
 * registered in the Firebase project and distribution through Google Play (or internal testing),
 * plus the Play Integrity API enabled in the Google Cloud project.
 */
fun Application.installAppCheck() {
    FirebaseAppCheck.getInstance()
        .installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance())
}
