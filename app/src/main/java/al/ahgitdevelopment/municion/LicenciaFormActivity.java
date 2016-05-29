package al.ahgitdevelopment.municion;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Alberto on 24/05/2016.
 */
public class LicenciaFormActivity extends AppCompatActivity {
    TextInputLayout layoutFechaExpedicion;
    TextInputLayout layoutFechaCaducidad;
    private AppCompatSpinner tipoLicencia;
    private EditText numLicencia;
    private EditText fechaExpedicion;
    private EditText fechaCaducidad;

    /**
     * Inicializa la actividad
     *
     * @param savedInstanceState Instancia del estado de la activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_licencia);

        tipoLicencia = (AppCompatSpinner) findViewById(R.id.form_tipo_licencia);
        numLicencia = (EditText) findViewById(R.id.form_num_licencia);
        layoutFechaExpedicion = (TextInputLayout) findViewById(R.id.layout_form_fecha_expedicion);
        fechaExpedicion = (EditText) findViewById(R.id.form_fecha_expedicion);
        layoutFechaCaducidad = (TextInputLayout) findViewById(R.id.layout_form_fecha_caducidad);
        fechaCaducidad = (EditText) findViewById(R.id.form_fecha_caducidad);

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
                    case 0: // Sumamos 1 año
                    case 1:
                    case 2:
                    case 3:
                        calendar.add(Calendar.YEAR, 1);
                        fechaCaducidad.setText(simpleDate.format(calendar.getTime()));
                        break;
                    case 4: // Sumamos 5 años
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                        calendar.add(Calendar.YEAR, 5);
                        fechaCaducidad.setText(simpleDate.format(calendar.getTime()));
                        break;
                }
            }
        });
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
                bundle.putString("tipo", tipoLicencia.getSelectedItem().toString());
                bundle.putInt("num_licencia", Integer.parseInt(numLicencia.getText().toString()));
                bundle.putString("fecha_expedicion", fechaExpedicion.getText().toString());
                bundle.putString("fecha_caducidad", fechaCaducidad.getText().toString());

                result.putExtras(bundle);

                setResult(Activity.RESULT_OK, result);
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Control de campos obligarios para poder guardar el formulario
     *
     * @return Flag indicando si estan todos los campos obligarios (true), en caso contrario (false)
     */
    private boolean controlCampos() {
        boolean flag = true;

        if (numLicencia.getText().toString().equals("")) {
            numLicencia.setError("Introdce el número de licencia", ResourcesCompat.getDrawable(getResources(), android.R.drawable.stat_notify_error, getTheme()));
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

        return flag;
    }
}
