package al.ahgitdevelopment.municion.forms;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.CalendarContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.ads.AdView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crash.FirebaseCrash;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import al.ahgitdevelopment.municion.FragmentMainContent;
import al.ahgitdevelopment.municion.NotificationPublisher;
import al.ahgitdevelopment.municion.R;
import al.ahgitdevelopment.municion.Utils;
import al.ahgitdevelopment.municion.datamodel.Licencia;

import static al.ahgitdevelopment.municion.di.SharedPrefsModule.PREFS_SHOW_ADS;

/**
 * Created by Alberto on 24/05/2016.
 */
public class LicenciaFormActivity extends AppCompatActivity {

    private static TextInputLayout layoutFechaExpedicion;
    private LinearLayout layoutPermisoConducir;
    private TextInputLayout textInputLayoutLicencia;
    private TextInputLayout layoutFechaCaducidad;
    private TextInputLayout layoutNumAbonado;
    private TextInputLayout layoutNumPoliza;
    private TextInputLayout layoutEdad;
    private AppCompatSpinner tipoLicencia;
    private AppCompatSpinner tipoPermisoConducir;
    private LinearLayout layoutCCAA;
    private AppCompatSpinner autonomia;
    private LinearLayout layoutEscala;
    private AppCompatSpinner tipoEscala;
    private LinearLayout layoutCategoria;
    private AppCompatSpinner categoria;
    private FloatingActionButton fab;
    // Creo este flag para comprabar en el Calendario si es un guardado o modificacion de licencia
    private boolean isModify;
    private Toolbar toolbar;
    private SharedPreferences prefs;
    private AdView mAdView;

