package al.ahgitdevelopment.municion.auth

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.databinding.ActivityLoginModernBinding
import al.ahgitdevelopment.municion.ui.MainActivity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * LoginActivity - Pantalla de autenticación moderna
 *
 * FASE 1: Migración de LoginPasswordActivity.java
 * - Material 3 UI
 * - BiometricPrompt (huella/Face ID)
 * - PIN encriptado con AndroidKeyStore (vía AuthManager)
 * - Migración automática de PIN legacy desde SharedPreferences
 * - Firebase Analytics integration
 *
 * Reemplaza a LoginPasswordActivity.java
 *
 * @since v3.0.0 (TRACK B - Auth Modernization)
 */
@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
        private const val PREFS_LEGACY = "Preferences"
        private const val KEY_LEGACY_PASSWORD = "password"
    }

    private lateinit var binding: ActivityLoginModernBinding

    @Inject
    lateinit var authManager: AuthManager

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    private val viewModel: AuthViewModel by viewModels()

    // Legacy SharedPreferences para migración
    private val legacyPrefs: SharedPreferences by lazy {
        getSharedPreferences(PREFS_LEGACY, Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginModernBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Log app open event
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, Bundle().apply {
            putString(FirebaseAnalytics.Param.VALUE, "LoginActivity opened")
        })

        // Setup toolbar
        setupToolbar()

        // Check for legacy migration
        checkLegacyMigration()

        // Setup UI based on current state
        setupUi()

        // Observe ViewModel states
        observeAuthState()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setIcon(R.drawable.ic_bullseye)
            title = getString(R.string.app_name)
            subtitle = getString(R.string.login)
        }
    }

    /**
     * Verifica si hay un PIN legacy en SharedPreferences y lo migra a AuthManager
     */
    private fun checkLegacyMigration() {
        val legacyPin = legacyPrefs.getString(KEY_LEGACY_PASSWORD, null)

        if (!legacyPin.isNullOrEmpty() && !authManager.hasPinConfigured()) {
            // Migrar PIN legacy al nuevo sistema encriptado
            val result = authManager.setupPin(legacyPin)
            if (result.isSuccess) {
                // Eliminar PIN legacy de SharedPreferences (mantener otros datos por ahora)
                legacyPrefs.edit().remove(KEY_LEGACY_PASSWORD).apply()
                android.util.Log.i(TAG, "Legacy PIN migrado exitosamente a AuthManager")
            } else {
                android.util.Log.e(TAG, "Error migrando PIN legacy: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    private fun setupUi() {
        val hasPinConfigured = authManager.hasPinConfigured()

        // Mostrar/ocultar campos según el estado
        binding.apply {
            if (hasPinConfigured) {
                // Usuario ya registrado - modo LOGIN
                textInputLayoutConfirm.visibility = View.GONE
                titleText.text = getString(R.string.lbl_insert_password)
                setupBiometricButton()
            } else {
                // Usuario nuevo - modo REGISTRO
                textInputLayoutConfirm.visibility = View.VISIBLE
                titleText.text = getString(R.string.lbl_password)
                biometricButton.visibility = View.GONE
            }

            // Setup PIN input
            pinInput.addTextChangedListener(createPinTextWatcher())
            pinInput.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                    handlePinSubmit()
                    true
                } else {
                    false
                }
            }

            // Confirm PIN input (solo registro)
            confirmPinInput.addTextChangedListener(createConfirmPinTextWatcher())

            // Continue button
            continueButton.setOnClickListener {
                handlePinSubmit()
            }

            // Version label
            versionLabel.text = getAppVersion()
        }
    }

    private fun setupBiometricButton() {
        val biometricAvailability = authManager.canAuthenticateWithBiometrics()

        binding.biometricButton.apply {
            if (biometricAvailability.isAvailable() && authManager.isBiometricEnabled()) {
                visibility = View.VISIBLE
                setOnClickListener {
                    authenticateWithBiometrics()
                }
                // Intentar biometría automáticamente si está habilitada
                authenticateWithBiometrics()
            } else if (biometricAvailability.isAvailable()) {
                // Biometría disponible pero no habilitada - mostrar opción para habilitarla
                visibility = View.VISIBLE
                setOnClickListener {
                    showEnableBiometricDialog()
                }
            } else {
                visibility = View.GONE
            }
        }
    }

    private fun showEnableBiometricDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Autenticación biométrica")
            .setMessage("¿Deseas habilitar la autenticación con huella digital o Face ID?")
            .setPositiveButton("Sí") { _, _ ->
                authenticateWithBiometricsToEnable()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun authenticateWithBiometricsToEnable() {
        lifecycleScope.launch {
            authManager.authenticateWithBiometrics(
                activity = this@LoginActivity,
                title = "Configurar biometría",
                subtitle = "Verifica tu huella para habilitar",
                negativeButtonText = "Cancelar"
            ).collect { result ->
                when (result) {
                    is BiometricResult.Success -> {
                        authManager.setBiometricEnabled(true)
                        Snackbar.make(
                            binding.root,
                            "Autenticación biométrica habilitada",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        // Aún necesita PIN la primera vez después de habilitar
                    }
                    is BiometricResult.Failed -> {
                        Snackbar.make(
                            binding.root,
                            "Intenta de nuevo",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    is BiometricResult.Error -> {
                        handleBiometricError(result.errorCode, result.message)
                    }
                }
            }
        }
    }

    private fun authenticateWithBiometrics() {
        lifecycleScope.launch {
            authManager.authenticateWithBiometrics(
                activity = this@LoginActivity,
                title = "Munición",
                subtitle = "Usa tu huella o Face ID para acceder",
                negativeButtonText = "Usar PIN"
            ).collect { result ->
                when (result) {
                    is BiometricResult.Success -> {
                        // Autenticación exitosa
                        logLoginSuccess("biometric")
                        navigateToMain()
                    }
                    is BiometricResult.Failed -> {
                        // Intento fallido pero puede reintentar
                        Snackbar.make(
                            binding.root,
                            "Huella no reconocida",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    is BiometricResult.Error -> {
                        handleBiometricError(result.errorCode, result.message)
                    }
                }
            }
        }
    }

    private fun handleBiometricError(errorCode: Int, message: String) {
        when (errorCode) {
            BiometricPrompt.ERROR_USER_CANCELED,
            BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                // Usuario canceló o eligió usar PIN - no mostrar error
                binding.pinInput.requestFocus()
            }
            BiometricPrompt.ERROR_LOCKOUT,
            BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                Snackbar.make(
                    binding.root,
                    "Demasiados intentos. Usa tu PIN.",
                    Snackbar.LENGTH_LONG
                ).show()
                binding.pinInput.requestFocus()
            }
            else -> {
                Snackbar.make(
                    binding.root,
                    "Error biométrico: $message",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun createPinTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.textInputLayoutPin.error = null
            }

            override fun afterTextChanged(s: Editable?) {}
        }
    }

    private fun createConfirmPinTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val pin = binding.pinInput.text?.toString() ?: ""
                val confirmPin = s?.toString() ?: ""

                binding.textInputLayoutConfirm.error = if (pin != confirmPin && confirmPin.isNotEmpty()) {
                    getString(R.string.password_equal_fail)
                } else {
                    null
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }
    }

    private fun handlePinSubmit() {
        val pin = binding.pinInput.text?.toString() ?: ""

        if (authManager.hasPinConfigured()) {
            // Modo LOGIN
            handleLogin(pin)
        } else {
            // Modo REGISTRO
            val confirmPin = binding.confirmPinInput.text?.toString() ?: ""
            handleRegistration(pin, confirmPin)
        }
    }

    private fun handleLogin(pin: String) {
        if (pin.length < AuthManager.MIN_PIN_LENGTH) {
            binding.textInputLayoutPin.error = getString(R.string.password_short_fail)
            return
        }

        if (authManager.verifyPin(pin)) {
            logLoginSuccess("pin")
            navigateToMain()
        } else {
            binding.textInputLayoutPin.error = getString(R.string.password_fail)
            binding.pinInput.text?.clear()
        }
    }

    private fun handleRegistration(pin: String, confirmPin: String) {
        // Validaciones
        if (pin.length < AuthManager.MIN_PIN_LENGTH) {
            binding.textInputLayoutPin.error = getString(R.string.password_short_fail)
            return
        }

        if (pin != confirmPin) {
            binding.textInputLayoutConfirm.error = getString(R.string.password_equal_fail)
            return
        }

        // Guardar PIN
        val result = authManager.setupPin(pin)
        if (result.isSuccess) {
            Snackbar.make(
                binding.root,
                getString(R.string.password_save),
                Snackbar.LENGTH_LONG
            ).show()

            logLoginSuccess("registration")
            navigateToMain()
        } else {
            binding.textInputLayoutPin.error = getString(R.string.password_save_fail)
        }
    }

    private fun logLoginSuccess(method: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, method)
        })
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun observeAuthState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authState.collect { state ->
                    when (state) {
                        is AuthViewModel.AuthState.Loading -> {
                            binding.progressIndicator.isVisible = true
                        }
                        is AuthViewModel.AuthState.Authenticated -> {
                            binding.progressIndicator.isVisible = false
                            // Ya autenticado, navegar directamente
                            navigateToMain()
                        }
                        is AuthViewModel.AuthState.Unauthenticated -> {
                            binding.progressIndicator.isVisible = false
                            // Mostrar UI de login
                        }
                        is AuthViewModel.AuthState.Error -> {
                            binding.progressIndicator.isVisible = false
                            Snackbar.make(
                                binding.root,
                                state.message,
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun getAppVersion(): String {
        return try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            "v${pInfo.versionName}"
        } catch (e: Exception) {
            ""
        }
    }
}
