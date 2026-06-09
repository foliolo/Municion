package al.ahgitdevelopment.municion.auth

import al.ahgitdevelopment.municion.firebase.CrashReporter
import dev.gitlive.firebase.auth.AuthCredential
import dev.gitlive.firebase.auth.EmailAuthProvider
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser

/**
 * Firebase Authentication wrapper (GitLive). Mandatory email/password since v3.4; anonymous
 * users can still link to an email, and v2.x users can recover via [migrateFromLegacy].
 */
class FirebaseAuthRepository(
    private val auth: FirebaseAuth,
    private val crashReporter: CrashReporter,
) {
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun isAuthenticated(): Boolean = auth.currentUser != null

    fun isAnonymous(): Boolean = auth.currentUser?.isAnonymous == true

    fun isLinked(): Boolean = auth.currentUser?.let { !it.isAnonymous } ?: false

    /** Links the current anonymous account to email/password, keeping the same uid and data. */
    suspend fun linkWithEmail(
        email: String,
        password: String,
    ): Result<FirebaseUser> =
        runCatching {
            val user = auth.currentUser ?: error("No user to link")
            val credential = EmailAuthProvider.credential(email, password)
            user.linkWithCredential(credential).user ?: error("Email link failed: user is null")
        }.onFailure { crashReporter.recordException(it) }

    suspend fun signInWithEmail(
        email: String,
        password: String,
    ): Result<FirebaseUser> =
        runCatching {
            auth.signInWithEmailAndPassword(email, password).user ?: error("Email sign-in failed: user is null")
        }.onFailure { crashReporter.recordException(it) }

    suspend fun createAccount(
        email: String,
        password: String,
    ): Result<FirebaseUser> =
        runCatching {
            auth.createUserWithEmailAndPassword(email, password).user ?: error("Account creation failed: user is null")
        }.onFailure { crashReporter.recordException(it) }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> =
        runCatching {
            auth.sendPasswordResetEmail(email)
        }.onFailure { crashReporter.recordException(it) }

    /** Attempts to recover a legacy (v2.x) account using the stored email + PIN as password. */
    suspend fun migrateFromLegacy(
        email: String,
        pin: String,
    ): Result<MigrationResult> =
        try {
            val user = auth.signInWithEmailAndPassword(email, pin).user
            if (user != null) {
                Result.success(MigrationResult.Success(user))
            } else {
                Result.success(MigrationResult.Failed("User is null after sign-in"))
            }
        } catch (e: Exception) {
            val msg = e.message.orEmpty()
            val failure =
                when {
                    msg.contains("password is invalid", ignoreCase = true) ||
                        msg.contains("credential is incorrect", ignoreCase = true) -> MigrationResult.InvalidCredentials
                    msg.contains("no user record", ignoreCase = true) -> MigrationResult.UserNotFound
                    msg.contains("network", ignoreCase = true) -> MigrationResult.NetworkError(msg)
                    else -> MigrationResult.Failed(msg.ifBlank { "Unknown error" })
                }
            Result.success(failure)
        }

    suspend fun signOut() {
        auth.signOut()
    }

    suspend fun deleteAccount(): Result<Unit> =
        runCatching {
            (auth.currentUser ?: error("No user to delete")).delete()
        }.onFailure { crashReporter.recordException(it) }

    /** Re-authenticates before sensitive operations (delete account, change email). */
    suspend fun reauthenticate(credential: AuthCredential): Result<Unit> =
        runCatching {
            (auth.currentUser ?: error("No user to reauthenticate")).reauthenticate(credential)
        }.onFailure { crashReporter.recordException(it) }
}

/** Legacy (v2.x) migration outcomes. */
sealed class MigrationResult {
    data class Success(
        val user: FirebaseUser,
    ) : MigrationResult()

    data object UserNotFound : MigrationResult()

    data object InvalidCredentials : MigrationResult()

    data class NetworkError(
        val message: String,
    ) : MigrationResult()

    data class Failed(
        val message: String,
    ) : MigrationResult()

    fun isSuccess(): Boolean = this is Success
}
