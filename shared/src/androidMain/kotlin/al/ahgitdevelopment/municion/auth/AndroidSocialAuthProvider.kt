package al.ahgitdevelopment.municion.auth

import al.ahgitdevelopment.municion.BuildConfig
import al.ahgitdevelopment.municion.firebase.CrashReporter
import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import java.lang.ref.WeakReference

object SocialAuthActivityHolder {
    private var activityReference: WeakReference<Activity>? = null

    fun set(activity: Activity) {
        activityReference = WeakReference(activity)
    }

    fun clear(activity: Activity) {
        if (activityReference?.get() === activity) {
            activityReference = null
        }
    }

    fun requireActivity(): Activity =
        activityReference?.get()
            ?: throw SocialAuthUnavailableException("No se pudo abrir el inicio de sesión. Vuelve a intentarlo.")
}

class AndroidSocialAuthProvider(
    context: Context,
    private val auth: FirebaseAuth,
    private val crashReporter: CrashReporter,
) : SocialAuthProvider {
    private val credentialManager = CredentialManager.create(context)

    override val isGoogleAvailable: Boolean
        get() = BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()

    override val isAppleAvailable: Boolean = false

    override suspend fun signInWithGoogle(): Result<FirebaseUser> {
        if (!isGoogleAvailable) {
            return Result.failure(SocialAuthUnavailableException("Google no está configurado."))
        }

        return runCatching {
            val idToken = requestGoogleIdToken()
            auth.signInWithCredential(GoogleAuthProvider.credential(idToken, null)).user
                ?: error("Google sign-in failed: user is null")
        }.onFailure { error ->
            if (error !is GetCredentialCancellationException) {
                crashReporter.recordException(error)
            }
        }
    }

    override suspend fun signInWithApple(): Result<FirebaseUser> =
        Result.failure(SocialAuthUnavailableException("Apple solo está disponible en iOS."))

    override suspend fun reauthenticateAndRevokeApple(): Result<Unit> =
        Result.failure(SocialAuthUnavailableException("Apple solo está disponible en iOS."))

    private suspend fun requestGoogleIdToken(): String {
        val activity = SocialAuthActivityHolder.requireActivity()
        val googleIdOption =
            GetGoogleIdOption
                .Builder()
                .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)
                .build()
        val request =
            GetCredentialRequest
                .Builder()
                .addCredentialOption(googleIdOption)
                .build()

        return try {
            requestGoogleIdToken(activity, request)
        } catch (_: NoCredentialException) {
            requestExplicitGoogleIdToken(activity)
        }
    }

    private suspend fun requestExplicitGoogleIdToken(activity: Activity): String {
        val googleOption =
            GetSignInWithGoogleOption
                .Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .build()
        val request =
            GetCredentialRequest
                .Builder()
                .addCredentialOption(googleOption)
                .build()
        return requestGoogleIdToken(activity, request)
    }

    private suspend fun requestGoogleIdToken(
        activity: Activity,
        request: GetCredentialRequest,
    ): String {
        val credential = credentialManager.getCredential(context = activity, request = request).credential
        if (
            credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            return GoogleIdTokenCredential.createFrom(credential.data).idToken
        }
        throw SocialAuthUnavailableException("No se pudo obtener una credencial válida de Google.")
    }
}
