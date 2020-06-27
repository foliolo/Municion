package al.ahgitdevelopment.municion.login

import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.databinding.FragmentLoginBinding
import al.ahgitdevelopment.municion.di.AppComponent
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.synthetic.main.fragment_login.*
import javax.inject.Inject

class LoginPasswordFragment : Fragment() {

    @Inject
    lateinit var prefs: SharedPreferences

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    @Inject
    lateinit var firebaseCrashlytics: FirebaseCrashlytics

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: LoginViewModel by viewModels {
        viewModelFactory
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        AppComponent.create(requireContext()).inject(this)
    }

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

        viewModel.userState.observe(viewLifecycleOwner,
            androidx.lifecycle.Observer { userState: LoginViewModel.UserState ->
                login_password_2.visibility = when (userState) {
                    LoginViewModel.UserState.NEW_USER -> View.VISIBLE
                    LoginViewModel.UserState.ACTIVE_USER -> View.GONE
                }
            })

        viewModel.password1Error.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            login_password_1.error = getErrorMessage(it)
        })

        viewModel.password2Error.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            login_password_2.error = getErrorMessage(it)
        })

        viewModel.navigateIntoApp.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            launchAppContent()
        })

        viewModel.onCreateView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        loadAppVersion()
    }

    override fun onResume() {
        super.onResume()
        setUpUser()
    }

    //    override fun onDestroy() {
//        // Gestión del año actual para actualizar los cupos y las compras
//        val calendar = Calendar.getInstance()
//        val yearPref = calendar[Calendar.YEAR]
//        prefs.edit().putInt("year", yearPref).apply()
//
//        super.onDestroy()
//    }

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
                    100 //Codigo de respuesta de
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
                    Log.w(TAG, "Permisos de correo no concedidos")
                }
            }
        }
        //Lanza el tutorial la primera vez
        showTutorial()
    }

    //    @AddTrace(name = "launchActivity", enabled = true/*Optional*/)
    private fun launchAppContent() {
//        val dbSqlHelper = DataBaseSQLiteHelper(requireContext())
        //Lanzamiento del Intent
//        val intent = Intent(this@LoginPasswordFragment, FragmentMainActivity::class.java)
//        intent.putParcelableArrayListExtra("guias", dbSqlHelper.getListGuias(null))
//        intent.putParcelableArrayListExtra("compras", dbSqlHelper.getListCompras(null))
//        intent.putParcelableArrayListExtra("licencias", dbSqlHelper.getListLicencias(null))
//        intent.putParcelableArrayListExtra("tiradas", dbSqlHelper.getListTiradas(null))
//        checkYearCupo(intent)
//        startActivity(intent)
//        dbSqlHelper.close()

        findNavController().navigate(R.id.licenciasFragment)

        // Registrar Login - Analytics
//        val androidId = Settings.Secure.getString(requireContext().contentResolver, Settings.Secure.ANDROID_ID)
//        val bundle = Bundle()
//        bundle.putString(FirebaseAnalytics.Param.VALUE, androidId)
//        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)
    }

//    private fun checkYearCupo(intent: Intent) { // Comprobar year para renovar los cupos
//        val yearPref = prefs.getInt("year", 0)
//        val year = Calendar.getInstance()[Calendar.YEAR] // Year actual
//        if (yearPref != 0 && year > yearPref) {
//            val listaGuias: ArrayList<Guia>? = intent.getParcelableArrayListExtra("guias")
//            listaGuias?.let {
//
//                if (listaGuias.size > 0) {
//                    for (guia in listaGuias) {
//                        val nombreLicencia = Utils.getStringLicenseFromId(requireContext(), guia.tipoLicencia)
//                        val idNombreLicencia = nombreLicencia.split(" ").toTypedArray()[0]
//                        val tipoArma = guia.tipoArma
//                        when (idNombreLicencia) {
//                            "A", "Libro" -> when (tipoArma) {
//                                0, 3 -> guia.cupo = 100
//                                1 -> guia.cupo = 5000
//                                2, 4 -> guia.cupo = 1000
//                            }
//                            "B" -> when (tipoArma) {
//                                0, 1 -> guia.cupo = 100
//                            }
//                            "C" -> guia.cupo = 100
//                            "D" -> guia.cupo = 1000
//                            "E" -> when (tipoArma) {
//                                0 -> guia.cupo = 5000
//                                1 -> guia.cupo = 1000
//                            }
//                            "F", "Federativa" -> when (tipoArma) {
//                                0, 3 -> guia.cupo = 100
//                                1 -> guia.cupo = 5000
//                                2 -> guia.cupo = 1000
//                            }
//                            "AE" -> guia.cupo = 1000
//                            "AER" -> when (tipoArma) {
//                                0, 2 -> {
//                                    guia.cupo = 100
//                                    guia.cupo = 1000
//                                }
//                                1 -> guia.cupo = 1000
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }

    /**
     * Lanza el tutorial la primera vez que se inicia la aplicación.
     */
    private fun showTutorial() { // Para que no se muestre el tutorial cuando se ha reseteado el password
//        var isTutorial = true
//        if (requireActivity().intent.hasExtra("tutorial")) isTutorial = requireActivity().intent.getBooleanExtra("tutorial", true)
//        if (prefs.getBoolean("show_tutorial", true) && isTutorial) {
//            val intent = Intent(this, FragmentTutorialActivity::class.java)
//            startActivity(intent)
//        }
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
            R.id.log_out -> {
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

        if (user != null) recordUserData(user)
        else signIn()
    }

    private fun signIn() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder()
                .setSignInOptions(
                    GoogleSignInOptions.Builder()
                        .build()
                )
                .build(),
            AuthUI.IdpConfig.AnonymousBuilder()
                .build(),
            AuthUI.IdpConfig.EmailBuilder()
                .build()
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
    }

    private fun recordUserData(user: FirebaseUser) {
        firebaseAnalytics.setUserId(user.uid)
        firebaseCrashlytics.setUserId(user.uid)
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

    private fun recordLoginEvent(user: FirebaseUser) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN) {
            param(FirebaseAnalytics.Param.METHOD, user.providerId)
            param(PARAM_USER_UID, user.uid)
        }
    }


    companion object {
        private const val TAG = "LoginPasswordActivity"
        const val MIN_PASS_LENGTH = 6
        private const val RC_SIGN_IN = 100
        private const val PARAM_USER_UID = "user_uid"
        private const val EVENT_LOGOUT = "logout"
    }
}
