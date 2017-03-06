package al.ahgitdevelopment.municion;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.Locale;

import al.ahgitdevelopment.municion.Adapters.CompraArrayAdapter;
import al.ahgitdevelopment.municion.Adapters.GuiaArrayAdapter;
import al.ahgitdevelopment.municion.Adapters.LicenciaArrayAdapter;
import al.ahgitdevelopment.municion.Adapters.TiradaArrayAdapter;
import al.ahgitdevelopment.municion.DataBases.DataBaseSQLiteHelper;
import al.ahgitdevelopment.municion.DataModel.Compra;
import al.ahgitdevelopment.municion.DataModel.Guia;
import al.ahgitdevelopment.municion.DataModel.Licencia;
import al.ahgitdevelopment.municion.DataModel.Tirada;
import al.ahgitdevelopment.municion.Forms.CompraFormActivity;
import al.ahgitdevelopment.municion.Forms.GuiaFormActivity;
import al.ahgitdevelopment.municion.Forms.LicenciaFormActivity;
import al.ahgitdevelopment.municion.Forms.TiradaFormActivity;

import static al.ahgitdevelopment.municion.Utils.PREFS_SHOW_ADS;

public class FragmentMainActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener {

    public static final int REQUEST_IMAGE_CAPTURE = 100;
    public static final int GUIA_COMPLETED = 10;
    public static final int COMPRA_COMPLETED = 11;
    private static final int LICENCIA_COMPLETED = 12;
    private static final int TIRADA_COMPLETED = 13;
    private static final int GUIA_UPDATED = 20;
    private static final int COMPRA_UPDATED = 21;
    private static final int LICENCIA_UPDATED = 22;
    private static final int TIRADA_UPDATED = 23;
    private static final String TAG = "FragmentMainActivity";

