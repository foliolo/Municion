package al.ahgitdevelopment.municion.login

import al.ahgitdevelopment.municion.*
import al.ahgitdevelopment.municion.billingutil.IabBroadcastReceiver
import al.ahgitdevelopment.municion.billingutil.IabBroadcastReceiver.IabBroadcastListener
import al.ahgitdevelopment.municion.billingutil.IabHelper
import al.ahgitdevelopment.municion.billingutil.IabHelper.IabAsyncInProgressException
import al.ahgitdevelopment.municion.billingutil.IabHelper.QueryInventoryFinishedListener
import al.ahgitdevelopment.municion.billingutil.IabResult
import al.ahgitdevelopment.municion.billingutil.Inventory
import al.ahgitdevelopment.municion.databases.DataBaseSQLiteHelper
import al.ahgitdevelopment.municion.datamodel.Guia
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.basic_toolbar.*
import java.util.*

class LoginPasswordActivity : AppCompatActivity(), IabBroadcastListener, QueryInventoryFinishedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE)
        //Gestion de mensajes de firebase en el intent de entrada
        // [START handle_data_extras]
        if (intent.extras != null) {
            for (key in intent.extras!!.keySet()) {
                val value = intent.extras!![key]
                Log.d(localClassName, "Key: $key Value: $value")
            }
        }
        // [END handle_data_extras]

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setIcon(R.drawable.ic_bullseye)
        toolbar.setTitle(R.string.app_name)
        toolbar.setSubtitle(R.string.login)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.VALUE, "Inicio de aplicacion")
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle)
        login_version_label.text = Utils.getAppVersion(this)

        // Registro de contraseña
        if (!prefs.contains("password") || prefs.getString("password", "") == "") {
            login_password_2.visibility = View.VISIBLE
        } else {
            login_password_2.visibility = View.GONE
        }
        //Añadimos la contraseña a las preferencias
        button.setOnClickListener { evaluatePassword(prefs) }
        login_password_1.editText?.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_NEXT -> evaluatePassword(prefs)
                EditorInfo.IME_ACTION_DONE -> evaluatePassword(prefs)
                else -> Toast.makeText(this@LoginPasswordActivity, "IME erroneo", Toast.LENGTH_SHORT).show()
            }
            true
        }
        login_password_1.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                try {
                    val pass = prefs.getString("password", "")
                    if (pass != "" && login_password_1.editText?.text.toString() != pass && login_password_1.error != null) {
                        login_password_1.error = getString(R.string.password_equal_fail)
                    }
                    if (login_password_1.editText?.text.toString() == pass && login_password_1.editText?.text.toString().length >= MIN_PASS_LENGTH)
                        login_password_1.error = null
                } catch (ex: Exception) {
                    Log.e(TAG, "Error en el onTextChange por la version de android")
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        login_password_2.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                try {
                    if (s.toString() == login_password_1.editText?.text.toString()) {
                        login_password_2.error = null
                    } else {
                        login_password_2.error = getString(R.string.password_equal_fail)
                    }
                } catch (ex: Exception) {
                    Log.e(TAG, "Error en el onTextChange por la version de android")
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        if (!prefs.contains(Utils.PREFS_SHOW_ADS)) { // Agregar la configuración de anuncios en SharedPrefs
            val editor = prefs.edit()
            editor.putBoolean(Utils.PREFS_SHOW_ADS, true)
            editor.apply()
        }
    }

    override fun onStart() {
        super.onStart()
        try { // Comprobación de compra de eliminacion de publicidad
            val base64EncodedPublicKey = getString(R.string.app_public_key)
            mHelper = IabHelper(this, base64EncodedPublicKey)
            // enable debug logging (for a production application, you should set this to false).
            mHelper.enableDebugLogging(BuildConfig.DEBUG)
            mHelper.startSetup { result ->
                if (!result.isSuccess) { // Oh no, there was a problem.
                    Log.w(TAG, "Problem setting up In-app Billing: " + result.message)
                    isPurchaseAvailable = false
                } else {
                    isPurchaseAvailable = true
                    try {
                        mHelper.queryInventoryAsync(this@LoginPasswordActivity  /*QueryInventoryFinishedListener*/)
                    } catch (ex: IabAsyncInProgressException) {
                        Log.e(TAG, "Error querying inventory. Another async operation in progress.", ex)
//                        FirebaseCrash.logcat(Log.ERROR, TAG, "Error querying inventory. Another async operation in progress.");
//                        FirebaseCrash.report(ex);
                    }
                }
            }

            // Important: Dynamically register for broadcast messages about updated purchases.
            // We register the receiver here instead of as a <receiver> in the Manifest
            // because we always call getPurchases() at startup, so therefore we can ignore
            // any broadcasts sent while the app isn't running.
            // Note: registering this listener in an Activity is a bad idea, but is done here
            // because this is a SAMPLE. Regardless, the receiver must be registered after
            // IabHelper is setup, but before first call to getPurchases().
            mBroadcastReceiver = IabBroadcastReceiver(this /*IabBroadcastListener*/)
            val broadcastFilter = IntentFilter(IabBroadcastReceiver.ACTION)
            registerReceiver(mBroadcastReceiver, broadcastFilter)
            if (prefs.getBoolean(Utils.PREFS_SHOW_ADS, true)) {
                login_adView.visibility = View.VISIBLE
                login_adView.loadAd(Utils.getAdRequest(login_adView))
            } else {
                login_adView.visibility = View.GONE
                login_adView.isEnabled = false
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Error en OnStart del login por fallo en la libreria de pago")
        }
    }

    override fun onDestroy() {
        // Gestión del año actual para actualizar los cupos y las compras
        val calendar = Calendar.getInstance()
        val yearPref = calendar[Calendar.YEAR]
        prefs.edit().putInt("year", yearPref).apply()

        unregisterReceiver(mBroadcastReceiver)

        Log.d(TAG, "Destroying helper.")
        mHelper.disposeWhenFinished()

        super.onDestroy()
    }

    private fun checkAccountPermission() {
        val accountPermission: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            accountPermission = checkSelfPermission(Manifest.permission.GET_ACCOUNTS)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_login, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this@LoginPasswordActivity, SettingsFragment::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Validación de la contraseña para poder entrar a la aplicación
     *
     * @param prefs Preferencias
     */
    private fun evaluatePassword(prefs: SharedPreferences?) { // Registro de usuario
        if (!prefs!!.contains("password") || prefs.getString("password", "") == "") {
            if (savePassword()) { // Guardamos la contraseña
                Toast.makeText(this@LoginPasswordActivity, R.string.password_save, Toast.LENGTH_LONG).show()
                launchActivity()
                finish()
            } else { // Fallo al guardar la contraseñas
                if (login_password_1.error == null) login_password_1.error = getString(R.string.password_save_fail)
            }
        } else {
            if (checkPassword()) { // Password correcta
                launchActivity()
                finish()
            } else { // Password incorrecta
                login_password_1.error = getString(R.string.password_fail)
                login_password_1.editText?.setText("")
            }
        }
    }

    /**
     * Guarda la contraseña en el sharedPreference
     *
     * @return Contraseña valida o no
     */
    private fun savePassword(): Boolean {
        var flag = false
        if (login_password_1.editText?.text.toString().length >= MIN_PASS_LENGTH) {
            if (login_password_1.editText?.text.toString() == login_password_2.editText?.text.toString()) {
                val editor = prefs.edit()
                editor.putString("password", login_password_1.editText?.text.toString())
                editor.apply()
                flag = true
            } else {
                login_password_2.error = getString(R.string.password_equal_fail)
            }
        } else {
            flag = false
            login_password_1.error = getString(R.string.password_short_fail)
        }
        return flag
    }

    /**
     * Valida la contraseña introducida por el usuario frente a la guardada en el sharedPreferences
     *
     * @return Contraseña valida o invalida
     */
    private fun checkPassword(): Boolean {
        var isPassCorrect = false
        val pass = prefs.getString("password", "")
        if (pass == login_password_1.editText?.text.toString()) {
            isPassCorrect = true
        } else {
            login_password_2.error = getString(R.string.password_equal_fail)
        }
        return isPassCorrect
    }

    //    @AddTrace(name = "launchActivity", enabled = true/*Optional*/)
    private fun launchActivity() {
        val dbSqlHelper = DataBaseSQLiteHelper(applicationContext)
        //Lanzamiento del Intent
        val intent = Intent(this@LoginPasswordActivity, FragmentMainActivity::class.java)
        intent.putParcelableArrayListExtra("guias", dbSqlHelper.getListGuias(null))
        intent.putParcelableArrayListExtra("compras", dbSqlHelper.getListCompras(null))
        intent.putParcelableArrayListExtra("licencias", dbSqlHelper.getListLicencias(null))
        intent.putParcelableArrayListExtra("tiradas", dbSqlHelper.getListTiradas(null))
        checkYearCupo(intent)
        startActivity(intent)
        dbSqlHelper.close()

        // Registrar Login - Analytics
        val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.VALUE, androidId)
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)
    }

    private fun checkYearCupo(intent: Intent) { // Comprobar year para renovar los cupos
        val yearPref = prefs.getInt("year", 0)
        val year = Calendar.getInstance()[Calendar.YEAR] // Year actual
        if (yearPref != 0 && year > yearPref) {
            val listaGuias: ArrayList<Guia>? = intent.getParcelableArrayListExtra("guias")
            listaGuias?.let {

                if (listaGuias.size > 0) {
                    for (guia in listaGuias) {
                        val nombreLicencia = Utils.getStringLicenseFromId(this@LoginPasswordActivity, guia.tipoLicencia)
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
        var isTutorial = true
        if (intent.hasExtra("tutorial")) isTutorial = intent.getBooleanExtra("tutorial", true)
        if (prefs.getBoolean("show_tutorial", true) && isTutorial) {
            val intent = Intent(this, FragmentTutorialActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onQueryInventoryFinished(result: IabResult, inventory: Inventory) {
        try { // if we were disposed of in the meantime, quit.
            if (result.isFailure) {
                Log.e(TAG, "Error obteniendo los detalles de los productos" + result.message)
                return
            }
            //Si el usuario ha comprado la eliminación de anuncios
            if (inventory.hasPurchase(PURCHASE_ID_REMOVE_ADS)) { //pero no tiene actualizado su shared prefs
                if (prefs.getBoolean(Utils.PREFS_SHOW_ADS, true)) { // Eliminamos la publicidad
                    login_adView.visibility = View.GONE
                    login_adView.isEnabled = false
                    // Actualizamos las preferencias
                    prefs.edit().putBoolean(Utils.PREFS_SHOW_ADS, false).apply()
                }
            } else {
                prefs.edit().putBoolean(Utils.PREFS_SHOW_ADS, true).apply()
                login_adView.visibility = View.VISIBLE
                login_adView.isEnabled = true
                login_adView.loadAd(Utils.getAdRequest(login_adView))
            }
            checkAccountPermission()
        } catch (ex: Exception) {
//            FirebaseCrash.logcat(Log.ERROR, TAG, "Error en el proceso de onQueryInventoryFinished");
//            FirebaseCrash.report(ex);
        }
    }

    override fun receivedBroadcast() { // Received a broadcast notification that the inventory of items has changed
        Log.d(TAG, "Received broadcast notification. Querying inventory.")
        try {
            mHelper.queryInventoryAsync(this@LoginPasswordActivity  /*QueryInventoryFinishedListener*/)
        } catch (ex: IabAsyncInProgressException) {
            Log.e(TAG, "Error querying inventory. Another async operation in progress.", ex)
        }
    }

    companion object {
        private const val TAG = "LoginPasswordActivity"
        const val MIN_PASS_LENGTH = 6
        const val PURCHASE_ID_REMOVE_ADS = "remove_ads"

        // Provides purchase notification while this app is running
        lateinit var mBroadcastReceiver: IabBroadcastReceiver

        lateinit var mFirebaseAnalytics: FirebaseAnalytics
        lateinit var prefs: SharedPreferences
        lateinit var mHelper: IabHelper

        var isPurchaseAvailable = false
    }
}