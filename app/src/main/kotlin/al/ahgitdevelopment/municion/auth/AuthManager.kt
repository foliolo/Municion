package al.ahgitdevelopment.municion.auth

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AuthManager - Gestión centralizada de autenticación local
 *
 * FASE 1: Sistema moderno de autenticación
 * - PIN encriptado con AndroidKeyStore (no plain text)
 * - BiometricPrompt para huella/face ID
 * - Sesión local segura
 * - Reemplaza SharedPreferences inseguro de LoginPasswordActivity
 *
 * @since v3.0.0 (TRACK B - Auth Modernization)
 */
@Singleton
class AuthManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "secure_auth_prefs"
        private const val KEY_PIN_ENCRYPTED = "pin_encrypted"
        private const val KEY_PIN_IV = "pin_iv"
        private const val KEY_HAS_PIN = "has_pin"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_SESSION_ACTIVE = "session_active"
        private const val KEYSTORE_ALIAS = "municion_pin_key"
        const val MIN_PIN_LENGTH = 6
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val biometricManager = BiometricManager.from(context)

    /**
     * Verifica si el usuario tiene un PIN configurado
     */
    fun hasPinConfigured(): Boolean {
        return encryptedPrefs.getBoolean(KEY_HAS_PIN, false)
    }

    /**
     * Verifica si hay una sesión activa (usuario autenticado)
     */
    fun hasActiveSession(): Boolean {
        return encryptedPrefs.getBoolean(KEY_SESSION_ACTIVE, false)
    }

    /**
     * Configura un nuevo PIN para el usuario
     * Encripta el PIN usando AndroidKeyStore
     *
     * @param pin PIN de al menos 6 dígitos
     * @return Result indicando éxito o error
     */
    fun setupPin(pin: String): Result<Unit> {
        return try {
            // Validación
            if (pin.length < MIN_PIN_LENGTH) {
                return Result.failure(
                    IllegalArgumentException("PIN debe tener al menos $MIN_PIN_LENGTH caracteres")
                )
            }

            if (!pin.all { it.isDigit() }) {
                return Result.failure(
                    IllegalArgumentException("PIN solo puede contener dígitos")
                )
            }

            // Encriptar PIN con KeyStore
            val (encryptedPin, iv) = encryptPin(pin)

            // Guardar en EncryptedSharedPreferences
            encryptedPrefs.edit().apply {
                putString(KEY_PIN_ENCRYPTED, encryptedPin)
                putString(KEY_PIN_IV, iv)
                putBoolean(KEY_HAS_PIN, true)
                apply()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("AuthManager", "Error setting up PIN", e)
            Result.failure(e)
        }
    }

    /**
     * Verifica un PIN contra el guardado
     *
     * @param pin PIN a verificar
     * @return true si el PIN es correcto, false en caso contrario
     */
    fun verifyPin(pin: String): Boolean {
        return try {
            val encryptedPin = encryptedPrefs.getString(KEY_PIN_ENCRYPTED, null) ?: return false
            val iv = encryptedPrefs.getString(KEY_PIN_IV, null) ?: return false

            val decryptedPin = decryptPin(encryptedPin, iv)
            val isValid = decryptedPin == pin

            // Si el PIN es válido, activar sesión
            if (isValid) {
                activateSession()
            }

            isValid
        } catch (e: Exception) {
            android.util.Log.e("AuthManager", "Error verifying PIN", e)
            false
        }
    }

    /**
     * Cambia el PIN del usuario
     * Requiere el PIN actual para autorizar el cambio
     *
     * @param currentPin PIN actual
     * @param newPin Nuevo PIN
     * @return Result indicando éxito o error
     */
    fun changePin(currentPin: String, newPin: String): Result<Unit> {
        return if (verifyPin(currentPin)) {
            setupPin(newPin)
        } else {
            Result.failure(IllegalArgumentException("PIN actual incorrecto"))
        }
    }

    /**
     * Activa una sesión local (usuario autenticado correctamente)
     */
    private fun activateSession() {
        encryptedPrefs.edit().putBoolean(KEY_SESSION_ACTIVE, true).apply()
    }

    /**
     * Cierra la sesión local
     */
    fun closeSession() {
        encryptedPrefs.edit().putBoolean(KEY_SESSION_ACTIVE, false).apply()
    }

    /**
     * Verifica si el dispositivo soporta autenticación biométrica
     */
    fun canAuthenticateWithBiometrics(): BiometricAvailability {
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                BiometricAvailability.Available

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                BiometricAvailability.NoHardware

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                BiometricAvailability.HardwareUnavailable

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                BiometricAvailability.NotEnrolled

            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED ->
                BiometricAvailability.SecurityUpdateRequired

            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED ->
                BiometricAvailability.Unsupported

            BiometricManager.BIOMETRIC_STATUS_UNKNOWN ->
                BiometricAvailability.Unknown

            else -> BiometricAvailability.Unknown
        }
    }

    /**
     * Verifica si la autenticación biométrica está habilitada
     */
    fun isBiometricEnabled(): Boolean {
        return encryptedPrefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    /**
     * Habilita/deshabilita la autenticación biométrica
     */
    fun setBiometricEnabled(enabled: Boolean) {
        encryptedPrefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    /**
     * Autentica con BiometricPrompt
     * Retorna un Flow que emite el resultado de la autenticación
     *
     * @param activity FragmentActivity para mostrar el prompt
     * @return Flow con el resultado de la autenticación biométrica
     */
    fun authenticateWithBiometrics(
        activity: FragmentActivity,
        title: String = "Autenticación",
        subtitle: String = "Usa tu huella o Face ID",
        negativeButtonText: String = "Usar PIN"
    ): Flow<BiometricResult> = callbackFlow {
        val executor = ContextCompat.getMainExecutor(context)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    activateSession()
                    trySend(BiometricResult.Success)
                    close()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    trySend(BiometricResult.Failed)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    trySend(BiometricResult.Error(errorCode, errString.toString()))
                    close()
                }
            })

        biometricPrompt.authenticate(promptInfo)

        awaitClose {
            // Cleanup if needed
        }
    }

    /**
     * Resetea completamente el PIN (para recuperación o testing)
     * ADVERTENCIA: Esto elimina el PIN guardado
     */
    fun resetPin() {
        encryptedPrefs.edit().apply {
            remove(KEY_PIN_ENCRYPTED)
            remove(KEY_PIN_IV)
            remove(KEY_HAS_PIN)
            remove(KEY_SESSION_ACTIVE)
            apply()
        }

        // Eliminar key del KeyStore
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            keyStore.deleteEntry(KEYSTORE_ALIAS)
        } catch (e: Exception) {
            android.util.Log.e("AuthManager", "Error deleting keystore entry", e)
        }
    }

    // ==================== PRIVATE ENCRYPTION METHODS ====================

    /**
     * Encripta el PIN usando AndroidKeyStore
     * Retorna el PIN encriptado y el IV usado
     */
    private fun encryptPin(pin: String): Pair<String, String> {
        val cipher = getCipher()
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())

        val encryptedBytes = cipher.doFinal(pin.toByteArray(StandardCharsets.UTF_8))
        val iv = cipher.iv

        val encryptedPinBase64 = Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        val ivBase64 = Base64.encodeToString(iv, Base64.DEFAULT)

        return Pair(encryptedPinBase64, ivBase64)
    }

    /**
     * Desencripta el PIN usando AndroidKeyStore
     */
    private fun decryptPin(encryptedPinBase64: String, ivBase64: String): String {
        val cipher = getCipher()
        val iv = Base64.decode(ivBase64, Base64.DEFAULT)
        val spec = IvParameterSpec(iv)

        cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), spec)

        val encryptedBytes = Base64.decode(encryptedPinBase64, Base64.DEFAULT)
        val decryptedBytes = cipher.doFinal(encryptedBytes)

        return String(decryptedBytes, StandardCharsets.UTF_8)
    }

    /**
     * Obtiene o crea la SecretKey en AndroidKeyStore
     */
    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        keyStore.getKey(KEYSTORE_ALIAS, null)?.let {
            return it as SecretKey
        }

        // Crear nueva key
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )

        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .setUserAuthenticationRequired(false)
            .setRandomizedEncryptionRequired(true)
            .build()

        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }

    /**
     * Obtiene instancia de Cipher para encriptación AES
     */
    private fun getCipher(): Cipher {
        return Cipher.getInstance(
            "${KeyProperties.KEY_ALGORITHM_AES}/" +
                    "${KeyProperties.BLOCK_MODE_CBC}/" +
                    KeyProperties.ENCRYPTION_PADDING_PKCS7
        )
    }
}

/**
 * Estados de disponibilidad de autenticación biométrica
 */
sealed class BiometricAvailability {
    object Available : BiometricAvailability()
    object NoHardware : BiometricAvailability()
    object HardwareUnavailable : BiometricAvailability()
    object NotEnrolled : BiometricAvailability()
    object SecurityUpdateRequired : BiometricAvailability()
    object Unsupported : BiometricAvailability()
    object Unknown : BiometricAvailability()

    fun isAvailable(): Boolean = this is Available
}

/**
 * Resultados de autenticación biométrica
 */
sealed class BiometricResult {
    object Success : BiometricResult()
    object Failed : BiometricResult()
    data class Error(val errorCode: Int, val message: String) : BiometricResult()
}
