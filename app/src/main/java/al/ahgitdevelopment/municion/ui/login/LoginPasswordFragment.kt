package al.ahgitdevelopment.municion.ui.login

import al.ahgitdevelopment.municion.BuildConfig
import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.databinding.FragmentLoginBinding
import al.ahgitdevelopment.municion.repository.firebase.RemoteStorageDataSource.Companion.EVENT_LOGOUT
import al.ahgitdevelopment.municion.repository.firebase.RemoteStorageDataSource.Companion.PARAM_USER_UID
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class LoginPasswordFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    @Inject
    lateinit var firebaseCrashlytics: FirebaseCrashlytics

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.userState.observe(viewLifecycleOwner) { userState ->
            binding.loginPassword2.visibility = when (userState) {
                LoginViewModel.UserState.NEW_USER -> View.VISIBLE
                LoginViewModel.UserState.ACTIVE_USER -> View.GONE
            }
        }

        viewModel.password1Error.observe(viewLifecycleOwner) {
            binding.loginPassword1.error = getErrorMessage(it)
        }

        viewModel.password2Error.observe(viewLifecycleOwner) {
            binding.loginPassword2.error = getErrorMessage(it)
        }

        viewModel.navigateIntoApp.observe(viewLifecycleOwner) {
            findNavController().navigate(R.id.licensesFragment)
        }

        viewModel.navigateIntoTutorial.observe(viewLifecycleOwner) {
            findNavController().navigate(R.id.tutorialViewPagerFragment)
        }

        viewModel.onCreateView()

        loadAppVersion()
    }

    override fun onResume() {
        super.onResume()
        setUpUser()

        if (BuildConfig.DEBUG) {
            binding.loginButton.visibility = View.VISIBLE
            binding.loginPassword1.editText?.setText(BuildConfig.PASSWORD)
        }
    }

    private fun getErrorMessage(error: LoginViewModel.ErrorMessages): String? {
        return when (error) {
            LoginViewModel.ErrorMessages.NOT_MATCHING_PASSWORD -> getString(R.string.login_not_matching_password_error)
            LoginViewModel.ErrorMessages.SHORT_PASSWORD -> getString(R.string.login_short_password_error)
            LoginViewModel.ErrorMessages.NONE -> null
        }
    }

    private fun loadAppVersion() {
        requireContext().packageManager.let { packageManager ->
            packageManager.getPackageInfo(requireContext().packageName, 0).let {
                val version = "v${it.versionName}"
                binding.loginVersionLabel.text = version
            }
        }
    }

    private fun checkAccountPermission() {
        val accountPermission: Int = requireContext().checkSelfPermission(Manifest.permission.GET_ACCOUNTS)
        if (accountPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.GET_ACCOUNTS), 100)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty()) {
            if (requestCode == 100) {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    checkAccountPermission()
                } else {
                    Timber.w("Permisos de correo no concedidos")
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_login, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.menu_log_out -> {
                signOut()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun signOut() {
        AuthUI.getInstance()
            .signOut(requireContext())
            .addOnCompleteListener {
                recordLogoutEvent()
                clearUserData()
                setUpUser()
            }
    }

    private fun recordLogoutEvent() {
        firebaseAnalytics.logEvent(EVENT_LOGOUT, null)
    }

    private fun clearUserData() {
        firebaseAnalytics.setUserId(null)
        firebaseCrashlytics.setUserId("")
    }

    private fun setUpUser() {
        val user = firebaseAuth.currentUser

        if (user != null) {
            recordUserData(user)
        } else {
            signIn()
        }
    }

    private fun signIn() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().setSignInOptions(GoogleSignInOptions.Builder().build()).build(),
            // AuthUI.IdpConfig.AnonymousBuilder().build(),
            AuthUI.IdpConfig.EmailBuilder().build(),
        )

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setLogo(R.mipmap.ic_launcher_3_light)
                .setTheme(R.style.AppTheme)
                .setAvailableProviders(providers)
                .build(),
            RC_SIGN_IN,
        )

        // FIXME: This fix the obsolete startActivityForResult but has to be called before the fragment is created.
        // AuthUI.getInstance()
        //     .createSignInIntentBuilder()
        //     .setLogo(R.mipmap.ic_launcher_3_light)
        //     .setTheme(R.style.AppTheme)
        //     .setAvailableProviders(providers)
        //     .build().apply {
        //         this@LoginPasswordFragment
        //             .registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        //                 this@LoginPasswordFragment
        //             }
        //     }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                val user = firebaseAuth.currentUser

                if (user != null) {
                    recordUserData(user)
                    recordLoginEvent(user)
                }
            } else {
                response?.error?.cause?.let(firebaseCrashlytics::recordException)
            }
        }
    }

    private fun recordUserData(user: FirebaseUser) {
        firebaseAnalytics.setUserId(user.uid)
        firebaseCrashlytics.setUserId(user.uid)
    }

    private fun recordLoginEvent(user: FirebaseUser) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN) {
            param(FirebaseAnalytics.Param.METHOD, user.providerId)
            param(PARAM_USER_UID, user.uid)
        }
    }

    companion object {
        private const val RC_SIGN_IN = 100
    }
}