    public static String fileImagePath = null;
    public static View auxView = null;
    public static ActionMode mActionMode = null;
    public static ActionMode.Callback mActionModeCallback = null;
    public static int imagePosition;
    public static ArrayList<Guia> guias;
    public static ArrayList<Compra> compras;
    public static ArrayList<Licencia> licencias;
    public static ArrayList<Tirada> tiradas;
    public static TextView textEmptyList = null;
    /**
     * Constante de la referencia push() del usuario en funcion del correo del dispositivo
     */
    private static GuiaArrayAdapter guiaArrayAdapter = null;
    private static CompraArrayAdapter compraArrayAdapter = null;
    private static LicenciaArrayAdapter licenciaArrayAdapter = null;
    private static TiradaArrayAdapter tiradaArrayAdapter = null;
    private static ListView listView;
    private static DataBaseSQLiteHelper dbSqlHelper;
    private static TextView tiradaCountDown;
    private Toolbar toolbar;
    /**
     * The {@link PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private SharedPreferences prefs;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private DatabaseReference userRef;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseStorage mStorage = FirebaseStorage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_main);

        prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);

        mActionModeCallback = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.menu_cab, menu);

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // Here you can perform updates to the CAB due to
                // an invalidate() request
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // Respond to clicks on the actions in the CAB
                switch (item.getItemId()) {
                    case R.id.item_menu_modify:
                        openForm((int) mActionMode.getTag());
                        mode.finish(); // Action picked, so close the CAB
                        break;
                    case R.id.item_menu_delete:
                        try {
                            deleteSelectedItems((int) mActionMode.getTag());
                            showTextEmptyList();
                        } catch (Exception ex) {
                            FirebaseCrash.logcat(Log.ERROR, TAG, "Error al borrar elementos de la lista en el método onActionItemClicked()");
                            FirebaseCrash.report(ex);
                        }
                        mode.finish(); // Action picked, so close the CAB
                        break;
                    default:
                        return false;
                }
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                if (auxView != null)
                    auxView.setSelected(false);

                mActionMode = null;
            }
        };

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_bullseye);

        textEmptyList = (TextView) findViewById(R.id.textEmptyList);

        // Instanciamos la base de datos
        dbSqlHelper = new DataBaseSQLiteHelper(getApplicationContext());

        // Carga de las listas en funcion de la conectividad:
        // - Con conexion: Firebase
        // - Sin conexion: DDBB local
        loadLists();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        if (mViewPager != null && mSectionsPagerAdapter != null)
            mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                invalidateOptionsMenu();
            }

            @Override
            public void onPageSelected(int position) {
                if (mActionMode != null)
                    mActionMode.finish();
                mActionMode = null;

                showTextEmptyList();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        mViewPager.setCurrentItem(2);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.banner_login_intersticial_id));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        if (mViewPager != null)
            tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    //                        .setAction("Action", null).show();
                    Intent form;
                    switch (mViewPager.getCurrentItem()) {
                        case 0:
                            if (Utils.getLicenseName(FragmentMainActivity.this).length > 0) {
                                // Seleccion de licencia a la que asociar la guia
                                DialogFragment dialog = new GuiaDialogFragment();
                                dialog.show(getSupportFragmentManager(), "NewGuiaDialogFragment");
                            } else {
                                Snackbar.make(view, R.string.dialog_guia_fail, Snackbar.LENGTH_LONG)
//                                        .setAction(android.R.string.ok, new View.OnClickListener() {
//                                            @Override
//                                            public void onClick(View view) {
//                                            }
//                                        })
                                        .setAction(android.R.string.ok, null)
                                        .show();
                            }

                            break;
                        case 1:
                            if (guias.size() > 0) {
//                                form = new Intent(FragmentMainActivity.this, CompraFormActivity.class);
//                                startActivityForResult(form, COMPRA_COMPLETED);
                                DialogFragment dialog = new CompraDialogFragment();
                                dialog.show(getSupportFragmentManager(), "NewCompraDialogFragment");
                            } else {
                                Snackbar.make(view, "Debe introducir una guia primero", Snackbar.LENGTH_LONG)
//                                        .setAction(android.R.string.ok, new View.OnClickListener() {
//                                            @Override
//                                            public void onClick(View view) {
//                                            }
//                                        })
                                        .setAction(android.R.string.ok, null)
                                        .show();
                            }

                            break;
                        case 2:
                            form = new Intent(FragmentMainActivity.this, LicenciaFormActivity.class);
                            startActivityForResult(form, LICENCIA_COMPLETED);
                            break;

                        case 3:
                            form = new Intent(FragmentMainActivity.this, TiradaFormActivity.class);
                            startActivityForResult(form, TIRADA_COMPLETED);
                            break;
                    }

                    mActionModeCallback.onDestroyActionMode(mActionMode);
                }
            });
        }
    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onStart() {
        super.onStart();

        updateGastoMunicion();
        saveUserInFirebase();
        mAuth.addAuthStateListener(this);

        // Gestion de anuncios
        mAdView = (AdView) findViewById(R.id.adView);
        prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        if (prefs.getBoolean(PREFS_SHOW_ADS, true)) {
            mAdView.setVisibility(View.VISIBLE);
            mAdView.setEnabled(true);
            mAdView.loadAd(Utils.getAdRequest(mAdView));
            mInterstitialAd.loadAd(Utils.getAdRequest(mAdView));

            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            }
        } else {
            mAdView.setVisibility(View.GONE);
            mAdView.setEnabled(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
            saveLists();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        dbSqlHelper.close();
        mAuth.removeAuthStateListener(this);
    }

    /**
     * Metodo para cargar las listas en función de su conectividad.
     * En caso de tener, se cargarán las listas de internet.
     * En caso contrario, se cargarán de la BBDD local
     */
    private void loadLists() {
        // Obtenemos las estructuras de datos
        if (guias == null) {
            guias = getIntent().getParcelableArrayListExtra("guias");
        }
        if (compras == null) {
            compras = getIntent().getParcelableArrayListExtra("compras");
        }
        if (licencias == null) {
            licencias = getIntent().getParcelableArrayListExtra("licencias");
        }
        if (tiradas == null) {
            tiradas = getIntent().getParcelableArrayListExtra("tiradas");
        }
    }

    private void showTextEmptyList() {
        textEmptyList.setVisibility(View.GONE);

        //En caso de estar la lista vacía, indicamos un texto por defecto.
        switch (mViewPager.getCurrentItem()) {
            case 0:
                if (guias.size() == 0) {
                    textEmptyList.setVisibility(View.VISIBLE);
                    textEmptyList.setText(R.string.guia_empty_list);
                } else
                    textEmptyList.setVisibility(View.GONE);

                break;

            case 1:
                if (compras.size() == 0) {
                    textEmptyList.setVisibility(View.VISIBLE);
                    textEmptyList.setText(R.string.compra_empty_list);
                } else
                    textEmptyList.setVisibility(View.GONE);
                break;

            case 2:
                if (licencias.size() == 0) {
                    textEmptyList.setVisibility(View.VISIBLE);
                    textEmptyList.setText(R.string.licencia_empty_list);
                } else
                    textEmptyList.setVisibility(View.GONE);
                break;

            case 3:
                if (tiradas.size() == 0) {
                    textEmptyList.setVisibility(View.VISIBLE);
                    textEmptyList.setText(R.string.tiradas_empty_list);
                } else
                    textEmptyList.setVisibility(View.GONE);
                break;

            default:
                textEmptyList.setVisibility(View.GONE);
        }
    }

