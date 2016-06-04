package al.ahgitdevelopment.municion;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RatingBar;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by ahidalgog on 11/04/2016.
 */
public class CompraFormActivity extends AppCompatActivity {
    private TextInputLayout layoutFecha;
    public static EditText fecha;
    private EditText calibre1;
    private EditText calibre2;
    private CheckBox checkSegundoCalibre;
    private EditText unidades;
    private EditText precio;
    private EditText tipoMunicion;
    private EditText pesoMunicion;
    private EditText marcaMunicion;
    private EditText tienda;
    private RatingBar valoracion;

    /**
     * Inicializa la actividad
     *
     * @param savedInstanceState Instancia del estado de la activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_compra);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher_4_transparent);

        calibre1 = (EditText) findViewById(R.id.form_calibre1);
        checkSegundoCalibre = (CheckBox) findViewById(R.id.form_check_segundo_calibre);
        calibre2 = (EditText) findViewById(R.id.form_calibre2);
        unidades = (EditText) findViewById(R.id.form_unidades);
        precio = (EditText) findViewById(R.id.form_precio);
        layoutFecha = (TextInputLayout) findViewById(R.id.layout_form_fecha_compra);
        fecha = (EditText) findViewById(R.id.form_fecha);
        tipoMunicion = (EditText) findViewById(R.id.form_tipo_municion);
        pesoMunicion = (EditText) findViewById(R.id.form_peso_municion);
        marcaMunicion = (EditText) findViewById(R.id.form_marca_municion);
        tienda = (EditText) findViewById(R.id.form_tienda);
        valoracion = (RatingBar) findViewById(R.id.form_ratingBar_valoracion);

        checkSegundoCalibre.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    calibre2.setVisibility(View.VISIBLE);
                } else {
                    calibre2.setVisibility(View.GONE);
                }
            }
        });

        layoutFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callDatePickerFragment();
            }
        });

        fecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callDatePickerFragment();
            }
        });

        // Evento que saca el calendario al recibir el foco en el campo fecha
        fecha.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    callDatePickerFragment();
            }
        });
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

        if (id == R.id.action_save) {
            // Create intent to deliver some kind of result data
            Intent result = new Intent(this, FragmentMainActivity.class);

            Bundle bundle = new Bundle();
            bundle.putString("calibre1", calibre1.getText().toString());
            bundle.putString("calibre2", calibre2.getText().toString());
            bundle.putInt("unidades", Integer.parseInt(unidades.getText().toString()));
            bundle.putString("precio", precio.getText().toString());
            bundle.putString("fecha", fecha.getText().toString());
            bundle.putString("tipo", tipoMunicion.getText().toString());
            bundle.putString("peso", pesoMunicion.getText().toString());
            bundle.putString("marca", marcaMunicion.getText().toString());
            bundle.putString("tienda", tienda.getText().toString());
            bundle.putInt("valoracion", valoracion.getNumStars());

            result.putExtras(bundle);

            setResult(Activity.RESULT_OK, result);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * DatePickerFragment para seleccionar la fecha de compra
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

            String f = new DateFormat().format("dd/MM/yyyy", date).toString();
            fecha.setText(f);
        }
    }
}
