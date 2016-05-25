package al.ahgitdevelopment.municion;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

/**
 * Created by Alberto on 25/03/2016.
 */
public class GuiaFormActivity extends AppCompatActivity {
    private EditText nombreArma;
    private EditText marca;
    private EditText modelo;
    private EditText numGuia;
    private EditText calibre1;
    private CheckBox segundoCalibre;
    private EditText calibre2;
    private AppCompatSpinner tipoArma;
    private EditText cartuchosGastados;
    private EditText cartuchosTotales;

    /**
     * Inicializa la actividad
     *
     * @param savedInstanceState Instancia del estado de la activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_guia);

        nombreArma = (EditText) findViewById(R.id.form_apodo_arma);
        marca = (EditText) findViewById(R.id.form_marca);
        modelo = (EditText) findViewById(R.id.form_modelo);
        numGuia = (EditText) findViewById(R.id.form_num_guia);
        calibre1 = (EditText) findViewById(R.id.form_calibre1);
        segundoCalibre = (CheckBox) findViewById(R.id.form_check_segundo_calibre);
        calibre2 = (EditText) findViewById(R.id.form_calibre2);
        tipoArma = (AppCompatSpinner) findViewById(R.id.form_tipo_arma);
        cartuchosGastados = (EditText) findViewById(R.id.form_cartuchos_gastados);
        cartuchosTotales = (EditText) findViewById(R.id.form_cartuchos_totales);

        segundoCalibre.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    calibre2.setVisibility(View.VISIBLE);
                } else {
                    calibre2.setVisibility(View.GONE);
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

        if (id == R.id.action_save) {
            // Create intent to deliver some kind of result data
            Intent result = new Intent(this, FragmentMainActivity.class);

            Bundle bundle = new Bundle();
            bundle.putString("nombreArma", nombreArma.getText().toString());
            bundle.putString("marca", marca.getText().toString());
            bundle.putString("modelo", modelo.getText().toString());
            bundle.putInt("numGuia", Integer.parseInt(numGuia.getText().toString()));
            bundle.putString("calibre1", calibre1.getText().toString());
            if (segundoCalibre.isChecked())
                bundle.putString("calibre2", calibre2.getText().toString());
            bundle.putString("tipoArma", (String) tipoArma.getSelectedItem());
            bundle.putInt("cartuchosGastados", Integer.parseInt(cartuchosGastados.getText().toString()));
            bundle.putInt("cartuchosTotales", Integer.parseInt(cartuchosTotales.getText().toString()));
            result.putExtras(bundle);

            setResult(Activity.RESULT_OK, result);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
