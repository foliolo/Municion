package al.ahgitdevelopment.municion.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.databinding.FragmentAccountSettingsBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * AccountSettingsFragment - UI para configuración de cuenta
 *
 * FASE 3: Account Settings
 * - Muestra estado de cuenta (anónima/vinculada)
 * - Permite vincular cuenta con Google o Email
 * - Configuración de PIN y biometría
 * - Beneficios de vincular cuenta explicados
 *
 * @since v3.0.0 (TRACK B - Auth Modernization)
 */
@AndroidEntryPoint
class AccountSettingsFragment : Fragment() {

    private var _binding: FragmentAccountSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AccountSettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeUiState()
    }

    private fun setupClickListeners() {
        // Vincular con Email
        binding.btnLinkEmail.setOnClickListener {
            showLinkEmailDialog()
        }

        // Vincular con Google
        binding.btnLinkGoogle.setOnClickListener {
            // TODO: Implementar Google Sign-In
            Snackbar.make(
                binding.root,
                "Google Sign-In: Próximamente",
                Snackbar.LENGTH_SHORT
            ).show()
        }

        // Toggle biometría
        binding.switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setBiometricEnabled(isChecked)
        }

        // Cambiar PIN
        binding.btnChangePin.setOnClickListener {
            showChangePinDialog()
        }

        // Cerrar sesión
        binding.btnSignOut.setOnClickListener {
            showSignOutDialog()
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observar estado de cuenta
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is AccountSettingsViewModel.AccountUiState.Loading -> {
                                binding.progressIndicator.visibility = View.VISIBLE
                                binding.contentGroup.visibility = View.GONE
                            }

                            is AccountSettingsViewModel.AccountUiState.NotAuthenticated -> {
                                binding.progressIndicator.visibility = View.GONE
                                binding.contentGroup.visibility = View.VISIBLE
                                updateUiForNotAuthenticated()
                            }

                            is AccountSettingsViewModel.AccountUiState.Loaded -> {
                                binding.progressIndicator.visibility = View.GONE
                                binding.contentGroup.visibility = View.VISIBLE
                                updateUiForLoaded(state)
                            }
                        }
                    }
                }

                // Observar estado de vinculación
                launch {
                    viewModel.linkingState.collect { state ->
                        when (state) {
                            is AccountSettingsViewModel.LinkingState.Idle -> {
                                binding.linkingProgress.visibility = View.GONE
                            }

                            is AccountSettingsViewModel.LinkingState.Linking -> {
                                binding.linkingProgress.visibility = View.VISIBLE
                            }

                            is AccountSettingsViewModel.LinkingState.Success -> {
                                binding.linkingProgress.visibility = View.GONE
                                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                                viewModel.resetLinkingState()
                            }

                            is AccountSettingsViewModel.LinkingState.Error -> {
                                binding.linkingProgress.visibility = View.GONE
                                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                                viewModel.resetLinkingState()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateUiForNotAuthenticated() {
        binding.apply {
            accountStatusText.text = "No autenticado"
            accountStatusIcon.setImageResource(R.drawable.ic_cross)

            // Mostrar opciones de vincular
            linkAccountCard.visibility = View.VISIBLE
            linkedAccountCard.visibility = View.GONE

            // Ocultar botón de cerrar sesión
            btnSignOut.visibility = View.GONE
        }
    }

    private fun updateUiForLoaded(state: AccountSettingsViewModel.AccountUiState.Loaded) {
        binding.apply {
            // Estado de cuenta
            accountStatusText.text = state.accountInfo.statusText

            if (state.accountInfo.isAnonymous) {
                accountStatusIcon.setImageResource(R.drawable.ic_cross)
                // Mostrar opciones de vincular
                linkAccountCard.visibility = View.VISIBLE
                linkedAccountCard.visibility = View.GONE
                benefitsCard.visibility = View.VISIBLE
            } else {
                accountStatusIcon.setImageResource(R.drawable.ic_go)
                // Mostrar info de cuenta vinculada
                linkAccountCard.visibility = View.GONE
                linkedAccountCard.visibility = View.VISIBLE
                benefitsCard.visibility = View.GONE

                linkedEmailText.text = state.accountInfo.email ?: "Sin email"
                linkedUidText.text = "UID: ${state.accountInfo.uid.take(8)}..."
            }

            // Seguridad
            switchBiometric.isChecked = state.securityInfo.biometricEnabled
            switchBiometric.isEnabled = state.securityInfo.biometricAvailable
            biometricUnavailableText.visibility =
                if (state.securityInfo.biometricAvailable) View.GONE else View.VISIBLE

            pinStatusText.text = if (state.securityInfo.hasPinConfigured) {
                "PIN configurado"
            } else {
                "PIN no configurado"
            }
        }
    }

    private fun showLinkEmailDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_link_email, null)
        val emailInput = dialogView.findViewById<TextInputEditText>(R.id.email_input)
        val passwordInput = dialogView.findViewById<TextInputEditText>(R.id.password_input)

        AlertDialog.Builder(requireContext())
            .setTitle("Vincular con Email")
            .setView(dialogView)
            .setPositiveButton("Vincular") { _, _ ->
                val email = emailInput.text?.toString() ?: ""
                val password = passwordInput.text?.toString() ?: ""
                viewModel.linkWithEmail(email, password)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showChangePinDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_pin, null)
        val currentPinInput = dialogView.findViewById<TextInputEditText>(R.id.current_pin_input)
        val newPinInput = dialogView.findViewById<TextInputEditText>(R.id.new_pin_input)
        val confirmPinInput = dialogView.findViewById<TextInputEditText>(R.id.confirm_pin_input)

        AlertDialog.Builder(requireContext())
            .setTitle("Cambiar PIN")
            .setView(dialogView)
            .setPositiveButton("Cambiar") { _, _ ->
                val currentPin = currentPinInput.text?.toString() ?: ""
                val newPin = newPinInput.text?.toString() ?: ""
                val confirmPin = confirmPinInput.text?.toString() ?: ""

                if (newPin != confirmPin) {
                    Snackbar.make(binding.root, "Los PINs no coinciden", Snackbar.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val result = viewModel.changePin(currentPin, newPin)
                if (result.isSuccess) {
                    Snackbar.make(binding.root, "PIN cambiado correctamente", Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(
                        binding.root,
                        result.exceptionOrNull()?.message ?: "Error cambiando PIN",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showSignOutDialog() {
        val message = if ((viewModel.uiState.value as? AccountSettingsViewModel.AccountUiState.Loaded)
                ?.accountInfo?.isAnonymous == true
        ) {
            "Si cierras sesión con una cuenta anónima, perderás acceso a tus datos en la nube. " +
                    "Los datos locales se mantendrán.\n\n¿Estás seguro?"
        } else {
            "¿Estás seguro de que deseas cerrar sesión?"
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Cerrar sesión")
            .setMessage(message)
            .setPositiveButton("Cerrar sesión") { _, _ ->
                viewModel.signOut()
                // Navegar al login
                activity?.finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
