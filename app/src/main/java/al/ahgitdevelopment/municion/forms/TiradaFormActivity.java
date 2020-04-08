package al.ahgitdevelopment.municion.forms;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.ads.AdView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crash.FirebaseCrash;

import java.util.Calendar;
import java.util.Date;

import al.ahgitdevelopment.municion.FragmentMainActivity;
import al.ahgitdevelopment.municion.R;
import al.ahgitdevelopment.municion.Utils;
import al.ahgitdevelopment.municion.datamodel.Tirada;

/**
 * Created by Alberto on 24/05/2016.
 */
public class TiradaFormActivity extends AppCompatActivity {
    private static final String TAG = "TiradaFormActivity";
    private static TextInputLayout fecha;
    private SharedPreferences prefs;
    private Toolbar toolbar;
    private TextInputLayout descripcion;
    private AppCompatSpinner rango;
    private TextInputLayout puntuacion;

    private AdView mAdView;

    /**
     * Inicializa la actividad
     *
     * @param savedInstanceState Instancia del estado de la activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_tirada);

        // Toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setSubtitle(R.string.title_nueva_tirada);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_bullseye);

        descripcion = findViewById(R.id.form_tirada_descripcion);
        rango = findViewById(R.id.form_tirada_rango);
        fecha = findViewById(R.id.form_tirada_fecha);
        puntuacion = findViewById(R.id.form_tirada_puntuacion);
        mAdView = findViewById(R.id.login_adView);

        //Carga de datos (en caso de modificacion)
        if (getIntent().getExtras() != null) {
            try {
                Tirada tirada = new Tirada((Tirada) getIntent().getExtras().getParcelable("modify_tirada"));
                descripcion.getEditText().setText(tirada.getDescripcion());
                rango.setSelection(getRangePositionFromString(tirada.getRango()));
                fecha.getEditText().setText(tirada.getFecha());
                puntuacion.getEditText().setText(String.valueOf(tirada.getPuntuacion()));
            } catch (NullPointerException ex) {
                FirebaseCrash.logcat(Log.ERROR, TAG, "Fallo al modificar una tirada en el formulario");
                FirebaseCrash.report(ex);
            }
        }

        // Eventos que sacan el calendario al recibir el foco en el campo fecha
        fecha.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callDatePickerFragment();
            }
        });
        fecha.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    callDatePickerFragment();
                }
            }
        });
        fecha.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    callDatePickerFragment();
                }
            }
        });

        // Gestion de anuncios
        prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        if (prefs.getBoolean(Utils.PREFS_SHOW_ADS, true)) {
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
            Intent result = new Intent(this, FragmentMainActivity.class);

            Bundle bundle = new Bundle();
            bundle.putParcelable("modify_tirada", getCurrenteTirada());

            //Paso de vuelta de la posicion del item en el array
            if (getIntent().getExtras() != null)
                bundle.putInt("position", getIntent().getExtras().getInt("position", -1));

            result.putExtras(bundle);

            setResult(Activity.RESULT_OK, result);
            finish();
        }
    }

    /**
     * Devuelve los datos de la interfaz en un objeto Tirada
     *
     * @return Objeto Tirada con todos los datos del formulario
     */
    private Tirada getCurrenteTirada() {
        Tirada tirada = new Tirada();
        try {
            tirada.setDescripcion(descripcion.getEditText().getText().toString());
            tirada.setRango(getStringFromRangePosition(rango.getSelectedItemPosition()));
            tirada.setFecha(fecha.getEditText().getText().toString());
            tirada.setPuntuacion(checkScore(puntuacion.getEditText().getText().toString()));
        } catch (Exception ex) {
            Log.e(getPackageName(), "Fallo en el empaquetado de la tirada para la notificación", ex);
        }
        return tirada;
    }

    private int checkScore(String s) {
        if (!s.equals("")) {
            int maxValue = 600;
            int val = Integer.parseInt(s);
            if (val > maxValue) {
                val = maxValue;
                Toast.makeText(this, getString(R.string.form_tirada_aviso_puntuacion), Toast.LENGTH_SHORT).show();
            }
            return val;
        }
        return 0;
    }

    private int getRangePositionFromString(String rango) {
        String[] listRangos = getResources().getStringArray(R.array.rango_tirada);

        for (int i = 0; i < listRangos.length; i++) {
            if (listRangos[i].equals(rango)) {
                return i;
            }
        }
        return -1;
    }

    private String getStringFromRangePosition(int rangoSelected) {
        String[] listRangos = getResources().getStringArray(R.array.rango_tirada);
        return listRangos[rangoSelected];
    }

    /**
     * Control de campos obligarios para poder guardar el formulario
     *
     * @return Flag indicando si estan todos los campos obligarios (true), en caso contrario (false)
     */
    private boolean controlCampos() {
        boolean flag = true;

        if (descripcion.getEditText().getText().toString().equals("")) {
            descripcion.setError(getString(R.string.error_before_save));
            flag = false;
        }
        if (fecha.getEditText().getText().toString().equals("")) {
            fecha.setError(getString(R.string.error_before_save));
            flag = false;
        }
        if (puntuacion.getEditText().getText().toString().equals("")) {
            puntuacion.setError(getString(R.string.error_before_save));
            flag = false;
        }

        return flag;
    }

    /**
     * DatePickerFragment para seleccionar la fecha de expedicion
     */
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int year = 0, month = 0, day = 0;
            Calendar calendar = Calendar.getInstance();

            if (fecha.getEditText().getText().toString().equals("")) {
                // Use the current date as the default date in the picker
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);
            } else {
                calendar.setTime(Utils.getDateFromString(fecha.getEditText().getText().toString()));
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);
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

            String currentDate = new DateFormat().format("dd/MM/yyyy", date).toString();
            fecha.getEditText().setText(currentDate);
        }
    }
}
