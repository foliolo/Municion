package al.ahgitdevelopment.municion.ui.login

import al.ahgitdevelopment.municion.BuildConfig
import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.databinding.FragmentLoginBinding
import al.ahgitdevelopment.municion.repository.firebase.RemoteStorageDataSource.Companion.EVENT_LOGOUT
import al.ahgitdevelopment.municion.repository.firebase.RemoteStorageDataSource.Companion.PARAM_USER_UID
import al.ahgitdevelopment.municion.utils.SimpleCountingIdlingResource
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import androidx.databinding.DataBindingUtil
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
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class LoginPasswordFragment : Fragment() {

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    @Inject
    lateinit var firebaseCrashlytics: FirebaseCrashlytics

    @Inject
    lateinit var idlingResource: SimpleCountingIdlingResource

    @VisibleForTesting(otherwise = PRIVATE)
    val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding: FragmentLoginBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.userState.observe(viewLifecycleOwner) { userState ->
            login_password_2.visibility = when (userState) {
                LoginViewModel.UserState.NEW_USER -> View.VISIBLE
                LoginViewModel.UserState.ACTIVE_USER -> View.GONE
            }
        }

        viewModel.password1Error.observe(viewLifecycleOwner) {
            login_password_1.error = getErrorMessage(it)
        }

        viewModel.password2Error.observe(viewLifecycleOwner) {
            login_password_2.error = getErrorMessage(it)
        }

        viewModel.navigateIntoApp.observe(viewLifecycleOwner) {
            findNavController().navigate(R.id.licensesFragment)
        }

        viewModel.navigateIntoTutorial.observe(viewLifecycleOwner) {
            findNavController().navigate(R.id.tutorialViewPagerFragment)
        }

        viewModel.onCreateView()

        loadAppVersion()

        /* Password 1 edittext listeners
        login_password_1.editText?.setOnEditorActionListener { v, actionId, event ->
            when (actionId) {
                IME_ACTION_DONE -> {
                    viewModel.onButtonClick(null)
                    true
                }
                else -> false
            }
        }

        login_password_1.editText?.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                viewModel.onButtonClick(null)
                true
            } else {
                false
            }
        }
        */
    }

    override fun onResume() {
        super.onResume()
        setUpUser()

        if (BuildConfig.DEBUG) {
            login_button.visibility = View.VISIBLE
            login_password_1.editText?.setText(BuildConfig.PASSWORD)
            // login_button.performClick()
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
                login_version_label.text = version
            }
        }
    }

    private fun checkAccountPermission() {
        val accountPermission: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            accountPermission =
                requireContext().checkSelfPermission(Manifest.permission.GET_ACCOUNTS)
            if (accountPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.GET_ACCOUNTS
                    ),
                    100
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
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
        idlingResource.increment()
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().setSignInOptions(GoogleSignInOptions.Builder().build()).build(),
            // AuthUI.IdpConfig.AnonymousBuilder().build(),
            AuthUI.IdpConfig.EmailBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setLogo(R.mipmap.ic_launcher_3_light)
                .setTheme(R.style.AppTheme)
                .setAvailableProviders(providers)
                .build(),
            RC_SIGN_IN
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

        idlingResource.decrement()
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