    private void openForm(int position) {
        Intent form;

        switch (mViewPager.getCurrentItem()) {
            case 0:
                form = new Intent(FragmentMainActivity.this, GuiaFormActivity.class);
                form.putExtra("modify_guia", guias.get(position));
                form.putExtra("position", position);
                startActivityForResult(form, GUIA_UPDATED);
                break;

            case 1:
                form = new Intent(FragmentMainActivity.this, CompraFormActivity.class);
                form.putExtra("modify_compra", compras.get(position));
                form.putExtra("position", position);
                startActivityForResult(form, COMPRA_UPDATED);
                break;

            case 2:
                form = new Intent(FragmentMainActivity.this, LicenciaFormActivity.class);
                form.putExtra("modify_licencia", licencias.get(position));
                form.putExtra("position", position);
                startActivityForResult(form, LICENCIA_UPDATED);
                break;

            case 3:
                form = new Intent(FragmentMainActivity.this, TiradaFormActivity.class);
                form.putExtra("modify_tirada", tiradas.get(position));
                form.putExtra("position", position);
                startActivityForResult(form, TIRADA_UPDATED);
                break;
        }
    }

    private void deleteSelectedItems(int position) {
        switch (mViewPager.getCurrentItem()) {
            case 0:
                if (guias != null && guias.size() > 0) {
                    guias.remove(position);
                    guiaArrayAdapter.notifyDataSetChanged();
                }
                break;
            case 1:
                try {
                    //Actualizar cupo de la guia correspondiente
                    Guia guia = guias.get(compras.get(position).getIdPosGuia());
                    int unidadesComprada = compras.get(position).getUnidades();
                    guia.setGastado(guia.getGastado() - unidadesComprada);

                    //Borrado de la compra
                    compras.remove(position);
                    compraArrayAdapter.notifyDataSetChanged();
                    guiaArrayAdapter.notifyDataSetChanged();
                } catch (IndexOutOfBoundsException ex) {
                    Log.e(getPackageName(), "Fallo con los index al borrar una compra", ex);
                }
                break;
            case 2:
                //Si existe alguna conexion, no se podra eliminar la licencia
                if (Utils.licenseCanBeDeleted(position)) {
                    Toast.makeText(FragmentMainActivity.this, R.string.delete_license_fail, Toast.LENGTH_LONG).show();
                } else {
                    try {
                        Utils.removeNotificationFromSharedPreference(this, licencias.get(position).getNumLicencia());
                    } catch (Exception ex) {
                        Log.wtf(getPackageName(), "Fallo al listar las notificaciones", ex);
                    }
                    // Eliminacion evento Calendario
                    deleteSameDayEventCalendar(position);
                    deleteMonthBeforeEventCalendar(position);

                    licencias.remove(position);
                    licenciaArrayAdapter.notifyDataSetChanged();
                    break;
                }
            case 3:
                if (tiradas != null && tiradas.size() > 0) {
                    tiradas.remove(position);
                    tiradaArrayAdapter.notifyDataSetChanged();
                    PlaceholderFragment.updateInfoTirada();
                }
                break;
        }

        try {
            // Guardado en la BBDD local de las estructuras de datos
            dbSqlHelper.saveListGuias(null, guias);
            dbSqlHelper.saveListCompras(null, compras);
            dbSqlHelper.saveListLicencias(null, licencias);
            dbSqlHelper.saveListTiradas(null, tiradas);
        } catch (Exception ex) {
            FirebaseCrash.report(ex);
            FirebaseCrash.logcat(Log.ERROR, TAG, "NPE caught");
        }
    }