    /**
     * Inicializa la actividad
     *
     * @param savedInstanceState Instancia del estado de la activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_licencia);

        // Toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setSubtitle(R.string.title_nueva_licencia);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_bullseye);

        tipoLicencia = findViewById(R.id.form_tipo_licencia);
        layoutPermisoConducir = findViewById(R.id.layout_permiso_conducir);
        textInputLayoutLicencia = findViewById(R.id.text_input_layout_licencia);
        layoutFechaExpedicion = findViewById(R.id.layout_form_fecha_expedicion);
        layoutFechaCaducidad = findViewById(R.id.layout_form_fecha_caducidad);
        layoutNumAbonado = findViewById(R.id.layout_form_num_abonado);
        layoutNumPoliza = findViewById(R.id.layout_form_num_poliza);
        layoutCCAA = findViewById(R.id.layout_ccaa);
        autonomia = findViewById(R.id.form_ccaa);
        tipoPermisoConducir = findViewById(R.id.form_tipo_permiso_conducir);
        layoutEdad = findViewById(R.id.text_input_layout_edad);
        layoutEscala = findViewById(R.id.layout_escala);
        tipoEscala = findViewById(R.id.form_tipo_escala);
        layoutCategoria = findViewById(R.id.layout_categoria);
        categoria = findViewById(R.id.form_categoria);
        fab = findViewById(R.id.fab_form_save);
        mAdView = findViewById(R.id.login_adView);
        isModify = false;

        //Gestion del boton de guardado en funcion de si se abre tras pulsar la notificacion
        fab.setVisibility(View.GONE);
        if (getIntent().hasExtra("notification_call")) {
        } else {
            fab.setVisibility(View.VISIBLE);
        }

        //Carga de datos (en caso de modificacion)
        if (getIntent().getExtras() != null) {
            try {
                Licencia licencia = new Licencia(getIntent().getExtras().getParcelable("modify_licencia"));
                tipoLicencia.setSelection(licencia.getTipo());
                textInputLayoutLicencia.getEditText().setText(String.valueOf(licencia.getNumLicencia()));
                layoutFechaExpedicion.getEditText().setText(licencia.getFechaExpedicion());
                layoutNumAbonado.getEditText().setText(String.valueOf(licencia.getNumAbonado()));
                layoutNumPoliza.getEditText().setText(String.valueOf(licencia.getNumSeguro()));
                autonomia.setSelection(licencia.getAutonomia());
                tipoPermisoConducir.setSelection(licencia.getTipoPermisoConduccion());
                layoutEdad.getEditText().setText(String.valueOf(licencia.getEdad()));
                tipoEscala.setSelection(licencia.getEscala());
                categoria.setSelection(licencia.getCategoria());
                isModify = true;
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        // Eventos que sacan el calendario al recibir el foco en el campo fecha
        layoutFechaExpedicion.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callDatePickerFragment();
            }
        });
        layoutFechaExpedicion.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    callDatePickerFragment();
                }
            }
        });
        layoutFechaExpedicion.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    callDatePickerFragment();
                }
            }
        });
        layoutFechaExpedicion.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callDatePickerFragment();
            }
        });

        setVisibilityFields(tipoLicencia.getSelectedItemPosition());
        fieldsUpdateFechaCaducidad();

        // Gestion de anuncios
        prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        if (prefs.getBoolean(PREFS_SHOW_ADS, true)) {
            mAdView.setVisibility(View.VISIBLE);
            mAdView.setEnabled(true);
            mAdView.loadAd(Utils.getAdRequest(mAdView));
        } else {
            mAdView.setVisibility(View.GONE);
            mAdView.setEnabled(false);
        }
    }

    private void callDatePickerFragment() {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void fabSaveOnClick(View view) {
        if (controlCampos()) { // Agregar fecha al Calendar Provider

            // Create intent to deliver some kind of result data
            Intent result = new Intent(this, FragmentMainContent.class);

            Bundle bundle = new Bundle();
            bundle.putParcelable("modify_licencia", getCurrenteLicense());

            //Paso de vuelta de la posicion del item en el array
            if (getIntent().getExtras() != null)
                bundle.putInt("position", getIntent().getExtras().getInt("position", -1));

            result.putExtras(bundle);

            //Agregar notificacion
            publishNotification();
            //Añadir fecha de caducidad al calendario
            checkCalendarPermission();

            setResult(Activity.RESULT_OK, result);
            finish();
        }
    }

    /**
     * Metodo para agregar la fecha de caducidad de las licencias al calendario de google
     * comprobando los permisos de forma dinámica para las versiones de android superiores
     * a la M
     */
    private void checkCalendarPermission() {
        int readPermission = PackageManager.PERMISSION_GRANTED;
        int writePermission = PackageManager.PERMISSION_GRANTED;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            readPermission = checkSelfPermission(Manifest.permission.READ_CALENDAR);
            writePermission = checkSelfPermission(Manifest.permission.WRITE_CALENDAR);
            if (readPermission != PackageManager.PERMISSION_GRANTED || writePermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{
                                Manifest.permission.READ_CALENDAR,
                                Manifest.permission.WRITE_CALENDAR
                        },
                        100 //Codigo de respuesta
                );
            }else {
                addSameDayEventToCalendar(isModify);
                addMonthBeforeEventToCalendar(isModify);
            }
        } else {
            addSameDayEventToCalendar(isModify);
            addMonthBeforeEventToCalendar(isModify);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                addSameDayEventToCalendar(isModify);
                addMonthBeforeEventToCalendar(isModify);
                finish();
            } else {
                Snackbar.make(textInputLayoutLicencia.getEditText(), R.string.dialog_licencia_no_permiso, Snackbar.LENGTH_LONG)
                        .setAction(android.R.string.ok, null)
                        .show();
            }
        }
    }

    /**
     * Proceso de agregar la fecha de caducidad como evento al calendario. Si es update se elimina el anterior y se guarda.
     * En caso contrario solo se guarda.
     */
    private void addSameDayEventToCalendar(boolean isModify) {
        Cursor cursor = null;
        ContentResolver contentResolver = getContentResolver();
        long calID = 3;
        long startMillis = 0;
        long endMillis = 0;
        // Primero se comprueba si es una modificacion. Si ya hay un evento previo se elimina.
        if (isModify) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
                Licencia licencia = new Licencia(getIntent().getExtras().getParcelable("modify_licencia"));
                try {
                    // Fecha inicio evento
                    Calendar beginTime = Calendar.getInstance();
                    beginTime.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(licencia.getFechaCaducidad()));
                    beginTime.set(Calendar.HOUR_OF_DAY, 0);
                    beginTime.set(Calendar.MINUTE, 0);
                    beginTime.set(Calendar.SECOND, 0);
                    startMillis = beginTime.getTimeInMillis();
                    // Fecha final evento
                    Calendar endTime = Calendar.getInstance();
                    endTime.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(licencia.getFechaCaducidad()));
                    endTime.set(Calendar.HOUR_OF_DAY, 23);
                    endTime.set(Calendar.MINUTE, 59);
                    endTime.set(Calendar.SECOND, 59);
                    endMillis = endTime.getTimeInMillis();
                } catch (ParseException e) {
                    FirebaseCrash.report(e);
                    FirebaseCrash.logcat(Log.ERROR, getLocalClassName(), "Fallo al modificar el evento del calendario");
                }
                // Preparacion de la query
                String title = "Tu licencia caduca hoy";
                String description = Utils.getStringLicenseFromId(LicenciaFormActivity.this, licencia.getTipo()) + ": " + licencia.getNumLicencia();
                String[] projection = new String[]{BaseColumns._ID, CalendarContract.Events.TITLE,
                        CalendarContract.Events.DESCRIPTION, CalendarContract.Events.DTSTART};
                String selection = CalendarContract.Events.DTSTART + " >= ? AND " + CalendarContract.Events.DTSTART + " <= ? AND "
                        + CalendarContract.Events.TITLE + " = ? AND " + CalendarContract.Events.DESCRIPTION + " = ? ";
                String[] selectionArgs = new String[]{Long.toString(startMillis), Long.toString(endMillis), title, description};
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
        try {
            //Fecha inicial
            Calendar beginTime = Calendar.getInstance();
            beginTime.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(layoutFechaCaducidad.getEditText().getText().toString()));
            beginTime.set(Calendar.HOUR_OF_DAY, Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
            beginTime.set(Calendar.MINUTE, Calendar.getInstance().get(Calendar.MINUTE));
            beginTime.set(Calendar.SECOND, Calendar.getInstance().get(Calendar.SECOND));
            startMillis = beginTime.getTimeInMillis();
            //Fecha de caducidad
            Calendar endTime = Calendar.getInstance();
            endTime.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(layoutFechaCaducidad.getEditText().getText().toString()));
            endTime.set(Calendar.HOUR_OF_DAY, Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + 1);
            endTime.set(Calendar.MINUTE, Calendar.getInstance().get(Calendar.MINUTE));
            endTime.set(Calendar.SECOND, Calendar.getInstance().get(Calendar.SECOND));
            endMillis = endTime.getTimeInMillis();

            ContentValues values = new ContentValues();
            values.put(CalendarContract.Events.DTSTART, startMillis);
            values.put(CalendarContract.Events.DTEND, endMillis);
            values.put(CalendarContract.Events.TITLE, "Tu licencia caduca hoy");
            values.put(CalendarContract.Events.DESCRIPTION, Utils.getStringLicenseFromId(
                    LicenciaFormActivity.this,
                    tipoLicencia.getSelectedItemPosition()) + ": " + textInputLayoutLicencia.getEditText().getText().toString());
            values.put(CalendarContract.Events.CALENDAR_ID, calID);
            values.put(CalendarContract.Events.EVENT_TIMEZONE, "Europe/Madrid");

            Uri uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values);

        } catch (SecurityException ex) {
            FirebaseCrash.report(ex);
            FirebaseCrash.logcat(Log.ERROR, getLocalClassName(), "Fallo en los permisos del calendario");
        } catch (ParseException ex) {
            FirebaseCrash.report(ex);
            FirebaseCrash.logcat(Log.ERROR, getLocalClassName(), "Fallo al crear el evento del calendario");
        } catch (Exception ex) {
            FirebaseCrash.report(ex);
            FirebaseCrash.logcat(Log.ERROR, getLocalClassName(), "Excepcion generica");
        }

    }

    // Lo mismo que el anterior metodo pero se crea el evento con un mes de antelacion
    private void addMonthBeforeEventToCalendar(boolean isModify) {
        Cursor cursor = null;
        ContentResolver contentResolver = getContentResolver();
        long calID = 3;
        long startMillis = 0;
        long endMillis = 0;
        // Primero se comprueba si es una modificacion. Si ya hay un evento previo se elimina.
        if (isModify) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
                Licencia licencia = new Licencia(getIntent().getExtras().getParcelable("modify_licencia"));
                try {
                    // Fecha inicio evento
                    Calendar beginTime = Calendar.getInstance();
                    beginTime.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(licencia.getFechaCaducidad()));
                    // Un mes de antelacion
                    beginTime.add(Calendar.MONTH, -1);
                    beginTime.set(Calendar.HOUR_OF_DAY, 0);
                    beginTime.set(Calendar.MINUTE, 0);
                    beginTime.set(Calendar.SECOND, 0);
                    startMillis = beginTime.getTimeInMillis();
                    // Fecha final evento
                    Calendar endTime = Calendar.getInstance();
                    endTime.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(licencia.getFechaCaducidad()));
                    // Un mes de antelacion
                    endTime.add(Calendar.MONTH, -1);
                    endTime.set(Calendar.HOUR_OF_DAY, 23);
                    endTime.set(Calendar.MINUTE, 59);
                    endTime.set(Calendar.SECOND, 59);
                    endMillis = endTime.getTimeInMillis();
                } catch (ParseException e) {
                    FirebaseCrash.report(e);
                    FirebaseCrash.logcat(Log.ERROR, getLocalClassName(), "Fallo al modificar el evento del calendario");
                }
                // Preparacion de la query
                String title = "Tu licencia caduca dentro de un mes";
                String description = Utils.getStringLicenseFromId(LicenciaFormActivity.this, licencia.getTipo()) + ": " + licencia.getNumLicencia();
                String[] projection = new String[]{BaseColumns._ID, CalendarContract.Events.TITLE,
                        CalendarContract.Events.DESCRIPTION, CalendarContract.Events.DTSTART};
                String selection = CalendarContract.Events.DTSTART + " >= ? AND " + CalendarContract.Events.DTSTART + " <= ? AND "
                        + CalendarContract.Events.TITLE + " = ? AND " + CalendarContract.Events.DESCRIPTION + " = ? ";
                String[] selectionArgs = new String[]{Long.toString(startMillis), Long.toString(endMillis), title, description};
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
        try {
            //Fecha inicial
            Calendar beginTime = Calendar.getInstance();
            beginTime.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(layoutFechaCaducidad.getEditText().getText().toString()));
            // Un mes de antelacion
            beginTime.add(Calendar.MONTH, -1);
            beginTime.set(Calendar.HOUR_OF_DAY, Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
            beginTime.set(Calendar.MINUTE, Calendar.getInstance().get(Calendar.MINUTE));
            beginTime.set(Calendar.SECOND, Calendar.getInstance().get(Calendar.SECOND));
            startMillis = beginTime.getTimeInMillis();
            //Fecha de caducidad
            Calendar endTime = Calendar.getInstance();
            endTime.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(layoutFechaCaducidad.getEditText().getText().toString()));
            // Un mes de antelacion
            endTime.add(Calendar.MONTH, -1);
            endTime.set(Calendar.HOUR_OF_DAY, Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + 1);
            endTime.set(Calendar.MINUTE, Calendar.getInstance().get(Calendar.MINUTE));
            endTime.set(Calendar.SECOND, Calendar.getInstance().get(Calendar.SECOND));
            endMillis = endTime.getTimeInMillis();

            ContentValues values = new ContentValues();
            values.put(CalendarContract.Events.DTSTART, startMillis);
            values.put(CalendarContract.Events.DTEND, endMillis);
            values.put(CalendarContract.Events.TITLE, "Tu licencia caduca dentro de un mes");
            values.put(CalendarContract.Events.DESCRIPTION, Utils.getStringLicenseFromId(
                    LicenciaFormActivity.this,
                    tipoLicencia.getSelectedItemPosition()) + ": " + textInputLayoutLicencia.getEditText().getText().toString());
            values.put(CalendarContract.Events.CALENDAR_ID, calID);
            values.put(CalendarContract.Events.EVENT_TIMEZONE, "Europe/Madrid");

            Uri uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values);

        } catch (SecurityException ex) {
            ex.printStackTrace();
            Log.e(getPackageName(), "Fallo en los permisos del calendario", ex);
        } catch (ParseException ex) {
            ex.printStackTrace();
            Log.e(getPackageName(), "Fallo al crear el evento del calendario", ex);
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(getPackageName(), "Excepcion generica", ex);
        }

    }

    /**
     * Campos y eventos que actualizan la fecha de caducidad
     */
    private void fieldsUpdateFechaCaducidad() {

        //Fecha de expedicion:
        // - Cambio de texto
        layoutFechaExpedicion.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateFechaCaducidad();
            }
        });

        //Tipo de licencia: cambio del tipo de licencia
        tipoLicencia.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateFechaCaducidad();
                setVisibilityFields(tipoLicencia.getSelectedItemPosition());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //Tipo de permiso de conducir: cambio del tipo de permiso
        tipoPermisoConducir.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateFechaCaducidad();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //Edad:
        // - Cambio de foco en el layout de la edad
        // - Modificacion de la edad
        layoutEdad.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                updateFechaCaducidad();
            }
        });
        layoutEdad.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateFechaCaducidad();
            }
        });
    }

    /**
     * Modificacion de la fecha de caducidad
     */
    private void updateFechaCaducidad() {

        if (!layoutFechaExpedicion.getEditText().getText().toString().equals("")) {

            //Calculamos al fecha de caducidad en función de fecha de expedición introducida
            SimpleDateFormat simpleDate = new SimpleDateFormat("dd/MM/yyyy");
            Calendar calendar = Calendar.getInstance();
            try {
                calendar.setTime(simpleDate.parse(layoutFechaExpedicion.getEditText().getText().toString()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            switch (tipoLicencia.getSelectedItemPosition()) {
                // Esta licencia no caduca por lo que ponemos una fecha muy lejaana
                case 0: // Licencia A
                    calendar.set(3000, Calendar.DECEMBER, 31);
                    layoutFechaCaducidad.getEditText().setText(simpleDate.format(calendar.getTime()));
                    break;
                // Sumamos 3 año
                case 1: // Licencia B
                case 5: // Licencia F
                    calendar.add(Calendar.YEAR, 3);
                    layoutFechaCaducidad.getEditText().setText(simpleDate.format(calendar.getTime()));
                    break;
                // Sumamos 5 años
                case 2: // Licencia C
                case 3: // Licencia D
                case 4: // Licencia E
                case 6: // Licencia AE
                case 7: // Licencia AER
                case 8: // Licencia Libero Coleccionesta
                    calendar.add(Calendar.YEAR, 5);
                    layoutFechaCaducidad.getEditText().setText(simpleDate.format(calendar.getTime()));
                    break;
                // Ajustamos al final de año
                case 9: // Autonomica Caza
                case 10: // Autonomica Pesca
                    calendar.set(calendar.get(Calendar.YEAR), Calendar.DECEMBER, 31);
                    layoutFechaCaducidad.getEditText().setText(simpleDate.format(calendar.getTime()));
                case 11: // Licencia Federativa de tiro
//                    calendar.add(Calendar.YEAR, 1);
                    calendar.set(calendar.get(Calendar.YEAR), Calendar.DECEMBER, 31);
                    layoutFechaCaducidad.getEditText().setText(simpleDate.format(calendar.getTime()));
                    break;
                // Sumamos 10 años
                case 12: // Persmiso de Conducir
                    if (!layoutEdad.getEditText().getText().toString().equals("") && Integer.parseInt(layoutEdad.getEditText().getText().toString()) < 65) {
                        switch (tipoPermisoConducir.getSelectedItemPosition()) {
                            case 0: // AM
                            case 1: // A1
                            case 2: // A2
                            case 3: // A
                            case 4: // B
                                calendar.add(Calendar.YEAR, 10);
                                layoutFechaCaducidad.getEditText().setText(simpleDate.format(calendar.getTime()));
                                break;
                            case 5: // C1
                            case 6: // C
                            case 7: // D1
                            case 8: // D
                            case 9: // BE
                            case 10: // C1E
                            case 11: // CE
                            case 12: // D1E
                            case 13: // DE
                            case 14: // BTP
                                calendar.add(Calendar.YEAR, 5);
                                layoutFechaCaducidad.getEditText().setText(simpleDate.format(calendar.getTime()));
                                break;
                        }
                    } else {
                        switch (tipoPermisoConducir.getSelectedItemPosition()) {
                            case 0: // AM
                            case 1: // A1
                            case 2: // A2
                            case 3: // A
                            case 4: // B
                                calendar.add(Calendar.YEAR, 5);
                                layoutFechaCaducidad.getEditText().setText(simpleDate.format(calendar.getTime()));
                                break;
                            case 5: // C1
                            case 6: // C
                            case 7: // D1
                            case 8: // D
                            case 9: // BE
                            case 10: // C1E
                            case 11: // CE
                            case 12: // D1E
                            case 13: // DE
                            case 14: // BTP
                                calendar.add(Calendar.YEAR, 3);
                                layoutFechaCaducidad.getEditText().setText(simpleDate.format(calendar.getTime()));
                                break;
                        }
                    }
                    break;
            }
        } else {
            layoutFechaCaducidad.getEditText().setText("");
        }
    }

    /**
     * Metodo para crear o modificar la notificación
     */
    private void publishNotification() {
        //  Se envía la notificación cuando el sistema llegue a la fecha indicada
        try {
            Calendar caducidad = Calendar.getInstance();
            caducidad.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(layoutFechaCaducidad.getEditText().getText().toString()));
            caducidad.set(Calendar.HOUR_OF_DAY, Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
            caducidad.set(Calendar.MINUTE, Calendar.getInstance().get(Calendar.MINUTE));
            caducidad.set(Calendar.SECOND, Calendar.getInstance().get(Calendar.SECOND) + 30);

            // Creates an explicit intent for an Activity in your app
            Intent notificationIntent = new Intent(this, NotificationPublisher.class);
            notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, textInputLayoutLicencia.getEditText().getText().toString().trim());
            notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, getNotification());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

//            long futureInMillis = caducidad.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, caducidad.getTimeInMillis(), pendingIntent);

            //Gestion de las notificaciones
            int pos;
            if (getIntent() != null && getIntent().hasExtra("position"))
                pos = getIntent().getExtras().getInt("position", -1);
            else
                pos = -1;

            Utils.addNotificationToSharedPreferences(this, pos);

        } catch (ParseException ex) {
            Log.e(getPackageName(), "Fallo al crear la notificacion", ex);
        }
    }
//    https://developer.android.com/training/scheduling/alarms.html

    /**
     * Creación de la notificación.
     *
     * @return Retorna la notificacion creada.
     */
    private Notification getNotification() {

        Intent resultIntent = new Intent(this, LicenciaFormActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("modify_licencia", getCurrenteLicense());
        resultIntent.putExtras(bundle);

        resultIntent.putExtra("notification_call", true);

/* Eliminamos el backstack
        // The stack builder object will contain an artificial back stack for the started Activity.
        // This ensures that navigating backward from the Activity leads out of your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(LicenciaFormActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);


        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
*/
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder mBuilder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher_4_transparent)
                    .setContentTitle("Caducidad de Licencia")
                    .setContentText("Tu licencia caduca hoy")
                    .setSubText(Utils.getStringLicenseFromId(
                            LicenciaFormActivity.this,
                            tipoLicencia.getSelectedItemPosition()) + ": " + textInputLayoutLicencia.getEditText().getText().toString())
                    .setContentIntent(resultPendingIntent)
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_LOW)
                    .setLights(Color.GREEN, 500, 500)
                    .setVibrate(new long[]{150, 300, 150, 400})
                    .setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.glitchy_language));
        } else {
            mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher_4_transparent)
                    .setContentTitle("Caducidad de Licencia")
                    .setContentText("Tu licencia caduca hoy")
                    .setSubText(Utils.getStringLicenseFromId(
                            LicenciaFormActivity.this,
                            tipoLicencia.getSelectedItemPosition()) + ": " + textInputLayoutLicencia.getEditText().getText().toString())
                    .setContentIntent(resultPendingIntent)
                    .setAutoCancel(true)
                    .setLights(Color.GREEN, 500, 500)
                    .setVibrate(new long[]{150, 300, 150, 400})
                    .setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.glitchy_language));
        }

        return mBuilder.build();
    }

    /**
     * Devuelve los datos de la interfaz en un objeto licencia
     *
     * @return Objeto licencia con todos los datos del formulario
     */
    private Licencia getCurrenteLicense() {
        Licencia licencia = new Licencia();
        try {
            licencia.setTipo(tipoLicencia.getSelectedItemPosition());
            if (licencia.getNombre() == null || licencia.getNombre().equals(""))
                licencia.setNombre(Utils.getStringLicenseFromId(this, licencia.getTipo()));
            if (layoutPermisoConducir.getVisibility() == View.VISIBLE) {
                licencia.setTipoPermisoConduccion(tipoPermisoConducir.getSelectedItemPosition());
            } else
                licencia.setTipoPermisoConduccion(-1);
            licencia.setNumLicencia(textInputLayoutLicencia.getEditText().getText().toString().trim());
            licencia.setFechaExpedicion(layoutFechaExpedicion.getEditText().getText().toString().trim());
            licencia.setFechaCaducidad(layoutFechaCaducidad.getEditText().getText().toString().trim());
            if (layoutNumAbonado.getVisibility() == View.VISIBLE) {
                if (layoutNumAbonado.getEditText().getText().toString().trim().equals(""))
                    licencia.setNumAbonado(0);
                else
                    licencia.setNumAbonado(Integer.parseInt(layoutNumAbonado.getEditText().getText().toString().trim()));
            }
            if (layoutNumPoliza.getVisibility() == View.VISIBLE) {
                licencia.setNumSeguro(layoutNumPoliza.getEditText().getText().toString().trim());
            }
            if (layoutCCAA.getVisibility() == View.VISIBLE) {
                licencia.setAutonomia(autonomia.getSelectedItemPosition());
            } else
                licencia.setAutonomia(-1);
            if (layoutEdad.getVisibility() == View.VISIBLE)
                licencia.setEdad(Integer.parseInt(layoutEdad.getEditText().getText().toString().trim()));
            if (layoutEscala.getVisibility() == View.VISIBLE) {
                licencia.setEscala(tipoEscala.getSelectedItemPosition());
            } else
                licencia.setEscala(-1);
            if (layoutCategoria.getVisibility() == View.VISIBLE) {
                licencia.setCategoria(categoria.getSelectedItemPosition());
            } else
                licencia.setCategoria(-1);

        } catch (Exception ex) {
            Log.e(getPackageName(), "Fallo en el empaquetado de la licencia para la notificación", ex);
        }

        //Guardado de la información de la notificacion
        Utils.notificationData.setLicencia(licencia.getNombre());
        Utils.notificationData.setId(String.valueOf(licencia.getNumLicencia()));
        Utils.notificationData.setFecha(licencia.getFechaCaducidad());

        return licencia;
    }

    /**
     * Control de campos obligarios para poder guardar el formulario
     *
     * @return Flag indicando si estan todos los campos obligarios (true), en caso contrario (false)
     */
    private boolean controlCampos() {
        boolean flag = true;

        if (textInputLayoutLicencia.getEditText().getText().toString().equals("")) {
            textInputLayoutLicencia.setError(getString(R.string.msg_err_num_licencia));
            flag = false;
        }
        if (layoutFechaExpedicion.getEditText().getText().toString().equals("")) {
            layoutFechaExpedicion.setError(getString(R.string.msg_err_fecha_exp));
            flag = false;
        }
        if (layoutFechaCaducidad.getEditText().getText().toString().equals("")) {
            layoutFechaCaducidad.setError(getString(R.string.msg_err_fecha_cad));
            flag = false;
        }
        //No obligatorio
//        if (layoutNumAbonado.getVisibility() == View.VISIBLE && layoutNumAbonado.getEditText().getText().toString().equals("")) {
//            layoutNumAbonado.setError("Introduce el número de abonado");
//            flag = false;
//        }
        if (layoutNumPoliza.getVisibility() == View.VISIBLE && layoutNumPoliza.getEditText().getText().toString().equals("")) {
            layoutNumPoliza.setError(getString(R.string.msg_err_poliza_seg));
            flag = false;
        }
        if (layoutEdad.getVisibility() == View.VISIBLE && layoutEdad.getEditText().getText().toString().equals("")) {
            layoutEdad.setError(getString(R.string.msg_err_edad));
            flag = false;
        }

        return flag;
    }

    /**
     * Método para modificar la visibilidad de los campos en función del tipo de licencia seleccionado
     *
     * @param tipoLicencia Licencia seleccionada
     */
    private void setVisibilityFields(int tipoLicencia) {
        //La licencia A no tiene fecha de caducidad
        if (tipoLicencia == 0) {
            layoutFechaCaducidad.setVisibility(View.GONE);
            layoutEscala.setVisibility(View.VISIBLE);
        } else {
            layoutFechaCaducidad.setVisibility(View.VISIBLE);
            layoutEscala.setVisibility(View.GONE);
        }

        switch (tipoLicencia) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
                textInputLayoutLicencia.setHint(getResources().getString(R.string.lbl_num_licencia));
                ((LinearLayout.LayoutParams) textInputLayoutLicencia.getLayoutParams()).setMargins(0, 0, 0, 0);
                layoutNumAbonado.setVisibility(View.GONE);
                layoutNumPoliza.setVisibility(View.GONE);
                layoutCCAA.setVisibility(View.GONE);
                layoutPermisoConducir.setVisibility(View.GONE);
                layoutEdad.setVisibility(View.GONE);
                layoutCategoria.setVisibility(View.GONE);
                break;

            case 9:
            case 10:
                textInputLayoutLicencia.setHint(getResources().getString(R.string.lbl_num_licencia));
                ((LinearLayout.LayoutParams) textInputLayoutLicencia.getLayoutParams()).setMargins(0, 0, 0, 0);
                layoutNumAbonado.setVisibility(View.VISIBLE);
                layoutNumPoliza.setVisibility(View.VISIBLE);
                layoutCCAA.setVisibility(View.VISIBLE);
                layoutPermisoConducir.setVisibility(View.GONE);
                layoutEdad.setVisibility(View.GONE);
                layoutCategoria.setVisibility(View.GONE);
                break;

            case 11:
                textInputLayoutLicencia.setHint(getResources().getString(R.string.lbl_num_licencia));
                ((LinearLayout.LayoutParams) textInputLayoutLicencia.getLayoutParams()).setMargins(0, 10, 0, 0);
                layoutNumAbonado.setVisibility(View.VISIBLE);
                layoutNumPoliza.setVisibility(View.GONE);
                layoutCCAA.setVisibility(View.VISIBLE);
                layoutPermisoConducir.setVisibility(View.GONE);
                layoutEdad.setVisibility(View.GONE);
                layoutCategoria.setVisibility(View.VISIBLE);
                break;

            case 12:
                textInputLayoutLicencia.setHint(getResources().getString(R.string.lbl_num_dni));
                ((LinearLayout.LayoutParams) textInputLayoutLicencia.getLayoutParams()).setMargins(0, 10, 0, 0);
                layoutNumAbonado.setVisibility(View.GONE);
                layoutNumPoliza.setVisibility(View.GONE);
                layoutCCAA.setVisibility(View.GONE);
                layoutPermisoConducir.setVisibility(View.VISIBLE);
                layoutEdad.setVisibility(View.VISIBLE);
                layoutCategoria.setVisibility(View.GONE);
                break;
        }
    }
      // Lo subo comentado por si en algun momento es necesario utilizarlo. Sirve para que despues
      // de agregarse un evento al Calendario este se sincronice. Esta comentado porque seria pedir
     // otro permiso mas al usuario y la sincronizacion de un dispositivo, en general, es automatica
    // por lo que no seria necesario utilizar es metodo.
//    public void syncCalendars() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        Account[] accounts = AccountManager.get(getApplicationContext()).getAccounts();
//        String authority = CalendarContract.Calendars.CONTENT_URI.getAuthority();
//        for (int i = 0; i < accounts.length; i++) {
//            Bundle extras = new Bundle();
//            extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
//            ContentResolver.requestSync(accounts[i], authority, extras);
//        }
//    }

    /**
     * DatePickerFragment para seleccionar la fecha de expedicion
     */
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int year = 0, month = 0, day = 0;
            final Calendar c = Calendar.getInstance();

            if (layoutFechaExpedicion.getEditText().getText().toString().equals("")) {
                // Use the current date as the default date in the picker
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);
            } else {
                try {
                    c.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(layoutFechaExpedicion.getEditText().getText().toString()));
                    year = c.get(Calendar.YEAR);
                    month = c.get(Calendar.MONTH);
                    day = c.get(Calendar.DAY_OF_MONTH);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, day);
            Date date = cal.getTime();

            String fecha = new DateFormat().format("dd/MM/yyyy", date).toString();
            layoutFechaExpedicion.getEditText().setText(fecha);
        }
    }
}
