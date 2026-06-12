package al.ahgitdevelopment.municion.auth

import dev.gitlive.firebase.auth.FirebaseUser

enum class SocialLoginProvider {
    Google,
    Apple,
}

data class SocialLoginAvailability(
    val google: Boolean,
    val apple: Boolean,
) {
    val hasAnyProvider: Boolean = google || apple
}

interface SocialAuthProvider {
    val isGoogleAvailable: Boolean
    val isAppleAvailable: Boolean

    suspend fun signInWithGoogle(): Result<FirebaseUser>

    suspend fun signInWithApple(): Result<FirebaseUser>

    /**
     * iOS only: re-authenticates the current Apple user (a fresh Sign in with Apple) and revokes
     * their Apple token. Required before deleting an account created with Sign in with Apple —
     * Firebase needs a recent login to delete, and Apple requires the token to be revoked.
     * Reauthentication must succeed; revocation itself is best-effort. On Android this fails fast
     * (Apple sign-in is iOS only).
     */
    suspend fun reauthenticateAndRevokeApple(): Result<Unit>
}

class SocialAuthUnavailableException(
    message: String,
) : IllegalStateException(message)
