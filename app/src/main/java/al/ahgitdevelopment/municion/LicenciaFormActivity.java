package al.ahgitdevelopment.municion;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
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
    private TextInputLayout layoutEdad;
    private AppCompatSpinner tipoLicencia;
    private TextView lblPermiso;
    private AppCompatSpinner tipoPermisoConducir;
    private EditText edad;
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
        layoutEdad = (TextInputLayout) findViewById(R.id.text_input_layout_edad);
        edad = (EditText) findViewById(R.id.form_edad);
        layoutEscala = (LinearLayout) findViewById(R.id.layout_escala);
        tipoEscala = (AppCompatSpinner) findViewById(R.id.form_tipo_escala);

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
                tipoPermisoConducir.setSelection(licencia.getTipoPermisoConduccion());
                edad.setText(String.valueOf(licencia.getEdad()));
                tipoEscala.setSelection(licencia.getEscala());
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        // Eventos que sacan el calendario al recibir el foco en el campo fecha
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

        fieldsUpdateFechaCaducidad();
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
                if (edad.getVisibility() == View.VISIBLE) {
                    bundle.putInt("edad", Integer.parseInt(edad.getText().toString()));
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

    /**
     * Campos y eventos que actualizan la fecha de caducidad
     */
    private void fieldsUpdateFechaCaducidad() {

        //Fecha de expedicion:
        // - Cambio de texto
        fechaExpedicion.addTextChangedListener(new TextWatcher() {
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
                SetVisibilityFields(tipoLicencia.getSelectedItemPosition());
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
        edad.addTextChangedListener(new TextWatcher() {
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
        //Calculamos al fecha de caducidad en función de fecha de expedición introducida
        SimpleDateFormat simpleDate = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(simpleDate.parse(fechaExpedicion.getText().toString()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        switch (tipoLicencia.getSelectedItemPosition()) {
            // Esta licencia no caduca por lo que ponemos una fecha muy lejaana
            case 0: // Licencia A
                calendar.set(3000, 11, 31);
                fechaCaducidad.setText(simpleDate.format(calendar.getTime()));
                break;
            // Sumamos 3 año
            case 1: // Licencia B
            case 5: // Licencia F
                calendar.add(Calendar.YEAR, 3);
                fechaCaducidad.setText(simpleDate.format(calendar.getTime()));
                break;
            // Sumamos 5 años
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
                // Sumamos 10 años
            case 11: // Persmiso de Conducir
                if (!edad.getText().toString().equals("") && Integer.parseInt(edad.getText().toString()) < 65) {
                    switch (tipoPermisoConducir.getSelectedItemPosition()) {
                        case 0: // AM
                        case 1: // A1
                        case 2: // A2
                        case 3: // A
                        case 4: // B
                            calendar.add(Calendar.YEAR, 10);
                            fechaCaducidad.setText(simpleDate.format(calendar.getTime()));
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
                            fechaCaducidad.setText(simpleDate.format(calendar.getTime()));
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
                            fechaCaducidad.setText(simpleDate.format(calendar.getTime()));
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
                            fechaCaducidad.setText(simpleDate.format(calendar.getTime()));
                            break;
                    }
                }
                break;
        }
    }

    /**
     * Metodo para crear o modifciar la notificación
     */
    private void setNotification() {
        //  Se envía la notificación cuando el sistema llegue a la fecha indicada
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new SimpleDateFormat("dd/MM/yyyy").parse(fechaCaducidad.getText().toString()));

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.mipmap.ic_launcher_4_transparent)
                            .setContentTitle("Caducidad de Licencia")
                            .setContentText("Tu licencia caduca hoy")
                            .setSubText(Utils.getStringLicenseFromId(LicenciaFormActivity.this, tipoLicencia.getSelectedItemPosition()).toString() + ": " +
                                    numLicencia.getText().toString());

            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(this, LicenciaFormActivity.class);

            // The stack builder object will contain an artificial back stack for the started Activity.
            // This ensures that navigating backward from the Activity leads out of your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(LicenciaFormActivity.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent)
                    .setAutoCancel(true)
                    .setShowWhen(true)
                    .setWhen(calendar.getTimeInMillis())
                    .setLights(Color.GREEN, 500, 500);

            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // mId allows you to update the notification later on.
            mNotificationManager.notify(Integer.parseInt(numLicencia.getText().toString()), mBuilder.build());


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
        if (edad.getVisibility() == View.VISIBLE && edad.getText().toString().equals("")) {
            edad.setError("Introduce la edad", ResourcesCompat.getDrawable(getResources(), android.R.drawable.stat_notify_error, getTheme()));
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
                edad.setVisibility(View.GONE);
                break;

            case 9:
            case 10:
                textInputLayoutLicencia.setHint(getResources().getString(R.string.lbl_num_licencia));
                numAbonado.setVisibility(View.VISIBLE);
                numSeguro.setVisibility(View.VISIBLE);
                layoutCCAA.setVisibility(View.VISIBLE);
                lblPermiso.setVisibility(View.GONE);
                tipoPermisoConducir.setVisibility(View.GONE);
                edad.setVisibility(View.GONE);
                break;

            case 11:
                textInputLayoutLicencia.setHint(getResources().getString(R.string.lbl_num_dni));
                numAbonado.setVisibility(View.GONE);
                numSeguro.setVisibility(View.GONE);
                layoutCCAA.setVisibility(View.GONE);
                lblPermiso.setVisibility(View.VISIBLE);
                tipoPermisoConducir.setVisibility(View.VISIBLE);
                edad.setVisibility(View.VISIBLE);
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
