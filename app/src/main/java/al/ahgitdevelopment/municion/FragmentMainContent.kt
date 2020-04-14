package al.ahgitdevelopment.municion

import al.ahgitdevelopment.municion.adapters.CompraArrayAdapter
import al.ahgitdevelopment.municion.adapters.GuiaArrayAdapter
import al.ahgitdevelopment.municion.adapters.LicenciaArrayAdapter
import al.ahgitdevelopment.municion.adapters.TiradaArrayAdapter
import al.ahgitdevelopment.municion.databases.DataBaseSQLiteHelper
import al.ahgitdevelopment.municion.datamodel.Compra
import al.ahgitdevelopment.municion.datamodel.Guia
import al.ahgitdevelopment.municion.datamodel.Licencia
import al.ahgitdevelopment.municion.datamodel.Tirada
import al.ahgitdevelopment.municion.di.SharedPrefsModule.Companion.PREFS_SHOW_ADS
import al.ahgitdevelopment.municion.forms.CompraFormActivity
import al.ahgitdevelopment.municion.forms.GuiaFormActivity
import android.Manifest
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.BaseColumns
import android.provider.CalendarContract
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemLongClickListener
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.crash.FirebaseCrash
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_fragment_main.*
import kotlinx.android.synthetic.main.activity_fragment_main.view.*
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class FragmentMainContent : Fragment(), AuthStateListener {
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private var prefs: SharedPreferences? = null

    private lateinit var mViewPager: ViewPager

    private lateinit var mInterstitialAd: InterstitialAd

    private var userRef: DatabaseReference? = null
    private val mAuth = FirebaseAuth.getInstance()
    private val mStorage = FirebaseStorage.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.activity_fragment_main, container, false)

        prefs = activity?.getSharedPreferences("Preferences", Context.MODE_PRIVATE)

        mActionModeCallback = object : ActionMode.Callback {

            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                val inflater = mode.menuInflater
                inflater.inflate(R.menu.menu_cab, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                // Here you can perform updates to the CAB due to
                // an invalidate() request
                return false
            }

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                // Respond to clicks on the actions in the CAB
                when (item.itemId) {
                    R.id.item_menu_modify -> {
                        openForm(mActionMode!!.tag as Int)
                        mode.finish() // Action picked, so close the CAB
                    }
                    R.id.item_menu_delete -> {
                        try {
                            deleteSelectedItems(mActionMode!!.tag as Int)
                            showTextEmptyList()
                        } catch (ex: Exception) {
                            FirebaseCrash.logcat(Log.ERROR, TAG,
                                    "Error al borrar elementos de la lista en el método onActionItemClicked()")
                            FirebaseCrash.report(ex)
                        }
                        mode.finish() // Action picked, so close the CAB
                    }
                    else -> return false
                }
                return true
            }

            override fun onDestroyActionMode(mode: ActionMode) {
                auxView.isSelected = false
                mActionMode = null
            }
        }

//        toolbar = findViewById(R.id.toolbar)
//        toolbar!!.setTitle(R.string.app_name)
        //        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setIcon(R.drawable.ic_bullseye);
