package al.ahgitdevelopment.municion.auth

import al.ahgitdevelopment.municion.firebase.CrashReporter
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.OAuthProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

typealias IosSocialAuthCompletion = (String?) -> Unit
typealias IosSocialAuthHandler = (IosSocialAuthCompletion) -> Unit

/** Completion for a token revocation request: the argument is an error message, or null on success. */
typealias IosRevokeCompletion = (String?) -> Unit
typealias IosRevokeHandler = (String, IosRevokeCompletion) -> Unit

object IosGoogleAuthBridge {
    var signInHandler: IosSocialAuthHandler? = null
}

object IosAppleAuthBridge {
    var signInHandler: IosSocialAuthHandler? = null
}

/**
 * Bridge to the native Firebase iOS SDK's `revokeToken(withAuthorizationCode:)`, which the GitLive
 * wrapper does not expose. Wired from Swift in `SocialAuthCoordinator.registerBridges()`.
 */
object IosAppleRevokeBridge {
    var revokeHandler: IosRevokeHandler? = null
}

class IosSocialAuthProvider(
    private val auth: FirebaseAuth,
    private val crashReporter: CrashReporter,
) : SocialAuthProvider {
    override val isGoogleAvailable: Boolean
        get() = IosGoogleAuthBridge.signInHandler != null

    override val isAppleAvailable: Boolean
        get() = IosAppleAuthBridge.signInHandler != null

    override suspend fun signInWithGoogle(): Result<FirebaseUser> =
        runCatching {
            val payload = awaitPayload(IosGoogleAuthBridge.signInHandler, "Google no está configurado.")
            val parts = payload.split(GOOGLE_PAYLOAD_SEPARATOR)
            val idToken = parts.firstOrNull().orEmpty()
            val accessToken = parts.getOrNull(1).orEmpty().ifBlank { null }
            require(idToken.isNotBlank()) { "Google no devolvió un token válido." }

            auth.signInWithCredential(GoogleAuthProvider.credential(idToken, accessToken)).user
                ?: error("Google sign-in failed: user is null")
        }.onFailure { crashReporter.recordException(it) }

    override suspend fun signInWithApple(): Result<FirebaseUser> =
        runCatching {
            val (idToken, rawNonce, _) = awaitApplePayload()
            require(idToken.isNotBlank() && rawNonce.isNotBlank()) { "Apple no devolvió una credencial válida." }

            auth
                .signInWithCredential(appleCredential(idToken, rawNonce))
                .user ?: error("Apple sign-in failed: user is null")
        }.onFailure { crashReporter.recordException(it) }

    override suspend fun reauthenticateAndRevokeApple(): Result<Unit> =
        runCatching {
            val (idToken, rawNonce, authCode) = awaitApplePayload()
            require(idToken.isNotBlank() && rawNonce.isNotBlank()) { "Apple no devolvió una credencial válida." }

            // Fresh re-authentication: Firebase requires a recent login before deleting the user, and
            // this also yields a fresh, single-use authorizationCode (the one from the original
            // sign-in is long expired and cannot be revoked).
            val user = auth.currentUser ?: error("No hay usuario para reautenticar.")
            user.reauthenticate(appleCredential(idToken, rawNonce))

            // Best-effort token revocation (Apple's account-deletion requirement). A failure here must
            // NOT block the account deletion, so it is logged but swallowed.
            if (authCode.isNotBlank()) {
                runCatching { awaitRevoke(authCode) }.onFailure { crashReporter.recordException(it) }
            }
        }.onFailure { crashReporter.recordException(it) }

    private fun appleCredential(
        idToken: String,
        rawNonce: String,
    ) = OAuthProvider.credential(
        providerId = "apple.com",
        accessToken = null,
        idToken = idToken,
        rawNonce = rawNonce,
    )

    /** Triggers a (fresh) Sign in with Apple and returns (idToken, rawNonce, authorizationCode). */
    private suspend fun awaitApplePayload(): Triple<String, String, String> {
        val payload = awaitPayload(IosAppleAuthBridge.signInHandler, "Apple no está configurado.")
        val parts = payload.split(APPLE_PAYLOAD_SEPARATOR)
        return Triple(
            parts.getOrNull(0).orEmpty(),
            parts.getOrNull(1).orEmpty(),
            parts.getOrNull(2).orEmpty(),
        )
    }

    private suspend fun awaitRevoke(authorizationCode: String): Unit =
        suspendCancellableCoroutine { continuation ->
            val handler = IosAppleRevokeBridge.revokeHandler
            if (handler == null) {
                // Revocation bridge not wired (e.g. an older build) — treat as a no-op so the
                // deletion still proceeds.
                continuation.resume(Unit)
                return@suspendCancellableCoroutine
            }
            handler(authorizationCode) { errorMessage ->
                if (!continuation.isActive) return@handler
                if (errorMessage.isNullOrBlank()) {
                    continuation.resume(Unit)
                } else {
                    continuation.resumeWithException(
                        SocialAuthUnavailableException("No se pudo revocar el token de Apple: $errorMessage"),
                    )
                }
            }
        }
}

private suspend fun awaitPayload(
    handler: IosSocialAuthHandler?,
    notConfiguredMessage: String,
): String =
    suspendCancellableCoroutine { continuation ->
        if (handler == null) {
            continuation.resumeWithException(SocialAuthUnavailableException(notConfiguredMessage))
            return@suspendCancellableCoroutine
        }

        handler { payload ->
            if (!continuation.isActive) return@handler
            if (payload.isNullOrBlank()) {
                continuation.resumeWithException(SocialAuthUnavailableException("Inicio de sesión cancelado."))
            } else {
                continuation.resume(payload)
            }
        }
    }

private const val GOOGLE_PAYLOAD_SEPARATOR = "|||accessToken|||"

// Apple payload is positional: "idToken|||rawNonce|||authorizationCode". None of those values can
// contain "|||" (JWTs, the nonce charset and the auth code are all base64url-style), so a plain
// split is safe.
private const val APPLE_PAYLOAD_SEPARATOR = "|||"
