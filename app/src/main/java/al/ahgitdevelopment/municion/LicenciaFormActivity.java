package al.ahgitdevelopment.municion;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import al.ahgitdevelopment.municion.DataModel.Licencia;

/**
 * Created by Alberto on 24/05/2016.
 */
public class LicenciaFormActivity extends AppCompatActivity {
    public static EditText fechaExpedicion;
    private TextInputLayout textInputLayoutLicencia;
    private TextInputLayout layoutFechaExpedicion;
    private TextInputLayout layoutFechaCaducidad;
    private AppCompatSpinner tipoLicencia;
    private TextView lblPermiso;
    private AppCompatSpinner tipoPermisoConducir;
    private EditText numLicencia;
    private EditText fechaCaducidad;
    private EditText numAbonado;
    private EditText numSeguro;
    private LinearLayout layoutCCAA;
    private AppCompatSpinner autonomia;
    private LinearLayout layoutEscala;
    private AppCompatSpinner tipoEscala;

    private PendingIntent pendingIntent;

    /**
     * Inicializa la actividad
     *
     * @param savedInstanceState Instancia del estado de la activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_licencia);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher_4_transparent);

        tipoLicencia = (AppCompatSpinner) findViewById(R.id.form_tipo_licencia);
        textInputLayoutLicencia = (TextInputLayout) findViewById(R.id.text_input_layout_licencia);
        numLicencia = (EditText) findViewById(R.id.form_num_licencia);
        layoutFechaExpedicion = (TextInputLayout) findViewById(R.id.layout_form_fecha_expedicion);
        fechaExpedicion = (EditText) findViewById(R.id.form_fecha_expedicion);
        layoutFechaCaducidad = (TextInputLayout) findViewById(R.id.layout_form_fecha_caducidad);
        fechaCaducidad = (EditText) findViewById(R.id.form_fecha_caducidad);
        numAbonado = (EditText) findViewById(R.id.form_num_abonado);
        numSeguro = (EditText) findViewById(R.id.form_num_poliza);
        layoutCCAA = (LinearLayout) findViewById(R.id.layout_ccaa);
        autonomia = (AppCompatSpinner) findViewById(R.id.form_ccaa);
        lblPermiso = (TextView) findViewById(R.id.form_lbl_tipo_permiso_conducir);
        tipoPermisoConducir = (AppCompatSpinner) findViewById(R.id.form_tipo_permiso_conducir);
        layoutEscala = (LinearLayout) findViewById(R.id.layout_escala);
        tipoEscala = (AppCompatSpinner) findViewById(R.id.form_tipo_escala);
//      button1 = (Button) findViewById(R.id.button1);
//      manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        //Carga de datos (en caso de modificacion)
        if (getIntent().getExtras() != null) {
            try {
                Licencia licencia = getIntent().getExtras().getParcelable("modify_licencia");
                tipoLicencia.setSelection(licencia.getTipo());
                numLicencia.setText(String.valueOf(licencia.getNumLicencia()));
                fechaExpedicion.setText(new SimpleDateFormat("dd/MM/yyyy").format(licencia.getFechaExpedicion().getTime()));
                numAbonado.setText(String.valueOf(licencia.getNumAbonado()));
                numSeguro.setText(String.valueOf(licencia.getNumLicencia()));
                autonomia.setSelection(licencia.getAutonomia());
                tipoEscala.setSelection(licencia.getEscala());
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        layoutFechaExpedicion.setOnClickListener(new View.OnClickListener() {
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

        // Evento que saca el calendario al recibir el foco en el campo fecha
        fechaExpedicion.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    callDatePickerFragment();
                }
            }
        });

        fechaExpedicion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callDatePickerFragment();
            }
        });

        fechaExpedicion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Calculamos al fecha de caducidad en función de fecha de expedición introducida
                SimpleDateFormat simpleDate = new SimpleDateFormat("dd/MM/yyyy");
                Calendar calendar = Calendar.getInstance();
                try {
                    calendar.setTime(simpleDate.parse(fechaExpedicion.getText().toString()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                switch (tipoLicencia.getSelectedItemPosition()) {
                    // Sumamos 3 año
                    case 1: // Licencia B
                    case 5: // Licencia F
                        calendar.add(Calendar.YEAR, 3);
                        fechaCaducidad.setText(simpleDate.format(calendar.getTime()));
                        break;
                    // Sumamos 5 años
                    case 0: // Licencia A
                    case 2: // Licencia C
                    case 3: // Licencia D
                    case 4: // Licencia E
                    case 6: // Licencia AE
                    case 7: // Licencia AER
                    case 8: // Licencia Libero Coleccionesta
                        calendar.add(Calendar.YEAR, 5);
                        fechaCaducidad.setText(simpleDate.format(calendar.getTime()));
                        break;
                    // Ajustamos al final de año
                    case 9: // Autonomica Caza
                    case 10: // Autonomica Pesca
                        calendar.set(calendar.get(Calendar.YEAR), 11, 31);
                        fechaCaducidad.setText(simpleDate.format(calendar.getTime()));
                        break;
                    // Sumamos 10 años
                    case 11: // Persmiso de Conducir
                        calendar.add(Calendar.YEAR, 10);
                        fechaCaducidad.setText(simpleDate.format(calendar.getTime()));
                        break;
                }
            }
        });

        tipoLicencia.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!fechaExpedicion.getText().toString().equals("")) {
                    //Calculamos al fecha de caducidad en función de fecha de expedición introducida
                    SimpleDateFormat simpleDate = new SimpleDateFormat("dd/MM/yyyy");
                    Calendar calendar = Calendar.getInstance();
                    try {
                        calendar.setTime(simpleDate.parse(fechaExpedicion.getText().toString()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    switch (tipoLicencia.getSelectedItemPosition()) {
                        // Sumamos 3 año
                        case 1: // Licencia B
                        case 5: // Licencia F
                            calendar.add(Calendar.YEAR, 3);
                            fechaCaducidad.setText(simpleDate.format(calendar.getTime()));
                            break;
                        // Sumamos 5 años
                        case 0: // Licencia A
                        case 2: // Licencia C
                        case 3: // Licencia D
                        case 4: // Licencia E
                        case 6: // Licencia AE
                        case 7: // Licencia AER
                        case 8: // Licencia Libero Coleccionesta
                            calendar.add(Calendar.YEAR, 5);
                            fechaCaducidad.setText(simpleDate.format(calendar.getTime()));
                            break;
                        // Ajustamos al final de año
                        case 9: // Autonomica Caza
                        case 10: // Autonomica Pesca
                            calendar.set(calendar.get(Calendar.YEAR), 11, 31);
                            fechaCaducidad.setText(simpleDate.format(calendar.getTime()));
                            break;
                        // Sumamos 10 años
                        case 11: // Persmiso de Conducir
                            calendar.add(Calendar.YEAR, 10);
                            fechaCaducidad.setText(simpleDate.format(calendar.getTime()));
                            break;
                    }
                }

                SetVisibilityFields(tipoLicencia.getSelectedItemPosition());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

//        // FIXME Cambiar por la lógica que corresponda para las fechas de las notificaciones
//        //  Se envía la notificación cuando el sistema llegue a la fecha indicada
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.MONTH, 6);
//        calendar.set(Calendar.YEAR, 2016);
//        calendar.set(Calendar.DAY_OF_MONTH, 25);
//        calendar.set(Calendar.HOUR_OF_DAY, 18);
//        calendar.set(Calendar.MINUTE, 32);
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.AM_PM,Calendar.PM);
//
//        Intent intent = new Intent(LicenciaFormActivity.this, ReceiverBroad.class);
//        pendingIntent = PendingIntent.getBroadcast(LicenciaFormActivity.this, 0, intent,0);
//
//        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
//        alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);

        // Para lanzar la notificacion con un boton
//        button1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // API level 11
//                Intent intent = new Intent(LicenciaFormActivity.this, NotificationView.class);
//
//                PendingIntent pendingIntent = PendingIntent.getActivity(LicenciaFormActivity.this, 1, intent, 0);
//
//                Notification.Builder builder = new Notification.Builder(LicenciaFormActivity.this);
//
//                builder.setAutoCancel(true);
//                builder.setContentTitle("WhatsApp Notification");
//                builder.setContentText("You have a new message");
//                builder.setSmallIcon(R.drawable.ic_notifications_black_24dp);
//                builder.setContentIntent(pendingIntent);
//                builder.setOngoing(true);
//                builder.setNumber(100);
//                // API level 16
////                builder.setSubText("This is subtext...");
////                builder.build();
//
//                myNotication = builder.getNotification();
//                manager.notify(11, myNotication);
//
//                /*
//                //API level 8
//                Notification myNotification8 = new Notification(R.drawable.ic_launcher, "this is ticker text 8", System.currentTimeMillis());
//
//                Intent intent2 = new Intent(MainActivity.this, SecActivity.class);
//                PendingIntent pendingIntent2 = PendingIntent.getActivity(getApplicationContext(), 2, intent2, 0);
//                myNotification8.setLatestEventInfo(getApplicationContext(), "API level 8", "this is api 8 msg", pendingIntent2);
//                manager.notify(11, myNotification8);
//                */
//
//            }
//        });
    }

    private void callDatePickerFragment() {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_form, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (controlCampos()) {
            if (id == R.id.action_save) {
                // Create intent to deliver some kind of result data
                Intent result = new Intent(this, FragmentMainActivity.class);

                Bundle bundle = new Bundle();
                bundle.putInt("tipo", tipoLicencia.getSelectedItemPosition());
                if (tipoPermisoConducir.getVisibility() == View.VISIBLE) {
                    bundle.putInt("tipo_permiso_conduccion", tipoPermisoConducir.getSelectedItemPosition());
                } else {
                    bundle.putInt("tipo_permiso_conduccion", -1); // Mandamos -1 para que el array adapter no muestre este campo
                }
                bundle.putInt("num_licencia", Integer.parseInt(numLicencia.getText().toString()));
                bundle.putString("fecha_expedicion", fechaExpedicion.getText().toString());
                bundle.putString("fecha_caducidad", fechaCaducidad.getText().toString());
                if (numAbonado.getVisibility() == View.VISIBLE) {
                    bundle.putInt("num_abonado", Integer.parseInt(numAbonado.getText().toString()));
                }
                if (numSeguro.getVisibility() == View.VISIBLE) {
                    bundle.putString("num_seguro", numSeguro.getText().toString());
                }
                if (layoutCCAA.getVisibility() == View.VISIBLE) {
                    bundle.putInt("autonomia", autonomia.getSelectedItemPosition());
                } else {
                    bundle.putInt("autonomia", -1); // Mandamos -1 para que el array adapter no muestre este campo
                }
                if (layoutEscala.getVisibility() == View.VISIBLE) {
                    bundle.putInt("escala", tipoEscala.getSelectedItemPosition());
                } else {
                    bundle.putInt("escala", -1); // Mandamos -1 para que el array adapter no muestre este campo
                }

                //Paso de vuelta de la posicion del item en el array
                if (getIntent().getExtras() != null)
                    bundle.putInt("position", getIntent().getExtras().getInt("position", -1));

                //Agregar notificacion
                setNotification();


                result.putExtras(bundle);

                setResult(Activity.RESULT_OK, result);
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void setNotification() {
        //  Se envía la notificación cuando el sistema llegue a la fecha indicada
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(fechaCaducidad.getText().toString()));

            Intent intent = new Intent(LicenciaFormActivity.this, ReceiverBroad.class);
            intent.putExtra("licencia", getResources().getStringArray(R.array.tipo_licencias)[tipoLicencia.getSelectedItemPosition()]);
            pendingIntent = PendingIntent.getBroadcast(LicenciaFormActivity.this, 0, intent, 0);

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);

            Toast.makeText(LicenciaFormActivity.this, "Notificacion creada para el día: " + fechaCaducidad.getText().toString(), Toast.LENGTH_LONG).show();
        } catch (ParseException ex) {
            Log.e(getPackageName(), "Fallo al crear la notificacion", ex);
        }
    }

    /**
     * Control de campos obligarios para poder guardar el formulario
     *
     * @return Flag indicando si estan todos los campos obligarios (true), en caso contrario (false)
     */
    private boolean controlCampos() {
        boolean flag = true;

        if (numLicencia.getText().toString().equals("")) {
            numLicencia.setError("Introduce el número de licencia", ResourcesCompat.getDrawable(getResources(), android.R.drawable.stat_notify_error, getTheme()));
            flag = false;
        }
        if (fechaExpedicion.getText().toString().equals("")) {
            layoutFechaExpedicion.setError("Introdce la fecha de expedición");
            flag = false;
        }
        if (fechaCaducidad.getText().toString().equals("")) {
            layoutFechaCaducidad.setError("Introdce la fecha de caducidad");
            flag = false;
        }
        if (numAbonado.getVisibility() == View.VISIBLE && numAbonado.getText().toString().equals("")) {
            numAbonado.setError("Introduce el número de abonado", ResourcesCompat.getDrawable(getResources(), android.R.drawable.stat_notify_error, getTheme()));
            flag = false;
        }
        if (numSeguro.getVisibility() == View.VISIBLE && numSeguro.getText().toString().equals("")) {
            numSeguro.setError("Introduce el número de la poliza del seguro", ResourcesCompat.getDrawable(getResources(), android.R.drawable.stat_notify_error, getTheme()));
            flag = false;
        }

        return flag;
    }

    /**
     * Método para modificar la visibilidad de los campos en función del tipo de licencia seleccionado
     *
     * @param tipoLicencia Licencia seleccionada
     */
    private void SetVisibilityFields(int tipoLicencia) {
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
                numAbonado.setVisibility(View.GONE);
                numSeguro.setVisibility(View.GONE);
                layoutCCAA.setVisibility(View.GONE);
                lblPermiso.setVisibility(View.GONE);
                tipoPermisoConducir.setVisibility(View.GONE);
                break;

            case 9:
            case 10:
                textInputLayoutLicencia.setHint(getResources().getString(R.string.lbl_num_licencia));
                numAbonado.setVisibility(View.VISIBLE);
                numSeguro.setVisibility(View.VISIBLE);
                layoutCCAA.setVisibility(View.VISIBLE);
                lblPermiso.setVisibility(View.GONE);
                tipoPermisoConducir.setVisibility(View.GONE);
                break;

            case 11:
                textInputLayoutLicencia.setHint(getResources().getString(R.string.lbl_num_dni));
                numAbonado.setVisibility(View.GONE);
                numSeguro.setVisibility(View.GONE);
                layoutCCAA.setVisibility(View.GONE);
                lblPermiso.setVisibility(View.VISIBLE);
                tipoPermisoConducir.setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * DatePickerFragment para seleccionar la fecha de expedicion
     */
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

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
            fechaExpedicion.setText(fecha);
        }

    }
}
