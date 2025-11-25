package al.ahgitdevelopment.municion.auth

import android.content.Context
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FirebaseAuthRepository - Manejo correcto de Firebase Authentication
 *
 * FASE 2: Firebase Auth Modernization
 * - Reemplaza código buggy de saveUserInFirebase() en FragmentMainActivity
 * - Login anónimo por defecto (privacidad)
 * - Vinculación opcional de cuenta (Google/Email)
 * - Migración de usuarios legacy
 * - Manejo correcto de errores
 *
 * @since v3.0.0 (TRACK B - Auth Modernization)
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
     * Login anónimo - Predeterminado para nuevos usuarios
     * No requiere datos personales, preserva privacidad
     */
    suspend fun signInAnonymously(): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInAnonymously().await()
            result.user?.let { user ->
                android.util.Log.i(TAG, "Anonymous sign-in successful: ${user.uid}")
                Result.success(user)
            } ?: Result.failure(Exception("Anonymous sign-in failed: user is null"))
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Anonymous sign-in failed", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Vincula cuenta anónima con Google
     * Permite sincronización multi-dispositivo
     *
     * @param idToken Token de Google Sign-In
     */
    suspend fun linkWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val currentUser = firebaseAuth.currentUser
                ?: return Result.failure(Exception("No user to link"))

            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = currentUser.linkWithCredential(credential).await()

            result.user?.let { user ->
                android.util.Log.i(TAG, "Google link successful: ${user.email}")
                Result.success(user)
            } ?: Result.failure(Exception("Google link failed: user is null"))
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Google link failed", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Vincula cuenta anónima con Email/Password
     * Alternativa a Google para usuarios que prefieren email
     *
     * @param email Email del usuario
     * @param password Contraseña elegida por el usuario
     */
    suspend fun linkWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val currentUser = firebaseAuth.currentUser
                ?: return Result.failure(Exception("No user to link"))

            val credential = EmailAuthProvider.getCredential(email, password)
            val result = currentUser.linkWithCredential(credential).await()

            result.user?.let { user ->
                android.util.Log.i(TAG, "Email link successful: ${user.email}")
                Result.success(user)
            } ?: Result.failure(Exception("Email link failed: user is null"))
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Email link failed", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Login con credencial existente (Google o Email)
     * Para usuarios que ya tienen cuenta vinculada
     */
    suspend fun signInWithCredential(credential: AuthCredential): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithCredential(credential).await()
            result.user?.let { user ->
                android.util.Log.i(TAG, "Sign-in with credential successful: ${user.uid}")
                Result.success(user)
            } ?: Result.failure(Exception("Sign-in failed: user is null"))
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Sign-in with credential failed", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Login con Email/Password existente
     */
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                android.util.Log.i(TAG, "Email sign-in successful: ${user.email}")
                Result.success(user)
            } ?: Result.failure(Exception("Email sign-in failed: user is null"))
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Email sign-in failed", e)
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
                android.util.Log.i(TAG, "Legacy migration successful for: ${user.email}")
                Result.success(MigrationResult.Success(user))
            } ?: Result.success(MigrationResult.Failed("User is null after sign-in"))
        } catch (e: Exception) {
            android.util.Log.w(TAG, "Legacy migration failed, user may need manual linking", e)

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
     * Cierra sesión de Firebase
     * NOTA: Si el usuario es anónimo, sus datos se perderán
     */
    fun signOut() {
        val wasAnonymous = isAnonymous()
        firebaseAuth.signOut()

        if (wasAnonymous) {
            android.util.Log.w(TAG, "Anonymous user signed out - data may be lost")
        }
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
            android.util.Log.i(TAG, "Account deleted successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Account deletion failed", e)
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
            android.util.Log.e(TAG, "Reauthentication failed", e)
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