//        textEmptyList = findViewById(R.id.textEmptyList)

        // Instanciamos la base de datos
        dbSqlHelper = DataBaseSQLiteHelper(requireContext())

        // Carga de las listas en funcion de la conectividad:
        // - Con conexion: Firebase
        // - Sin conexion: DDBB local
        loadLists()

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(activity?.supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        root.pager_container.adapter = mSectionsPagerAdapter
        root.pager_container.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                activity?.invalidateOptionsMenu()
            }

            override fun onPageSelected(position: Int) {
                if (mActionMode != null) mActionMode!!.finish()
                mActionMode = null
                showTextEmptyList()
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
//        root.pager_container.currentItem = 2
        mInterstitialAd = InterstitialAd(requireContext())
        mInterstitialAd.adUnitId = getString(R.string.banner_login_intersticial_id)

//        root.tabs.setupWithViewPager(root.pager_container)
//        if (root.fab != null) {
//            root.fab.setOnClickListener { view ->
//
//                val form: Intent
//                when (mViewPager.currentItem) {
//                    0 -> if (Utils.getLicenseName(requireContext()).isNotEmpty()) {
//                        // Seleccion de licencia a la que asociar la guia
//                        val dialog: DialogFragment = GuiaDialogFragment()
//                        dialog.show(parentFragmentManager, "NewGuiaDialogFragment")
//                    } else {
//                        Snackbar.make(view, R.string.dialog_guia_fail,
//                                Snackbar.LENGTH_LONG)
//                                .setAction(android.R.string.ok, null)
//                                .show()
//                    }
//                    1 -> if (guias!!.size > 0) {
//                        //                                form = new Intent(FragmentMainActivity.this, CompraFormActivity.class);
//                        //                                startActivityForResult(form, COMPRA_COMPLETED);
//                        val dialog: DialogFragment = CompraDialogFragment()
//                        dialog.show(parentFragmentManager, "NewCompraDialogFragment")
//                    } else {
//                        Snackbar.make(view, "Debe introducir una guia primero",
//                                Snackbar.LENGTH_LONG)
//                                .setAction(android.R.string.ok, null)
//                                .show()
//                    }
//                    2 -> {
//                        //                        form = Intent(this@FragmentMainActivity, LicenciaFormActivity::class.java)
//                        //                        startActivityForResult(form, LICENCIA_COMPLETED)
//                    }
//                    3 -> {
//                        //                        form = Intent(this@FragmentMainActivity, TiradaFormActivity::class.java)
//                        //                        startActivityForResult(form, TIRADA_COMPLETED)
//                    }
//                }
//                mActionModeCallback.onDestroyActionMode(
//                        (mActionMode)!!)
//            }
//        }

        return root
    }

    /**
     * Dispatch onPause() to fragments.
     */
    override fun onStart() {
        super.onStart()
//        updateGastoMunicion()
        saveUserInFirebase()
        mAuth.addAuthStateListener(this)

        // Gestion de anuncios
        prefs = activity?.getSharedPreferences("Preferences", Context.MODE_PRIVATE)
        if (prefs!!.getBoolean(PREFS_SHOW_ADS, true)) {
            login_adView.visibility = View.VISIBLE
            login_adView.isEnabled = true
            login_adView.loadAd(Utils.getAdRequest(login_adView))
            mInterstitialAd.loadAd(Utils.getAdRequest(login_adView))
            if (mInterstitialAd.isLoaded) {
                mInterstitialAd.show()
            }
        } else {
            login_adView.visibility = View.GONE
            login_adView.isEnabled = false
        }
    }

    override fun onPause() {
        super.onPause()
        val cm = getSystemService(requireContext(), ConnectivityManager::class.java)!!
        if (cm.isDefaultNetworkActive) {
            saveLists()
        }
    }

    override fun onStop() {
        super.onStop()
        dbSqlHelper!!.close()
        mAuth.removeAuthStateListener(this)
    }

    /**
     * Metodo para cargar las listas en función de su conectividad.
     * En caso de tener, se cargarán las listas de internet.
     * En caso contrario, se cargarán de la BBDD local
     */
    private fun loadLists() {
        // Obtenemos las estructuras de datos
        if (guias == null) {
            guias = activity?.intent?.getParcelableArrayListExtra("guias")
        }
        if (compras == null) {
            compras = activity?.intent?.getParcelableArrayListExtra("compras")
        }
        if (licencias == null) {
            licencias = activity?.intent?.getParcelableArrayListExtra("licencias")
        }
        if (tiradas == null) {
            tiradas = activity?.intent?.getParcelableArrayListExtra("tiradas")
        }
    }

    private fun showTextEmptyList() {
        textEmptyList!!.visibility = View.GONE
        when (mViewPager.currentItem) {
            0 -> if (guias!!.size == 0) {
                textEmptyList!!.visibility = View.VISIBLE
                textEmptyList!!.setText(R.string.guia_empty_list)
            } else textEmptyList!!.visibility = View.GONE
            1 -> if (compras!!.size == 0) {
                textEmptyList!!.visibility = View.VISIBLE
                textEmptyList!!.setText(R.string.compra_empty_list)
            } else textEmptyList!!.visibility = View.GONE
            2 -> if (licencias!!.size == 0) {
                textEmptyList!!.visibility = View.VISIBLE
                textEmptyList!!.setText(R.string.licencia_empty_list)
            } else textEmptyList!!.visibility = View.GONE
            3 -> if (tiradas!!.size == 0) {
                textEmptyList!!.visibility = View.VISIBLE
                textEmptyList!!.setText(R.string.tiradas_empty_list)
            } else textEmptyList!!.visibility = View.GONE
            else -> textEmptyList!!.visibility = View.GONE
        }
    }

    private fun openForm(position: Int) {
        val form: Intent
        when (mViewPager.currentItem) {
            0 -> {
//                form = Intent(this@FragmentMainActivity, GuiaFormActivity::class.java)
//                form.putExtra("modify_guia", guias!![position])
//                form.putExtra("position", position)
//                startActivityForResult(form, GUIA_UPDATED)
            }
            1 -> {
//                form = Intent(this@FragmentMainActivity, CompraFormActivity::class.java)
//                form.putExtra("modify_compra", compras!![position])
//                form.putExtra("position", position)
//                startActivityForResult(form, COMPRA_UPDATED)
            }
            2 -> {
//                form = Intent(this@FragmentMainActivity, LicenciaFormActivity::class.java)
//                form.putExtra("modify_licencia", licencias!![position])
//                form.putExtra("position", position)
//                startActivityForResult(form, LICENCIA_UPDATED)
            }
            3 -> {
//                form = Intent(this@FragmentMainActivity, TiradaFormActivity::class.java)
//                form.putExtra("modify_tirada", tiradas!![position])
//                form.putExtra("position", position)
//                startActivityForResult(form, TIRADA_UPDATED)
            }
        }
    }

    private fun deleteSelectedItems(position: Int) {
        when (mViewPager.currentItem) {
            0 -> if (guias != null && guias!!.size > 0) {
                guias!!.removeAt(position)
                guiaArrayAdapter!!.notifyDataSetChanged()
            }
            1 -> try {
                //Actualizar cupo de la guia correspondiente
                val guia = guias!![compras!![position].idPosGuia]
                val unidadesComprada = compras!![position].unidades
                guia.gastado = guia.gastado - unidadesComprada

                //Borrado de la compra
                compras!!.removeAt(position)
                compraArrayAdapter!!.notifyDataSetChanged()
                guiaArrayAdapter!!.notifyDataSetChanged()
            } catch (ex: IndexOutOfBoundsException) {
                Log.e(activity?.packageName, "Fallo con los index al borrar una compra", ex)
            }
            2 -> {
                //Si existe alguna conexion, no se podra eliminar la licencia
                if (Utils.licenseCanBeDeleted(position)) {
                    Toast.makeText(requireContext(), R.string.delete_license_fail, Toast.LENGTH_LONG).show()
                } else {
                    try {
                        Utils.removeNotificationFromSharedPreference(requireContext(),
                                licencias!![position].numLicencia)
                    } catch (ex: Exception) {
                        Log.wtf(activity?.packageName, "Fallo al listar las notificaciones", ex)
                    }
                    // Eliminacion evento Calendario
                    deleteSameDayEventCalendar(position)
                    deleteMonthBeforeEventCalendar(position)
                    licencias!!.removeAt(position)
                    licenciaArrayAdapter!!.notifyDataSetChanged()
                }
                if (tiradas != null && tiradas!!.size > 0) {
                    tiradas!!.removeAt(position)
                    tiradaArrayAdapter!!.notifyDataSetChanged()
                    PlaceholderFragment.updateInfoTirada()
                }
            }
            3 -> if (tiradas != null && tiradas!!.size > 0) {
                tiradas!!.removeAt(position)
                tiradaArrayAdapter!!.notifyDataSetChanged()
                PlaceholderFragment.updateInfoTirada()
            }
        }
        try {
            // Guardado en la BBDD local de las estructuras de datos
            dbSqlHelper!!.saveListGuias(null, guias)
            dbSqlHelper!!.saveListCompras(null, compras)
            dbSqlHelper!!.saveListLicencias(null, licencias)
            dbSqlHelper!!.saveListTiradas(null, tiradas)
        } catch (ex: Exception) {
            FirebaseCrash.report(ex)
            FirebaseCrash.logcat(Log.ERROR, TAG, "NPE caught")
        }
    }

    /**
     * Metodo para eliminar un evento del calendario del sistema despues de que el usuario elimine una licencia
     */
    private fun deleteSameDayEventCalendar(position: Int) {
        // Se comprueba el permiso de lectura del calendario porque te obliga la implementacion de ContentResolver Query
        // No tendria que ser necesario hacerlo porque ya se han comprobado los permisos de lectura y escritura en el
        // guardado de las licencias. Si el usuario no los ha aceptado no puede guardar una licencia y por tanto tampoco eliminarla
        if (ActivityCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            // Inicio preparacion eliminacion evento
            val cursor: Cursor?
            var startDay: Long = 0
            var endDay: Long = 0
            try {
                // Fecha inicio evento
                val beginTime = Calendar.getInstance()
                beginTime.time = SimpleDateFormat("dd/MM/yyyy")
                        .parse(licencias!![position].fechaCaducidad)
                beginTime[Calendar.HOUR_OF_DAY] = 0
                beginTime[Calendar.MINUTE] = 0
                beginTime[Calendar.SECOND] = 0
                startDay = beginTime.timeInMillis
                // Fecha final evento
                val endTime = Calendar.getInstance()
                endTime.time = SimpleDateFormat("dd/MM/yyyy")
                        .parse(licencias!![position].fechaCaducidad)
                endTime[Calendar.HOUR_OF_DAY] = 23
                endTime[Calendar.MINUTE] = 59
                endTime[Calendar.SECOND] = 59
                endDay = endTime.timeInMillis
            } catch (e: ParseException) {
                e.printStackTrace()
                Log.e(TAG, "Fallo al eliminar el evento del calendario", e)
            }
            // Preparacion de la query
            val title = "Tu licencia caduca hoy"
            val description =
                    Utils.getStringLicenseFromId(requireContext(),
                            licencias!![position].tipo) + ": " + licencias!![position].numLicencia
            val projection =
                    arrayOf(BaseColumns._ID, CalendarContract.Events.TITLE,
                            CalendarContract.Events.DESCRIPTION, CalendarContract.Events.DTSTART)
            val selection =
                    (CalendarContract.Events.DTSTART + " >= ? AND " + CalendarContract.Events.DTSTART + " <= ? AND "
                            + CalendarContract.Events.TITLE + " = ? AND " + CalendarContract.Events.DESCRIPTION + " = ? ")
            val selectionArgs =
                    arrayOf(java.lang.Long.toString(startDay), java.lang.Long.toString(endDay), title,
                            description)
            // Primero se recupera el id del evento a eliminar
            cursor = activity?.contentResolver?.query(CalendarContract.Events.CONTENT_URI, projection, selection,
                    selectionArgs,
                    null)
            while (cursor!!.moveToNext()) {
                val eventId = cursor.getLong(cursor.getColumnIndex("_id"))
                // Despues se elimina el evento en funcion de su id
                activity?.contentResolver?.delete(
                        ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId), null,
                        null)
            }
            cursor.close()
        }
    }

    // Lo mismo que el anterior metodo pero se elimina el evento con un mes de antelacion
    private fun deleteMonthBeforeEventCalendar(position: Int) {
        // Se comprueba el permiso de lectura del calendario porque te obliga la implementacion de ContentResolver Query
        // No tendria que ser necesario hacerlo porque ya se han comprobado los permisos de lectura y escritura en el
        // guardado de las licencias. Si el usuario no los ha aceptado no puede guardar una licencia y por tanto tampoco eliminarla
        if (ActivityCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            // Inicio preparacion eliminacion evento
            val cursor: Cursor?
            var startDay: Long = 0
            var endDay: Long = 0
            try {
                // Fecha inicio evento
                val beginTime = Calendar.getInstance()
                beginTime.time = SimpleDateFormat("dd/MM/yyyy")
                        .parse(licencias!![position].fechaCaducidad)
                // Un mes de antelacion
                beginTime.add(Calendar.MONTH, -1)
                beginTime[Calendar.HOUR_OF_DAY] = 0
                beginTime[Calendar.MINUTE] = 0
                beginTime[Calendar.SECOND] = 0
                startDay = beginTime.timeInMillis
                // Fecha final evento
                val endTime = Calendar.getInstance()
                endTime.time = SimpleDateFormat("dd/MM/yyyy")
                        .parse(licencias!![position].fechaCaducidad)
                // Un mes de antelacion
                endTime.add(Calendar.MONTH, -1)
                endTime[Calendar.HOUR_OF_DAY] = 23
                endTime[Calendar.MINUTE] = 59
                endTime[Calendar.SECOND] = 59
                endDay = endTime.timeInMillis
            } catch (e: ParseException) {
                e.printStackTrace()
                Log.e(TAG, "Fallo al eliminar el evento del calendario", e)
            }
            // Preparacion de la query
            val title = "Tu licencia caduca dentro de un mes"
            val description =
                    Utils.getStringLicenseFromId(requireContext(),
                            licencias!![position].tipo) + ": " + licencias!![position].numLicencia
            val projection =
                    arrayOf(BaseColumns._ID, CalendarContract.Events.TITLE,
                            CalendarContract.Events.DESCRIPTION, CalendarContract.Events.DTSTART)
            val selection =
                    (CalendarContract.Events.DTSTART + " >= ? AND " + CalendarContract.Events.DTSTART + " <= ? AND "
                            + CalendarContract.Events.TITLE + " = ? AND " + CalendarContract.Events.DESCRIPTION + " = ? ")
            val selectionArgs =
                    arrayOf(java.lang.Long.toString(startDay), java.lang.Long.toString(endDay), title,
                            description)

            // Primero se recupera el id del evento a eliminar
            cursor = activity?.contentResolver?.query(CalendarContract.Events.CONTENT_URI, projection, selection,
                    selectionArgs,
                    null)
            while (cursor!!.moveToNext()) {
                val eventId = cursor.getLong(cursor.getColumnIndex("_id"))
                // Despues se elimina el evento en funcion de su id
                activity?.contentResolver?.delete(
                        ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId), null,
                        null)
            }
            cursor.close()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_main, menu)
        when (mViewPager.currentItem) {
            0, 1, 2 -> menu.findItem(R.id.tabla_tiradas).isVisible = false
            3 -> menu.findItem(R.id.tabla_tiradas).isVisible = true
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(requireContext(), SettingsFragment::class.java)
                startActivity(intent)
            }
            R.id.tabla_tiradas -> try {
                val bitmap = BitmapFactory.decodeResource(resources, R.drawable.image_table)
                Utils.showImage(requireContext(), bitmap, "table")
            } catch (ex: Exception) {
                Log.e(TAG, "Error mostrando la tabla de tiradas")
                FirebaseCrash.logcat(Log.ERROR, TAG,
                        "Error mostrando la tabla de tiradas")
                FirebaseCrash.report(ex)
            }
            else -> return false
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Recepción de los datos del formulario
     *
     * @param requestCode Código de peticion
     * @param resultCode  Código de resultado de la operacion
     * @param data        Intent con los datos de respuesta
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        Trace myTrace = FirebasePerformance.getInstance().newTrace("onActivityResult_FragmentMainActivity");
//        myTrace.start();
        super.onActivityResult(requestCode, resultCode, data)
        val localImageBitmap: Bitmap?
        val firebaseImageBitmap: Bitmap?
        // Check which request we're responding to
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> try {
                    firebaseImageBitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver,
                            Uri.fromFile(File(fileImagePath)))
                    localImageBitmap = ThumbnailUtils.extractThumbnail(
                            BitmapFactory.decodeFile(fileImagePath),
                            (firebaseImageBitmap.width * 0.2).toInt(),
                            (firebaseImageBitmap.height * 0.2).toInt() /*,
                                ThumbnailUtils.OPTIONS_RECYCLE_INPUT*/)

                    // No se necesita esta función
