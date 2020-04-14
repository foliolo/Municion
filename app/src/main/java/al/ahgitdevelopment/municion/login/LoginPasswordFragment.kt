package al.ahgitdevelopment.municion.login

import al.ahgitdevelopment.municion.NavigationActivity
import al.ahgitdevelopment.municion.R
import al.ahgitdevelopment.municion.Utils
import al.ahgitdevelopment.municion.databinding.FragmentLoginBinding
import al.ahgitdevelopment.municion.datamodel.Guia
import al.ahgitdevelopment.municion.di.AppComponent
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.activity_navigation.*
import kotlinx.android.synthetic.main.fragment_login.*
import java.util.*
import javax.inject.Inject

class LoginPasswordFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefs: SharedPreferences

    private val viewModel: LoginViewModel by viewModels {
        viewModelFactory
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        AppComponent.create(requireContext()).inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val binding: FragmentLoginBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.buttonNavBarVisibility.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            requireActivity().nav_view.visibility = it
        })

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

        setUpToolBar()
        loadAppVersion()
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

    private fun setUpToolBar() {
        (activity as NavigationActivity).supportActionBar?.apply {
            setIcon(R.drawable.ic_bullseye)
            setTitle(R.string.app_name)
            setSubtitle(R.string.login)
        }
    }

    private fun loadAppVersion() {
        requireContext().packageManager.let { packageManager ->
            packageManager.getPackageInfo(requireContext().packageName, 0).let {
                val version = "v${it.versionName}"
            }
        }
    }

    private fun checkAccountPermission() {
        val accountPermission: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            accountPermission = requireContext().checkSelfPermission(Manifest.permission.GET_ACCOUNTS)
            if (accountPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(
                        Manifest.permission.GET_ACCOUNTS),
                        100 //Codigo de respuesta de
                )
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
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

        findNavController().navigate(R.id.fragmentMainContent)

        // Registrar Login - Analytics
//        val androidId = Settings.Secure.getString(requireContext().contentResolver, Settings.Secure.ANDROID_ID)
//        val bundle = Bundle()
//        bundle.putString(FirebaseAnalytics.Param.VALUE, androidId)
//        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)
    }

    private fun checkYearCupo(intent: Intent) { // Comprobar year para renovar los cupos
        val yearPref = prefs.getInt("year", 0)
        val year = Calendar.getInstance()[Calendar.YEAR] // Year actual
        if (yearPref != 0 && year > yearPref) {
            val listaGuias: ArrayList<Guia>? = intent.getParcelableArrayListExtra("guias")
            listaGuias?.let {

                if (listaGuias.size > 0) {
                    for (guia in listaGuias) {
                        val nombreLicencia = Utils.getStringLicenseFromId(requireContext(), guia.tipoLicencia)
                        val idNombreLicencia = nombreLicencia.split(" ").toTypedArray()[0]
                        val tipoArma = guia.tipoArma
                        when (idNombreLicencia) {
                            "A", "Libro" -> when (tipoArma) {
                                0, 3 -> guia.cupo = 100
                                1 -> guia.cupo = 5000
                                2, 4 -> guia.cupo = 1000
                            }
                            "B" -> when (tipoArma) {
                                0, 1 -> guia.cupo = 100
                            }
                            "C" -> guia.cupo = 100
                            "D" -> guia.cupo = 1000
                            "E" -> when (tipoArma) {
                                0 -> guia.cupo = 5000
                                1 -> guia.cupo = 1000
                            }
                            "F", "Federativa" -> when (tipoArma) {
                                0, 3 -> guia.cupo = 100
                                1 -> guia.cupo = 5000
                                2 -> guia.cupo = 1000
                            }
                            "AE" -> guia.cupo = 1000
                            "AER" -> when (tipoArma) {
                                0, 2 -> {
                                    guia.cupo = 100
                                    guia.cupo = 1000
                                }
                                1 -> guia.cupo = 1000
                            }
                        }
                    }
                }
            }
        }
    }

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

    companion object {
        private const val TAG = "LoginPasswordActivity"
        const val MIN_PASS_LENGTH = 6

        lateinit var mFirebaseAnalytics: FirebaseAnalytics
        lateinit var prefs: SharedPreferences

    }
}
