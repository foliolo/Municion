package al.ahgitdevelopment.municion.auth

import android.content.Context
import android.util.Log
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FirebaseAuthRepository - Manejo de Firebase Authentication
 *
 * v3.4.0: Auth Simplification
 * - Login obligatorio con email/password desde el primer uso
 * - Eliminado login anonimo y vinculacion con Google
 * - Mantenido linkWithEmail() para migracion de usuarios anonimos existentes
 * - Migración de usuarios legacy (v2.x)
 *
 * @since v3.0.0 (TRACK B - Auth Modernization)
 * @updated v3.4.0 (Auth Simplification)
 */
@Singleton
class FirebaseAuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val crashlytics: FirebaseCrashlytics
) {
    companion object {
        private const val TAG = "FirebaseAuthRepository"
    }

    /**
     * Obtiene el usuario actual de Firebase
     */
    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    /**
     * Verifica si hay un usuario autenticado
     */
    fun isAuthenticated(): Boolean = firebaseAuth.currentUser != null

    /**
     * Verifica si el usuario es anónimo
     */
    fun isAnonymous(): Boolean = firebaseAuth.currentUser?.isAnonymous == true

    /**
     * Verifica si el usuario tiene cuenta vinculada (no anónima)
     */
    fun isLinked(): Boolean {
        val user = firebaseAuth.currentUser ?: return false
        return !user.isAnonymous
    }

    /**
     * Vincula cuenta anonima existente con Email/Password.
     * Usado para migrar usuarios anonimos existentes a cuentas con email.
     * Mantiene el mismo UID y todos los datos existentes.
     *
     * @param email Email del usuario
     * @param password Contrasena elegida por el usuario
     * @return Result con FirebaseUser si exitoso
     *
     * @since v3.4.0 (Auth Simplification - Migration flow)
     */
    suspend fun linkWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val currentUser = firebaseAuth.currentUser
                ?: return Result.failure(Exception("No user to link"))

            val credential = EmailAuthProvider.getCredential(email, password)
            val result = currentUser.linkWithCredential(credential).await()

            result.user?.let { user ->
                Log.i(TAG, "Email link successful: ${user.email}")
                Result.success(user)
            } ?: Result.failure(Exception("Email link failed: user is null"))
        } catch (e: Exception) {
            Log.e(TAG, "Email link failed", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Login con Email/Password existente.
     *
     * @param email Email del usuario
     * @param password Contrasena del usuario
     * @return Result con FirebaseUser si exitoso
     */
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                Log.i(TAG, "Email sign-in successful: ${user.email}")
                Result.success(user)
            } ?: Result.failure(Exception("Email sign-in failed: user is null"))
        } catch (e: Exception) {
            Log.e(TAG, "Email sign-in failed", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Crea una nueva cuenta con Email/Password
     *
     * @param email Email del nuevo usuario
     * @param password Contrasena elegida (min 6 caracteres)
     * @return Result con FirebaseUser si exitoso
     *
     * @since v3.4.0 (Auth Simplification)
     */
    suspend fun createAccount(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                Log.i(TAG, "Account created: ${user.email}")
                Result.success(user)
            } ?: Result.failure(Exception("Account creation failed: user is null"))
        } catch (e: Exception) {
            Log.e(TAG, "Account creation failed", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Envia email de recuperacion de contrasena
     *
     * @param email Email de la cuenta a recuperar
     * @return Result<Unit> indicando exito o fallo
     *
     * @since v3.4.0 (Auth Simplification)
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Log.i(TAG, "Password reset email sent to: $email")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Password reset email failed", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Migración de usuarios legacy
     * Intenta recuperar cuenta con email+PIN guardados
     *
     * @param email Email del dispositivo (legacy)
     * @param pin PIN guardado como password (legacy)
     * @return MigrationResult indicando éxito o tipo de fallo
     */
    suspend fun migrateFromLegacy(email: String, pin: String): Result<MigrationResult> {
        return try {
            // Intentar login con credenciales legacy
            val signInResult = firebaseAuth.signInWithEmailAndPassword(email, pin).await()

            signInResult.user?.let { user ->
                Log.i(TAG, "Legacy migration successful for: ${user.email}")
                Result.success(MigrationResult.Success(user))
            } ?: Result.success(MigrationResult.Failed("User is null after sign-in"))
        } catch (e: Exception) {
            Log.w(TAG, "Legacy migration failed, user may need manual linking", e)

            // Determinar tipo de fallo
            val failureType = when {
                e.message?.contains("password is invalid", ignoreCase = true) == true ->
                    MigrationResult.InvalidCredentials

                e.message?.contains("no user record", ignoreCase = true) == true ->
                    MigrationResult.UserNotFound

                e.message?.contains("network", ignoreCase = true) == true ->
                    MigrationResult.NetworkError(e.message ?: "Network error")

                else -> MigrationResult.Failed(e.message ?: "Unknown error")
            }

            Result.success(failureType)
        }
    }

    /**
     * Cierra sesion de Firebase.
     */
    fun signOut() {
        firebaseAuth.signOut()
        Log.i(TAG, "User signed out")
    }

    /**
     * Elimina la cuenta de Firebase completamente
     * ADVERTENCIA: Esto elimina todos los datos del usuario
     */
    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser
                ?: return Result.failure(Exception("No user to delete"))

            user.delete().await()
            Log.i(TAG, "Account deleted successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Account deletion failed", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Re-autentica al usuario para operaciones sensibles
     * Necesario antes de eliminar cuenta o cambiar email
     */
    suspend fun reauthenticate(credential: AuthCredential): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser
                ?: return Result.failure(Exception("No user to reauthenticate"))

            user.reauthenticate(credential).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Reauthentication failed", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }
}

/**
 * Resultados de migración de usuarios legacy
 */
sealed class MigrationResult {
    data class Success(val user: FirebaseUser) : MigrationResult()
    object UserNotFound : MigrationResult()
    object InvalidCredentials : MigrationResult()
    data class NetworkError(val message: String) : MigrationResult()
    data class Failed(val message: String) : MigrationResult()

    fun isSuccess(): Boolean = this is Success
}