//                        imageBitmap = Utils.resizeImage(imageBitmap, null);
                    updateImage(localImageBitmap, firebaseImageBitmap)
                } catch (ex: Exception) {
                    Log.e(TAG, "Error obteniendo la imagen de la camara",
                            ex)
                }
                GUIA_COMPLETED -> {
                    guias!!.add(Guia(data!!.extras!!))
                    guiaArrayAdapter!!.notifyDataSetChanged()
                }
                COMPRA_COMPLETED -> {
                    val newCompra = Compra(data!!.extras!!)
                    compras!!.add(newCompra)
                    compraArrayAdapter!!.notifyDataSetChanged()
                }
                LICENCIA_COMPLETED -> {
                    licencias!!.add(
                            Licencia(data!!.extras!!.getParcelable<Parcelable>("modify_licencia") as Licencia))
                    licenciaArrayAdapter!!.notifyDataSetChanged()
                }
                TIRADA_COMPLETED -> {
                    tiradas!!.add(
                            Tirada(data!!.extras!!.getParcelable<Parcelable>("modify_tirada") as Tirada))
                    PlaceholderFragment.updateInfoTirada()
                    tiradaArrayAdapter!!.notifyDataSetChanged()
                }
                GUIA_UPDATED -> updateGuia(data)
                COMPRA_UPDATED -> updateCompra(data)
                LICENCIA_UPDATED -> updateLicencia(data)
                TIRADA_UPDATED -> PlaceholderFragment.updateInfoTirada(
                        data)
            }
        } else if (resultCode == RESULT_CANCELED) {
            Log.e(TAG, "Resultado de la camara cancelada")
        }
        try {
            // Guardado en la BBDD local de las estructuras de datos
            dbSqlHelper!!.saveListGuias(null, guias)
            dbSqlHelper!!.saveListCompras(null, compras)
            dbSqlHelper!!.saveListLicencias(null, licencias)
            dbSqlHelper!!.saveListTiradas(null, tiradas)
        } catch (ex: Exception) {
            FirebaseCrash.report(ex)
            FirebaseCrash.logcat(Log.ERROR, TAG, "NPE caught")
        }
        showTextEmptyList()

