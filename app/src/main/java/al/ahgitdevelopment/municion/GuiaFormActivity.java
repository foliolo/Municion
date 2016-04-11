package al.ahgitdevelopment.municion;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

/**
 * Created by Alberto on 25/03/2016.
 */
public class GuiaFormActivity extends AppCompatActivity {
    private AppCompatEditText nombreArma;
    private AppCompatEditText marca;
    private AppCompatEditText modelo;
    private AppCompatEditText numGuia;
    private AppCompatEditText calibre;
    private AppCompatSpinner tipoArma;
    private AppCompatEditText cartuchosGastados;
    private AppCompatEditText cartuchosTotales;

    /**
     * Inicializa la actividad
     * @param savedInstanceState Instancia del estado de la activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_guia);

        nombreArma = (AppCompatEditText) findViewById(R.id.form_nombre_arma);
        marca = (AppCompatEditText) findViewById(R.id.form_marca);
        modelo = (AppCompatEditText) findViewById(R.id.form_modelo);
        numGuia = (AppCompatEditText) findViewById(R.id.form_num_guia);
        calibre = (AppCompatEditText) findViewById(R.id.form_calibre);
        tipoArma = (AppCompatSpinner) findViewById(R.id.form_tipo_arma);
        cartuchosGastados = (AppCompatEditText) findViewById(R.id.form_cartuchos_gastados);
        cartuchosTotales = (AppCompatEditText) findViewById(R.id.form_cartuchos_totales);

        tipoArma.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.tipo_armas)));
        tipoArma.setSelection(0);
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
            bundle.putString("calibre", calibre.getText().toString());
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