    /**
     * Metodo para eliminar un evento del calendario del sistema despues de que el usuario elimine una licencia
     */
    private void deleteSameDayEventCalendar(int position) {
        // Se comprueba el permiso de lectura del calendario porque te obliga la implementacion de ContentResolver Query
        // No tendria que ser necesario hacerlo porque ya se han comprobado los permisos de lectura y escritura en el
        // guardado de las licencias. Si el usuario no los ha aceptado no puede guardar una licencia y por tanto tampoco eliminarla
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            // Inicio preparacion eliminacion evento
            Cursor cursor = null;
            ContentResolver contentResolver = getContentResolver();
            long startDay = 0;
            long endDay = 0;
            try {
                // Fecha inicio evento
                Calendar beginTime = Calendar.getInstance();
                beginTime.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(licencias.get(position).getFechaCaducidad()));
                beginTime.set(Calendar.HOUR_OF_DAY, 0);
                beginTime.set(Calendar.MINUTE, 0);
                beginTime.set(Calendar.SECOND, 0);
                startDay = beginTime.getTimeInMillis();
                // Fecha final evento
                Calendar endTime = Calendar.getInstance();
                endTime.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(licencias.get(position).getFechaCaducidad()));
                endTime.set(Calendar.HOUR_OF_DAY, 23);
                endTime.set(Calendar.MINUTE, 59);
                endTime.set(Calendar.SECOND, 59);
                endDay = endTime.getTimeInMillis();
            } catch (ParseException e) {
                e.printStackTrace();
                Log.e(getPackageName(), "Fallo al eliminar el evento del calendario", e);
            }
            // Preparacion de la query
            String title = "Tu licencia caduca hoy";
            String description = Utils.getStringLicenseFromId(FragmentMainActivity.this, licencias.get(position).getTipo()) + ": " + licencias.get(position).getNumLicencia();
            String[] projection = new String[]{BaseColumns._ID, CalendarContract.Events.TITLE,
                    CalendarContract.Events.DESCRIPTION, CalendarContract.Events.DTSTART};
            String selection = CalendarContract.Events.DTSTART + " >= ? AND " + CalendarContract.Events.DTSTART + " <= ? AND "
                    + CalendarContract.Events.TITLE + " = ? AND " + CalendarContract.Events.DESCRIPTION + " = ? ";
            String[] selectionArgs = new String[]{Long.toString(startDay), Long.toString(endDay), title, description};
            // Primero se recupera el id del evento a eliminar
            cursor = contentResolver.query(CalendarContract.Events.CONTENT_URI, projection, selection, selectionArgs, null);
            while (cursor.moveToNext()) {
                long eventId = cursor.getLong(cursor.getColumnIndex("_id"));
                // Despues se elimina el evento en funcion de su id
                contentResolver.delete(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId), null, null);
            }
            cursor.close();
        }
    }

    // Lo mismo que el anterior metodo pero se elimina el evento con un mes de antelacion
    private void deleteMonthBeforeEventCalendar(int position) {
        // Se comprueba el permiso de lectura del calendario porque te obliga la implementacion de ContentResolver Query
        // No tendria que ser necesario hacerlo porque ya se han comprobado los permisos de lectura y escritura en el
        // guardado de las licencias. Si el usuario no los ha aceptado no puede guardar una licencia y por tanto tampoco eliminarla
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            // Inicio preparacion eliminacion evento
            Cursor cursor = null;
            ContentResolver contentResolver = getContentResolver();
            long startDay = 0;
            long endDay = 0;
            try {
                // Fecha inicio evento
                Calendar beginTime = Calendar.getInstance();
                beginTime.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(licencias.get(position).getFechaCaducidad()));
                // Un mes de antelacion
                beginTime.add(Calendar.MONTH, -1);
                beginTime.set(Calendar.HOUR_OF_DAY, 0);
                beginTime.set(Calendar.MINUTE, 0);
                beginTime.set(Calendar.SECOND, 0);
                startDay = beginTime.getTimeInMillis();
                // Fecha final evento
                Calendar endTime = Calendar.getInstance();
                endTime.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(licencias.get(position).getFechaCaducidad()));
                // Un mes de antelacion
                endTime.add(Calendar.MONTH, -1);
                endTime.set(Calendar.HOUR_OF_DAY, 23);
                endTime.set(Calendar.MINUTE, 59);
                endTime.set(Calendar.SECOND, 59);
                endDay = endTime.getTimeInMillis();
            } catch (ParseException e) {
                e.printStackTrace();
                Log.e(getPackageName(), "Fallo al eliminar el evento del calendario", e);
            }
            // Preparacion de la query
            String title = "Tu licencia caduca dentro de un mes";
            String description = Utils.getStringLicenseFromId(FragmentMainActivity.this, licencias.get(position).getTipo()) + ": " + licencias.get(position).getNumLicencia();
            String[] projection = new String[]{BaseColumns._ID, CalendarContract.Events.TITLE,
                    CalendarContract.Events.DESCRIPTION, CalendarContract.Events.DTSTART};
            String selection = CalendarContract.Events.DTSTART + " >= ? AND " + CalendarContract.Events.DTSTART + " <= ? AND "
                    + CalendarContract.Events.TITLE + " = ? AND " + CalendarContract.Events.DESCRIPTION + " = ? ";
            String[] selectionArgs = new String[]{Long.toString(startDay), Long.toString(endDay), title, description};

            // Primero se recupera el id del evento a eliminar
            cursor = contentResolver.query(CalendarContract.Events.CONTENT_URI, projection, selection, selectionArgs, null);
            while (cursor.moveToNext()) {
                long eventId = cursor.getLong(cursor.getColumnIndex("_id"));
                // Despues se elimina el evento en funcion de su id
                contentResolver.delete(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId), null, null);
            }
            cursor.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fragment_main, menu);

        switch (mViewPager.getCurrentItem()) {
            case 0:
            case 1:
            case 2:
                menu.findItem(R.id.tabla_tiradas).setVisible(false);
                break;
            case 3:
                menu.findItem(R.id.tabla_tiradas).setVisible(true);
                break;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                Intent intent = new Intent(FragmentMainActivity.this, SettingsFragment.class);
                startActivity(intent);
                break;
            case R.id.tabla_tiradas:
                if (Utils.isNetworkAvailable(this)) {
                    StorageReference storageRef = mStorage.getReference();
                    StorageReference islandRef = storageRef.child(getString(R.string.storage_element_tabla_tiradas));

                    final long ONE_MEGABYTE = 1024 * 1024;
                    islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            Utils.showDialogBitmap(FragmentMainActivity.this, bitmap);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                            Log.e(TAG, "Error descargando la imagen de las tables", exception);
                            Toast.makeText(FragmentMainActivity.this, R.string.error_downloading_image, Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Snackbar.make(findViewById(R.id.main_content), R.string.sin_conexion, Snackbar.LENGTH_LONG).show();
                }

                break;

            default:
                return false;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Recepción de los datos del formulario
     *
     * @param requestCode Código de peticion
     * @param resultCode  Código de resultado de la operacion
     * @param data        Intent con los datos de respuesta
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap localImageBitmap = null;
        Bitmap firebaseImageBitmap = null;
        // Check which request we're responding to
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    try {
                        firebaseImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(new File(fileImagePath)));

                        localImageBitmap = ThumbnailUtils.extractThumbnail(
                                BitmapFactory.decodeFile(fileImagePath),
                                (int) (firebaseImageBitmap.getWidth() * 0.2),
                                (int) (firebaseImageBitmap.getHeight() * 0.2)/*,
                                ThumbnailUtils.OPTIONS_RECYCLE_INPUT*/);

                        // No se necesita esta función
//                        imageBitmap = Utils.resizeImage(imageBitmap, null);

                        updateImage(localImageBitmap, firebaseImageBitmap);
                    } catch (Exception ex) {
                        Log.e(TAG, "Error obteniendo la imagen de la camara", ex);
                    }
                    break;

                case GUIA_COMPLETED:
                    guias.add(new Guia(data.getExtras()));
                    guiaArrayAdapter.notifyDataSetChanged();
                    break;

                case COMPRA_COMPLETED:
                    Compra newCompra = new Compra(data.getExtras());
                    compras.add(newCompra);
                    compraArrayAdapter.notifyDataSetChanged();
                    break;

                case LICENCIA_COMPLETED:
                    licencias.add(new Licencia((Licencia) data.getExtras().getParcelable("modify_licencia")));
                    licenciaArrayAdapter.notifyDataSetChanged();
                    break;

                case TIRADA_COMPLETED:
                    tiradas.add(new Tirada((Tirada) data.getExtras().getParcelable("modify_tirada")));
                    PlaceholderFragment.updateInfoTirada();
                    tiradaArrayAdapter.notifyDataSetChanged();
                    break;

                case GUIA_UPDATED:
                    updateGuia(data);
                    break;
                case COMPRA_UPDATED:
                    updateCompra(data);
                    break;
                case LICENCIA_UPDATED:
                    updateLicencia(data);
                    break;
                case TIRADA_UPDATED:
                    PlaceholderFragment.updateInfoTirada(data);
                    break;
            }
        } else if (resultCode == RESULT_CANCELED) {
            Log.e(getPackageName(), "Resultado de la camara cancelada");
        }

        try {
            // Guardado en la BBDD local de las estructuras de datos
            dbSqlHelper.saveListGuias(null, guias);
            dbSqlHelper.saveListCompras(null, compras);
            dbSqlHelper.saveListLicencias(null, licencias);
            dbSqlHelper.saveListTiradas(null, tiradas);
        } catch (Exception ex) {
            FirebaseCrash.report(ex);
            FirebaseCrash.logcat(Log.ERROR, TAG, "NPE caught");
        }

        showTextEmptyList();
    }

    private void saveLists() {
        try {
            //Borrado de la vase de datos actual;
            if (userRef != null) {
                userRef.child("db").removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        userRef.child("db").child("guias").setValue(guias);
                        userRef.child("db").child("compras").setValue(compras);
                        userRef.child("db").child("licencias").setValue(licencias);
                        userRef.child("db").child("tiradas").setValue(tiradas);

                        Log.i(TAG, "Guardado de listas en Firebase");
                    }
                });
            } else {
                FirebaseCrash.logcat(Log.WARN, TAG, "Fallo al  guardar las listas, usuario a null");
            }
        } catch (Exception ex) {
            FirebaseCrash.logcat(Log.ERROR, TAG, "Fallo guardando las listas");
            FirebaseCrash.report(ex);
        }
    }

    /**
     * Recalculamos el gasto de municion de todas las guias, recorriendo las compras
     */
    private void updateGastoMunicion() {
        //Reseteo de los gastos de todas las guias
        for (Guia guia : guias) {
            guia.setGastado(0);
        }

        // Recalculamos todos los gastos
        for (Compra comp : compras) {
            Guia guia = guias.get(comp.getIdPosGuia());
            int currentYear = getSharedPreferences("Preferences", Context.MODE_PRIVATE).getInt("year", 0);

            try {
                if (currentYear != 0 && guia != null) {
                    //Sumaamos solo las compras del año en el que estamos
                    Calendar fechaCompra = Calendar.getInstance();
                    fechaCompra.setTime(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(comp.getFecha()));

                    //noinspection WrongConstant
                    if (currentYear == fechaCompra.get(Calendar.YEAR)) {
                        guia.setGastado(guia.getGastado() + comp.getUnidades());
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (guiaArrayAdapter == null)
            guiaArrayAdapter = new GuiaArrayAdapter(FragmentMainActivity.this, R.layout.guia_item, guias);

        guiaArrayAdapter.notifyDataSetChanged();
    }

    private void updateImage(Bitmap LocalImageBitmap, Bitmap FirebaseImageBitmap) {
        if (LocalImageBitmap != null && FirebaseImageBitmap != null) {
            try {
                //Guardado en disco de la imagen tomada con la foto
                Utils.saveBitmapToFile(LocalImageBitmap);
                //Guardado de la imagen en Firebase
                Utils.saveBitmapToFirebase(mStorage, FirebaseImageBitmap, fileImagePath, mAuth.getCurrentUser().getUid());
            } catch (Exception ex) {
                Log.e(TAG, "Error guarando la imagen en Firebase", ex);
            }

            //Actualizacion de las listas para mostrar las nuevas imagenes
            switch (mViewPager.getCurrentItem()) {
                case 0:
                    guias.get(imagePosition).setImagePath(fileImagePath);
                    guiaArrayAdapter.notifyDataSetChanged();
                    break;
                case 1:
                    compras.get(imagePosition).setImagePath(fileImagePath);
                    compraArrayAdapter.notifyDataSetChanged();
                    break;
            }
        } else
            Log.e(TAG, "Imagen Null. No se han guardado las imagenes");
    }


    private void updateGuia(Intent data) {
        if (data.getExtras() != null) {
            int position = data.getExtras().getInt("position", -1);
            Guia guia = guias.get(position);

            //TODO: Refactorizar y cambiar esto como en licencias. Hacer que el intent devuelva un objeto Guia y no los campos individualizados.
//            guia.setIdCompra(data.getExtras().getInt(""));
            guia.setTipoLicencia(data.getExtras().getInt("tipoLicencia"));
            guia.setMarca(data.getExtras().getString("marca"));
            guia.setModelo(data.getExtras().getString("modelo"));
            guia.setApodo(data.getExtras().getString("apodo"));
            guia.setTipoArma(data.getExtras().getInt("tipoArma"));
            guia.setCalibre1(data.getExtras().getString("calibre1"));
            guia.setCalibre2(data.getExtras().getString("calibre2"));
            guia.setNumGuia(data.getExtras().getString("numGuia"));
            guia.setNumArma(data.getExtras().getString("numArma"));
            guia.setImagePath(data.getExtras().getString("imagePath"));
            guia.setCupo(data.getExtras().getInt("cupo"));
            guia.setGastado(data.getExtras().getInt("gastado"));

            guiaArrayAdapter.notifyDataSetChanged();
            compraArrayAdapter.notifyDataSetChanged();
        }
    }

    private void updateCompra(Intent data) {
        if (data.getExtras() != null) {
            int position = data.getExtras().getInt("position", -1);
            Compra compra = compras.get(position);

            //TODO: Refactorizar y cambiar esto como en licencias. Hacer que el intent devuelva un objeto Compra y no los campos individualizados.
            compra.setIdPosGuia(data.getExtras().getInt("idPosGuia"));
            compra.setCalibre1(data.getExtras().getString("calibre1"));
            compra.setCalibre2(data.getExtras().getString("calibre2"));
            compra.setUnidades(data.getExtras().getInt("unidades"));
            compra.setPrecio(data.getExtras().getDouble("precio"));
            compra.setFecha(data.getExtras().getString("fecha"));
            compra.setTipo(data.getExtras().getString("tipo"));
            compra.setPeso(data.getExtras().getInt("peso"));
            compra.setMarca(data.getExtras().getString("marca"));
            compra.setTienda(data.getExtras().getString("tienda"));
            compra.setValoracion(data.getExtras().getFloat("valoracion"));
            compra.setImagePath(data.getExtras().getString("imagePath"));

            compraArrayAdapter.notifyDataSetChanged();
        }
    }

    private void updateLicencia(Intent data) {
        if (data.getExtras() != null) {
            int position = data.getExtras().getInt("position", -1);

            Licencia licencia = new Licencia((Licencia) data.getExtras().get("modify_licencia"));
            licencias.set(position, licencia);

            licenciaArrayAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Dialog para la seleccion de la licencia qu
     */
    public void saveUserInFirebase() {
        try {
            //Guardado del usuario en las shared preferences del dispositivo
            String email = Utils.getUserEmail(this);
            String pass = prefs.getString("password", "");

            if (!email.isEmpty()) {
                //Obtención del código de autentificación del usuario
                mAuth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
//                                Toast.makeText(context, R.string.auth_usuario_existente, Toast.LENGTH_SHORT).show();
                                    Log.w(TAG, task.getException().getMessage());
                                }
                            }
                        });

                mAuth.signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Log.w(TAG, "signInWithEmail:failed", task.getException());
//                                Toast.makeText(context, R.string.auth_usuario_logado, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            } else {
                mAuth.signInAnonymously()
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());

                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Log.w(TAG, "signInAnonymously", task.getException());
//                                Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }

        } catch (Exception ex) {
            FirebaseCrash.logcat(Log.ERROR, TAG, "Fallo al iniciar la base de datos de firebase.");
            FirebaseCrash.report(ex);
        }
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        try {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // User is signed in
                Log.w(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                //Cargamos la información del usuario
                userRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
                userRef.child("email").setValue(user.getEmail());
                userRef.child("pass").setValue(prefs.getString("password", ""));
                userRef.child("settings").child("ads").setValue(prefs.getBoolean(PREFS_SHOW_ADS, true));
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
                Log.w(TAG, "onAuthStateChanged:signed_out");
            }
        } catch (Exception ex) {
            FirebaseCrash.logcat(Log.ERROR, TAG, "Fallo al obtener el usuario para la inserccion en la BBDD de Firebase.");
            FirebaseCrash.report(ex);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static Context context;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber, Context mContext) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);

            context = mContext;

            return fragment;
        }

        /**
         * @param data
         */
        public static void updateInfoTirada(Intent data) {
            if (data.getExtras() != null) {
                int position = data.getExtras().getInt("position", -1);

                Tirada tirada = new Tirada((Tirada) data.getExtras().get("modify_tirada"));
                tiradas.set(position, tirada);
            }
            updateInfoTirada();
        }

        /**
         *
         */
        private static void updateInfoTirada() {
            try {
                // Ordenamos el array de tiradas por fecha descendente (la mas actual arriba)
                Collections.sort(tiradas, new Comparator<Tirada>() {
                    @Override
                    public int compare(Tirada date1, Tirada date2) {
                        return Utils.getDateFromString(date2.getFecha()).compareTo(Utils.getDateFromString(date1.getFecha()));
                    }
                });

                if (tiradas.size() > 0 && tiradaCountDown != null) {
                    tiradaCountDown.setVisibility(View.VISIBLE);

                } else {
                    if (tiradaCountDown != null)
                        tiradaCountDown.setVisibility(View.GONE);
                }

                if (tiradas.size() > 0)
                    updateCaducidadLicenciaTirada();

            } catch (IndexOutOfBoundsException ex) {
                Log.e(TAG, "Error calculando la caducidad de la tirada", ex);
//                FirebaseCrash.logcat(Log.ERROR, TAG, "Error calculando la caducidad de la tirada");
//                FirebaseCrash.report(ex);
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage(), ex);
//                FirebaseCrash.logcat(Log.ERROR, TAG, ex.getMessage());
//                FirebaseCrash.report(ex);
            }
        }

        /**
         *
         */
        private static void updateCaducidadLicenciaTirada() throws IndexOutOfBoundsException {
            int daysRemain = Math.round((float) Tirada.millisUntilExpiracy(tiradas.get(0)) / (1000 * 60 * 60 * 24));
//                    int horasRemain = Math.round(millisUntilFinished / (1000 * 60 * 60 * 24));
//                    int minutosRemain = Math.round(millisUntilFinished / (1000 * 60 * 60));
//                    int segundosRemain = Math.round(millisUntilFinished / (1000 * 60));
            StringBuilder sb = new StringBuilder();
            Formatter formatter = new Formatter(sb);

            String text = formatter.format("%s\n%s %s",
                    context.getString(R.string.lbl_caducidad_tirada),
                    daysRemain,
                    context.getString(R.string.days)
            ).toString();

            if (tiradaCountDown != null) {
                tiradaCountDown.setText(text);
            }

            if (daysRemain <= 10) {
                tiradaCountDown.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
                tiradaCountDown.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            } else {
                tiradaCountDown.setBackgroundColor(ContextCompat.getColor(context, R.color.light_yellow));
                tiradaCountDown.setTextColor(ContextCompat.getColor(context, android.R.color.black));
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.list_view_pager, container, false);
            listView = (ListView) rootView.findViewById(R.id.ListView);
            tiradaCountDown = (TextView) rootView.findViewById(R.id.pager_tirada_countdown);

            try {
                switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                    case 0: // Lista de las guias
                        tiradaCountDown.setVisibility(View.GONE);
                        guiaArrayAdapter = new GuiaArrayAdapter(getActivity(), R.layout.guia_item, guias);
                        listView.setAdapter(guiaArrayAdapter);
                        break;

                    case 1: // Lista de las compras
                        tiradaCountDown.setVisibility(View.GONE);
                        compraArrayAdapter = new CompraArrayAdapter(getActivity(), R.layout.compra_item, compras);
                        listView.setAdapter(compraArrayAdapter);
                        break;

                    case 2: // Lista de las licencias
                        try {
                            tiradaCountDown.setVisibility(View.GONE);
                            licenciaArrayAdapter = new LicenciaArrayAdapter(getActivity(), R.layout.licencia_item, licencias);
                            listView.setAdapter(licenciaArrayAdapter);
                        } catch (Exception ex) {
                            FirebaseCrash.logcat(Log.ERROR, TAG, ex.getMessage());
                            FirebaseCrash.report(ex);
                        }
                        break;
                    case 3: // Lista de Tiradas
                        try {
                            if (tiradas.size() > 0) {
                                tiradaCountDown.setVisibility(View.VISIBLE);
                            } else {
                                tiradaCountDown.setVisibility(View.GONE);
                            }
                            tiradaArrayAdapter = new TiradaArrayAdapter(getActivity(), R.layout.tirada_item, tiradas);
                            listView.setAdapter(tiradaArrayAdapter);
                            updateInfoTirada();
                        } catch (Exception ex) {
                            FirebaseCrash.logcat(Log.ERROR, TAG, "Fallo al actualizar la lista de tiradas");
                            FirebaseCrash.report(ex);
                        }
                        break;
                }

                listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
                listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        if (mActionMode != null) {
                            return false;
                        }

                        view.setSelected(true);
                        auxView = view;

                        // Start the CAB using the ActionMode.Callback defined above
                        mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
                        assert mActionMode != null;
                        mActionMode.setTitle(R.string.menu_cab_title);
                        mActionMode.setTag(position);
                        return true;
                    }
                });

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (mActionMode != null)
                            mActionMode.finish();
                        mActionMode = null;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

            return rootView;
        }
    }

    /**
     * Dialog para la seleccion de la licencia
     */
    public static class GuiaDialogFragment extends DialogFragment {

        //https://developer.android.com/guide/topics/ui/dialogs.html

        private int selectedLicense;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            // Set title
            builder.setTitle(R.string.dialog_licencia_title)
                    // Set items
                    .setSingleChoiceItems(Utils.getLicenseName(getActivity()), 0, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
//                            Toast.makeText(getActivity(), "Seleccionado: " + (String) getGuiaName()[i], Toast.LENGTH_SHORT).show();
                            selectedLicense = i;
                        }
                    })
                    // Add action buttons
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int pos) {
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
                            Intent form = new Intent(getActivity(), GuiaFormActivity.class);
                            form.putExtra("tipo_licencia", (String) Utils.getLicenseName(getActivity())[selectedLicense]);
                            getActivity().startActivityForResult(form, FragmentMainActivity.GUIA_COMPLETED);
//                            }
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            GuiaDialogFragment.this.getDialog().cancel();
                        }
                    });
            return builder.create();
        }
    }

    /**
     * Dialog para la seleccion de la licencia qu
     */
    public static class CompraDialogFragment extends DialogFragment {

        //https://developer.android.com/guide/topics/ui/dialogs.html

        private int selectedGuia;


        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            // Set title
            builder.setTitle(R.string.dialog_guia_title)
                    // Set items
                    .setSingleChoiceItems(getGuiaName(), 0, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int pos) {
                            selectedGuia = pos;
                        }
                    })
                    // Add action buttons
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int pos) {
//                            if (pos >= 0)
//                                Toast.makeText(getActivity(), "Seleccionado: " + getGuiaName()[pos].toString(), Toast.LENGTH_SHORT).show();

                            Intent form = new Intent(getActivity(), CompraFormActivity.class);
                            form.putExtra("position_guia", selectedGuia);
                            form.putExtra("guia", guias.get(selectedGuia));
                            getActivity().startActivityForResult(form, FragmentMainActivity.COMPRA_COMPLETED);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            CompraDialogFragment.this.getDialog().cancel();
                        }
                    });
            return builder.create();
        }

        private CharSequence[] getGuiaName() {
            ArrayList<String> list = new ArrayList<>();
            for (Guia guia : guias) {
                list.add(guia.getApodo());
            }

            return list.toArray(new CharSequence[list.size()]);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position, getApplicationContext());
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.section_guias_title);
                case 1:
                    return getString(R.string.section_compras_title);
                case 2:
                    return getString(R.string.section_licencias_title);
                case 3:
                    return getString(R.string.section_competiciones_title);
            }
            return null;
        }
    }
}

//http://stackoverflow.com/questions/17207366/creating-a-menu-after-a-long-click-event-on-a-list-view
//http://stackoverflow.com/questions/18204386/contextual-action-mode-in-fragment-close-if-not-focused