//        myTrace.stop();
    }

    private fun saveLists() {
        try {
            //Borrado de la base de datos actual;
            if (userRef != null) {
                userRef!!.child("db").removeValue { databaseError, databaseReference ->
                    userRef!!.child("db").child("guias").setValue(guias)
                    userRef!!.child("db").child("compras").setValue(compras)
                    userRef!!.child("db").child("licencias").setValue(licencias)
                    userRef!!.child("db").child("tiradas").setValue(tiradas)
                    Log.i(TAG, "Guardado de listas en Firebase")
                }
            } else {
                FirebaseCrash.logcat(Log.WARN, TAG,
                        "Fallo al  guardar las listas, usuario a null")
            }
        } catch (ex: Exception) {
            FirebaseCrash.logcat(Log.ERROR, TAG,
                    "Fallo guardando las listas")
            FirebaseCrash.report(ex)
        }
    }

    /**
     * Recalculamos el gasto de municion de todas las guias, recorriendo las compras
     */
    private fun updateGastoMunicion() {
        //Reseteo de los gastos de todas las guias
        for (guia: Guia in guias!!) {
            guia.gastado = 0
        }

        // Recalculamos todos los gastos
        for (comp: Compra in compras!!) {
            val guia: Guia = guias!![comp.idPosGuia]
            val currentYear: Int =
                    activity?.getSharedPreferences("Preferences", Context.MODE_PRIVATE)?.getInt("year", 0)!!
            try {
                if (currentYear != 0) {
                    //Sumaamos solo las compras del año en el que estamos
                    val fechaCompra = Calendar.getInstance()
                    fechaCompra.time = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .parse(comp.fecha)
                    if (currentYear == fechaCompra[Calendar.YEAR]) {
                        guia.gastado = guia.gastado + comp.unidades
                    }
                }
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
        if (guiaArrayAdapter == null) guiaArrayAdapter = GuiaArrayAdapter(requireContext(), R.layout.guia_item, guias)
        guiaArrayAdapter!!.notifyDataSetChanged()
    }

    private fun updateImage(LocalImageBitmap: Bitmap?, FirebaseImageBitmap: Bitmap?) {
        if (LocalImageBitmap != null && FirebaseImageBitmap != null) {
            try {
                //Guardado en disco de la imagen tomada con la foto
                Utils.saveBitmapToFile(LocalImageBitmap)
                //Guardado de la imagen en Firebase
                Utils.saveBitmapToFirebase(mStorage, FirebaseImageBitmap,
                        fileImagePath, mAuth.currentUser!!.uid)
            } catch (ex: Exception) {
                Log.e(TAG, "Error guarando la imagen en Firebase", ex)
            }
            when (mViewPager.currentItem) {
                0 -> {
                    guias!![imagePosition].imagePath = fileImagePath
                    guiaArrayAdapter!!.notifyDataSetChanged()
                }
                1 -> {
                    compras!!.get(imagePosition).imagePath = fileImagePath
                    compraArrayAdapter!!.notifyDataSetChanged()
                }
            }
        } else Log.e(TAG, "Imagen Null. No se han guardado las imagenes")
    }

    private fun updateGuia(data: Intent?) {
        if (data!!.extras != null) {
            val position = data.extras!!.getInt("position", -1)
            val guia = guias!![position]

            //TODO: Refactorizar y cambiar esto como en licencias. Hacer que el intent devuelva un objeto Guia y no los campos individualizados.
//            guia.setIdCompra(data.getExtras().getInt(""));
            guia.tipoLicencia = data.extras!!.getInt("tipoLicencia")
            guia.marca = data.extras!!.getString("marca")
            guia.modelo = data.extras!!.getString("modelo")
            guia.apodo = data.extras!!.getString("apodo")
            guia.tipoArma = data.extras!!.getInt("tipoArma")
            guia.calibre1 = data.extras!!.getString("calibre1")
            guia.calibre2 = data.extras!!.getString("calibre2")
            guia.numGuia = data.extras!!.getString("numGuia")
            guia.numArma = data.extras!!.getString("numArma")
            guia.imagePath = data.extras!!.getString("imagePath")
            guia.cupo = data.extras!!.getInt("cupo")
            guia.gastado = data.extras!!.getInt("gastado")
            guiaArrayAdapter!!.notifyDataSetChanged()
            compraArrayAdapter!!.notifyDataSetChanged()
        }
    }

    private fun updateCompra(data: Intent?) {
        if (data!!.extras != null) {
            val position = data.extras!!.getInt("position", -1)
            val compra = compras!![position]

            //TODO: Refactorizar y cambiar esto como en licencias. Hacer que el intent devuelva un objeto Compra y no los campos individualizados.
            compra.idPosGuia = data.extras!!.getInt("idPosGuia")
            compra.calibre1 = data.extras!!.getString("calibre1")
            compra.calibre2 = data.extras!!.getString("calibre2")
            compra.unidades = data.extras!!.getInt("unidades")
            compra.precio = data.extras!!.getDouble("precio")
            compra.fecha = data.extras!!.getString("fecha")
            compra.tipo = data.extras!!.getString("tipo")
            compra.peso = data.extras!!.getInt("peso")
            compra.marca = data.extras!!.getString("marca")
            compra.tienda = data.extras!!.getString("tienda")
            compra.valoracion = data.extras!!.getFloat("valoracion")
            compra.imagePath = data.extras!!.getString("imagePath")
            compraArrayAdapter!!.notifyDataSetChanged()
        }
    }

    private fun updateLicencia(data: Intent?) {
        if (data!!.extras != null) {
            val position = data.extras!!.getInt("position", -1)
            val licencia = Licencia(data.extras!!["modify_licencia"] as Licencia)
            licencias!![position] = licencia
            licenciaArrayAdapter!!.notifyDataSetChanged()
        }
    }

    /**
     * Dialog para la seleccion de la licencia qu
     */
    private fun saveUserInFirebase() {
        try {
            //Guardado del usuario en las shared preferences del dispositivo
            val email = Utils.getUserEmail(requireContext())
            val pass = prefs!!.getString("password", "")
            if (email.isNotEmpty()) {
                //Obtención del código de autentificación del usuario
                mAuth.createUserWithEmailAndPassword(email, (pass)!!)
                        .addOnCompleteListener(requireActivity()) { task ->
                            Log.d(TAG,
                                    "createUserWithEmail:onComplete:" + task.isSuccessful)

                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful) {
                                //                                Toast.makeText(context, R.string.auth_usuario_existente, Toast.LENGTH_SHORT).show();
                                Log.w(TAG, task.exception?.message ?: "Error")
                            }
                        }
                mAuth.signInWithEmailAndPassword(email, (pass)).addOnCompleteListener(requireActivity()
                ) { task ->
                    Log.d(TAG,
                            "signInWithEmail:onComplete:" + task.isSuccessful)

                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful) {
                        Log.w(TAG, "signInWithEmail:failed",
                                task.exception)
                        //                                Toast.makeText(context, R.string.auth_usuario_logado, Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                mAuth.signInAnonymously()
                        .addOnCompleteListener(requireActivity()) { task ->
                            Log.d(TAG,
                                    "signInAnonymously:onComplete:" + task.isSuccessful)

                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful) {
                                Log.w(TAG, "signInAnonymously",
                                        task.exception)
                                //                                Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        }
            }
        } catch (ex: Exception) {
            FirebaseCrash.logcat(Log.ERROR, TAG,
                    "Fallo al iniciar la base de datos de firebase.")
            FirebaseCrash.report(ex)
        }
    }

    override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
        try {
            val user = firebaseAuth.currentUser
            if (user != null) {
                // User is signed in
                Log.w(TAG, "onAuthStateChanged:signed_in:" + user.uid)

                //Cargamos la información del usuario
                userRef = FirebaseDatabase.getInstance().reference.child("users").child(user.uid)
                userRef!!.child("email").setValue(user.email)
                userRef!!.child("pass").setValue(prefs!!.getString("password", ""))
                userRef!!.child("settings").child("ads")
                        .setValue(prefs!!.getBoolean(PREFS_SHOW_ADS, true))
                //                userRef.child("settings").child("ads_admin").setValue(prefs.getBoolean(PREFS_SHOW_ADS, true));

//                userRef.child("settings").child("ads_admin").addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        // Si no existe, lo creamos
//                        if (!dataSnapshot.exists()) {
//                            dataSnapshot.getRef().setValue(prefs.getBoolean(PREFS_SHOW_ADS, true));
//                        }
//
//                        if (Boolean.parseBoolean(dataSnapshot.getValue().toString())) {
//                            mAdView.setVisibility(View.VISIBLE);
//                            mAdView.setEnabled(true);
//                            mAdView.loadAd(Utils.getAdRequest(mAdView));
//                        } else {
//                            mAdView.setVisibility(View.GONE);
//                            mAdView.setEnabled(false);
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//                        FirebaseCrash.logcat(Log.WARN, TAG, "Error en el control de ads_admin");
//                    }
//                });
            } else {
                // User is signed out
                Log.w(TAG, "onAuthStateChanged:signed_out")
            }
        } catch (ex: Exception) {
            FirebaseCrash.logcat(Log.ERROR, TAG,
                    "Fallo al obtener el usuario para la inserccion en la BBDD de Firebase.")
            FirebaseCrash.report(ex)
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment : Fragment() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val rootView = inflater.inflate(R.layout.list_view_pager, container, false)
            listView = rootView.findViewById(R.id.ListView)

            tiradaCountDown = rootView.findViewById(R.id.pager_tirada_countdown)
            try {
                if (tiradaCountDown != null) {
                    tiradaCountDown!!.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                }
                when (requireArguments().getInt(
                        ARG_SECTION_NUMBER)) {
                    0 -> {
                        (tiradaCountDown as View).visibility = View.GONE
                        guiaArrayAdapter = GuiaArrayAdapter(activity, R.layout.guia_item, guias)
                        (listView as ListView).adapter = guiaArrayAdapter
                    }
                    1 -> {
                        (tiradaCountDown as View).visibility = View.GONE
                        compraArrayAdapter =
                                CompraArrayAdapter(activity, R.layout.compra_item, compras)
                        (listView as ListView).adapter = compraArrayAdapter
                    }
                    2 -> try {
                        (tiradaCountDown as View).visibility = View.GONE
                        licenciaArrayAdapter = LicenciaArrayAdapter(activity, R.layout.licencia_item, licencias)
                        (listView as ListView).adapter = licenciaArrayAdapter
                    } catch (ex: Exception) {
                        FirebaseCrash.logcat(Log.ERROR, TAG, ex.message)
                        FirebaseCrash.report(ex)
                    }
                    3 -> try {
                        if (tiradas!!.size > 0) {
                            (tiradaCountDown as View).visibility = View.VISIBLE
                        } else {
                            (tiradaCountDown as View).visibility = View.GONE
                        }
                        tiradaArrayAdapter =
                                TiradaArrayAdapter(activity, R.layout.tirada_item,
                                        tiradas)
                        (listView as ListView).adapter = tiradaArrayAdapter
                        updateInfoTirada()
                    } catch (ex: Exception) {
                        FirebaseCrash.logcat(Log.ERROR, TAG,
                                "Fallo al actualizar la lista de tiradas")
                        FirebaseCrash.report(ex)
                    }
                }
                (listView as ListView).choiceMode = AbsListView.CHOICE_MODE_SINGLE
                (listView as ListView).onItemLongClickListener =
                        OnItemLongClickListener { parent: AdapterView<*>?, view: View, position: Int, id: Long ->
                            if (mActionMode != null) {
                                return@OnItemLongClickListener false
                            }
                            view.isSelected = true
                            auxView = view

                            // Start the CAB using the ActionMode.Callback defined above
                            mActionMode =
                                    requireActivity().startActionMode(mActionModeCallback)
                            assert(mActionMode != null)
                            mActionMode!!.setTitle(R.string.menu_cab_title)
                            mActionMode!!.tag = position
                            true
                        }
                (listView as ListView).onItemClickListener =
                        AdapterView.OnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
                            if (mActionMode != null) mActionMode!!.finish()
                            mActionMode = null
                        }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return rootView
        }

        companion object {
            /**
             * The fragment argument representing the section number for this fragment.
             */
            private val ARG_SECTION_NUMBER = "section_number"
            private var context: Context? = null

            /**
             * Returns a new instance of this fragment for the given section
             * number.
             */
            fun newInstance(sectionNumber: Int,
                            mContext: Context?): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                val args = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
                fragment.arguments = args
                context = mContext
                return fragment
            }

            /**
             * @param data
             */
            fun updateInfoTirada(data: Intent?) {
                if (data!!.extras != null) {
                    val position = data.extras!!.getInt("position", -1)
                    val tirada = Tirada(data.extras!!["modify_tirada"] as Tirada)
                    tiradas!![position] = tirada
                }
                updateInfoTirada()
            }

            /**
             *
             */
            fun updateInfoTirada() {
                try {
                    // Ordenamos el array de tiradas por fecha descendente (la mas actual arriba)
                    tiradas?.sortWith(Comparator { date1, date2 ->
                        Utils.getDateFromString(date2.fecha)
                                .compareTo(Utils.getDateFromString(
                                        date1.fecha))
                    })
                    if (tiradas!!.size > 0 && tiradaCountDown != null) {
                        tiradaCountDown!!.visibility = View.VISIBLE
                    } else {
                        if (tiradaCountDown != null) tiradaCountDown!!.visibility = View.GONE
                    }
                    if (tiradas!!.size > 0) updateCaducidadLicenciaTirada()
                } catch (ex: IndexOutOfBoundsException) {
                    Log.e(TAG, "Error calculando la caducidad de la tirada", ex)
//                  FirebaseCrash.logcat(Log.ERROR, TAG, "Error calculando la caducidad de la tirada");
//                  FirebaseCrash.report(ex);
                } catch (ex: Exception) {
                    Log.e(TAG, ex.message, ex)
//                    FirebaseCrash.logcat(Log.ERROR, TAG, ex.getMessage());
//                    FirebaseCrash.report(ex);
                }
            }

            /**
             *
             */
            @Throws(IndexOutOfBoundsException::class)
            private fun updateCaducidadLicenciaTirada() {
                val daysRemain = Math.round(Tirada.millisUntilExpiracy(
                        tiradas!![0]).toFloat() / (1000 * 60 * 60 * 24))
                //                    int horasRemain = Math.round(millisUntilFinished / (1000 * 60 * 60 * 24));
//                    int minutosRemain = Math.round(millisUntilFinished / (1000 * 60 * 60));
//                    int segundosRemain = Math.round(millisUntilFinished / (1000 * 60));
                val sb = StringBuilder()
                val formatter = Formatter(sb)
                val text = formatter.format(
                        context!!.getString(
                                R.string.lbl_caducidad_tirada), daysRemain).toString()
                if (tiradaCountDown != null) {
                    tiradaCountDown!!.text = text
                }
                try {
                    if (daysRemain <= 10) {
                        tiradaCountDown!!.setBackgroundColor(ContextCompat.getColor(
                                (context)!!,
                                android.R.color.holo_red_dark))
                    } else {
                        tiradaCountDown!!.setBackgroundColor(ContextCompat.getColor(
                                (context)!!,
                                R.color.colorPrimary))
                    }
                } catch (ex: Exception) {
                    Log.e(TAG,
                            "Error \"controlado\" en getColor en el label de tiradas", ex)
                    FirebaseCrash.logcat(Log.ERROR, TAG,
                            "Error \"controlado\" en getColor en el label de tiradas")
                    FirebaseCrash.report(ex)
                }
            }
        }
    }

    /**
     * Dialog para la seleccion de la licencia
     */
    class GuiaDialogFragment : DialogFragment() {
        //https://developer.android.com/guide/topics/ui/dialogs.html
        private var selectedLicense = 0
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder =
                    AlertDialog.Builder((activity)!!)

            // Set title
            builder.setTitle(R.string.dialog_licencia_title) // Set items
                    .setSingleChoiceItems(Utils.getLicenseName(activity), 0
                    ) { _: DialogInterface?, i: Int ->
                        //                            Toast.makeText(getActivity(), "Seleccionado: " + (String) getGuiaName()[i], Toast.LENGTH_SHORT).show();
                        selectedLicense = i
                    } // Add action buttons
                    .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                        // Alberto H (10/1/2017):
                        // Comentada la condicion de obligar al usuario a tener la licencia
                        // federativa para poder crear la licencia F - Tiro olimpico.
                        //                            String tipoLicencia = (String) Utils.getLicenseName(getActivity())[selectedLicense];
                        //                            if (tipoLicencia.equals("F - Tiro olimpico")) {
                        //                                if (Utils.isLicenciaFederativa(getActivity())) {
                        //                                    Intent form = new Intent(getActivity(), GuiaFormActivity.class);
                        //                                    form.putExtra("tipo_licencia", (String) Utils.getLicenseName(getActivity())[selectedLicense]);
                        //                                    getActivity().startActivityForResult(form, FragmentMainActivity.GUIA_COMPLETED);
                        //                                } else {
                        //                                    Toast.makeText(getActivity(), R.string.dialog_guia_licencia_federativa, Toast.LENGTH_LONG).show();
                        //                                    GuiaDialogFragment.this.getDialog().dismiss();
                        //                                }
                        //                            } else {
                        val form = Intent(requireActivity(), GuiaFormActivity::class.java)
                        form.putExtra("tipo_licencia",
                                Utils.getLicenseName(requireActivity())[selectedLicense] as String?)
                        requireActivity().startActivityForResult(form, GUIA_COMPLETED)
                    }
                    .setNegativeButton(android.R.string.cancel) { _: DialogInterface?, _: Int ->
                        dialog!!.cancel()
                    }
            return builder.create()
        }
    }

    /**
     * Dialog para la seleccion de la licencia qu
     */
    class CompraDialogFragment : DialogFragment() {
        //https://developer.android.com/guide/topics/ui/dialogs.html
        private var selectedGuia = 0
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder =
                    AlertDialog.Builder((activity)!!)

            // Set title
            builder.setTitle(R.string.dialog_guia_title) // Set items
                    .setSingleChoiceItems(guiaName, 0) { _: DialogInterface?, pos: Int ->
                        selectedGuia = pos
                    } // Add action buttons
                    .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                        //                            if (pos >= 0)
                        //                                Toast.makeText(getActivity(), "Seleccionado: " + getGuiaName()[pos].toString(), Toast.LENGTH_SHORT).show();
                        val form = Intent(activity, CompraFormActivity::class.java)
                        form.putExtra("position_guia", selectedGuia)
                        form.putExtra("guia", guias!![selectedGuia])
                        requireActivity().startActivityForResult(form,
                                COMPRA_COMPLETED)
                    }
                    .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
            return builder.create()
        }

        private val guiaName: Array<CharSequence>
            get() {
                val list = ArrayList<String>()
                for (guia: Guia in guias!!) {
                    list.add(guia.apodo ?: "")
                }
                return list.toTypedArray()
            }
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager?) :
            FragmentPagerAdapter((fm)!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position, requireContext())
        }

        override fun getCount(): Int {
            return 4
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return getString(R.string.section_guias_title)
                1 -> return getString(R.string.section_compras_title)
                2 -> return getString(R.string.section_licencias_title)
                3 -> return getString(R.string.section_competiciones_title)
            }
            return null
        }
    }

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 100
        const val GUIA_COMPLETED = 10
        const val COMPRA_COMPLETED = 11
        private const val LICENCIA_COMPLETED = 12
        private const val TIRADA_COMPLETED = 13
        private const val GUIA_UPDATED = 20
        private const val COMPRA_UPDATED = 21
        private const val LICENCIA_UPDATED = 22
        private const val TIRADA_UPDATED = 23
        private const val TAG = "FragmentMainActivity"

        lateinit var fileImagePath: String
        lateinit var auxView: View
        var mActionMode: ActionMode? = null
        lateinit var mActionModeCallback: ActionMode.Callback

        @JvmField
        var imagePosition = 0

        @JvmField
        var guias: ArrayList<Guia>? = null

        @JvmField
        var compras: ArrayList<Compra>? = null

        @JvmField
        var licencias: ArrayList<Licencia>? = null
        var tiradas: ArrayList<Tirada>? = null

        /**
         * Constante de la referencia push() del usuario en funcion del correo del dispositivo
         */
        private var guiaArrayAdapter: GuiaArrayAdapter? = null
        private var compraArrayAdapter: CompraArrayAdapter? = null
        private var licenciaArrayAdapter: LicenciaArrayAdapter? = null
        private var tiradaArrayAdapter: TiradaArrayAdapter? = null
        private var listView: ListView? = null
        private var dbSqlHelper: DataBaseSQLiteHelper? = null
        private var tiradaCountDown: TextView? = null
    }
}