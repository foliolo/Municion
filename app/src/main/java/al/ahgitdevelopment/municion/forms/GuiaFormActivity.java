package al.ahgitdevelopment.municion.forms;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.ads.AdView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

import al.ahgitdevelopment.municion.FragmentMainContent;
import al.ahgitdevelopment.municion.R;
import al.ahgitdevelopment.municion.Utils;
import al.ahgitdevelopment.municion.databases.DataBaseSQLiteHelper;
import al.ahgitdevelopment.municion.datamodel.Guia;

import static al.ahgitdevelopment.municion.di.SharedPrefsModule.PREFS_SHOW_ADS;

/**
 * Created by Alberto on 25/03/2016.
 */
public class GuiaFormActivity extends AppCompatActivity {

    // Constantes para guias maximas para Licencia E por arma
    public static final int MAXIMO_GUIAS_LICENCIA_TIPO_E = 12;
    public static final int MAXIMO_GUIAS_LICENCIA_TIPO_E_ESCOPETA = 6;
    public static final int MAXIMO_GUIAS_LICENCIA_TIPO_E_RIFLE = 6;

    // Constantes para guias maximas por categoria Licencia F
    private final int GUIAS_MAXIMAS_PRIMERA_CATEGORIA = 10;
    private final int GUIAS_MAXIMAS_SEGUNDA_CATEGORIA = 6;
    private final int GUIAS_MAXIMAS_TERCERA_CATEGORIA = 1;

    private ArrayList<String> finalWeapons = new ArrayList<>();

    private int tipoLicencia;
    private AppCompatSpinner tipoArma;
    private TextInputLayout layoutCalibre1;
    private AutoCompleteTextView calibre1;
    private CheckBox segundoCalibre;
    private AutoCompleteTextView calibre2;
    private CheckBox aumentoCupo;
    private TextInputLayout layoutMarca;
    private TextInputLayout layoutModelo;
    private TextInputLayout layoutApodo;
    private TextInputLayout layoutNumGuia;
    private TextInputLayout layoutNumArma;
    private TextInputLayout layoutCupo;
    private TextInputLayout layoutGastado;

    // Mensaje de error antes de guardar
    private TextView mensajeError;
    private String imagePath;

    private DataBaseSQLiteHelper dbSqlHelper;
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
        setContentView(R.layout.form_guia);

        // Toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setSubtitle(R.string.title_nueva_guia);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_bullseye);

        tipoArma = findViewById(R.id.form_tipo_arma);
        layoutCalibre1 = findViewById(R.id.text_input_layout_calibre1);
        calibre1 = findViewById(R.id.form_calibre1);
        segundoCalibre = findViewById(R.id.form_check_segundo_calibre);
        calibre2 = findViewById(R.id.form_calibre2);
        aumentoCupo = findViewById(R.id.form_check_aumento_cupo);
        mensajeError = findViewById(R.id.form_mensaje_guia);
        layoutMarca = findViewById(R.id.text_input_layout_marca);
        layoutModelo = findViewById(R.id.text_input_layout_modelo);
        layoutApodo = findViewById(R.id.text_input_layout_apodo);
        layoutNumGuia = findViewById(R.id.text_input_layout_num_guia);
        layoutNumArma = findViewById(R.id.text_input_layout_num_arma);
        layoutCupo = findViewById(R.id.layout_cupo);
        layoutGastado = findViewById(R.id.text_input_layout_cartuchos_gastados);
        mAdView = findViewById(R.id.login_adView);

        //Municion gastada por defecto = 0
        if (layoutGastado.getEditText().getText().toString().equals("")) {
            layoutGastado.getEditText().setText("0");
        }

        imagePath = null;

        //Carga de calibres
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(GuiaFormActivity.this,
                        android.R.layout.simple_dropdown_item_1line,
                        getResources().getStringArray(R.array.calibres));
        calibre1.setAdapter(adapter);
        calibre2.setAdapter(adapter);

        //Mostrar la lista de tipos de armas en funcion de la licencia
        tipoArmasDisponibles();

        //Inicializacion del cupo por defecto
        tipoArma.setSelection(0);
        layoutCupo.getEditText().setText(String.valueOf(getDefaultCupo(finalWeapons.get(tipoArma.getSelectedItemPosition()))));

        //Carga de datos (en caso de modificacion)
        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().get("tipo_licencia") == null) {
                try {
                    Guia guia = getIntent().getExtras().getParcelable("modify_guia");
                    assert guia != null;
                    tipoLicencia = guia.getTipoLicencia();
                    layoutMarca.getEditText().setText(guia.getMarca());
                    layoutModelo.getEditText().setText(guia.getModelo());
                    layoutApodo.getEditText().setText(guia.getApodo());
                    layoutCalibre1.getEditText().setText(guia.getCalibre1());
                    tipoArma.setSelection(guia.getTipoArma());
                    if (guia.getCalibre2() == null || "".equals(guia.getCalibre2())) {
                        segundoCalibre.setChecked(false);
                        calibre2.setVisibility(View.GONE);
                        calibre2.setText("");
                        guia.setCalibre2("");
                    } else {
                        segundoCalibre.setChecked(true);
                        calibre2.setVisibility(View.VISIBLE);
                    }

                    calibre2.setText(guia.getCalibre2());
                    layoutNumGuia.getEditText().setText(String.valueOf(guia.getNumGuia()));
                    layoutNumArma.getEditText().setText(String.valueOf(guia.getNumArma()));
                    layoutGastado.getEditText().setText(String.valueOf(guia.getGastado()));
                    if (aumentoCupo.isChecked()) {
                        layoutCupo.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
                        layoutCupo.setClickable(true);
                        layoutCupo.setEnabled(true);
                        layoutCupo.setFocusable(true);
                    } else {
                        layoutCupo.getEditText().setInputType(InputType.TYPE_NULL);
                        layoutCupo.setClickable(false);
                        layoutCupo.setEnabled(false);
                        layoutCupo.setFocusable(false);
                    }
                    layoutCupo.getEditText().setText(String.valueOf(guia.getCupo()));
                    imagePath = guia.getImagePath();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }

        segundoCalibre.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    calibre2.setVisibility(View.VISIBLE);
                } else {
                    calibre2.setVisibility(View.GONE);
                    calibre2.setText("");
                }
            }
        });

        aumentoCupo.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                layoutCupo.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
                layoutCupo.setClickable(true);
                layoutCupo.setEnabled(true);
                layoutCupo.setFocusable(true);
            } else {
                layoutCupo.getEditText().setInputType(InputType.TYPE_NULL);
                layoutCupo.setClickable(false);
                layoutCupo.setEnabled(false);
                layoutCupo.setFocusable(false);
            }
        });

        tipoArma.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                layoutCupo.getEditText().setText(String.valueOf(getDefaultCupo(finalWeapons.get(tipoArma.getSelectedItemPosition()))));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // Validaciones de campos obligatorios antes de guardar
        // Marca
        layoutMarca.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (layoutMarca.getEditText().getText().toString().length() < 1) {
                    layoutMarca.setError(getString(R.string.error_before_save));
                }
            }
        });
        // Modelo
        layoutModelo.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (layoutModelo.getEditText().getText().toString().length() < 1) {
                    layoutModelo.setError(getString(R.string.error_before_save));
                }
            }
        });
        // Calibre1
        layoutCalibre1.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (layoutCalibre1.getEditText().getText().toString().length() < 1) {
                    layoutCalibre1.setError(getString(R.string.error_before_save));
                }
            }
        });
        // Num Guia
        layoutNumGuia.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (layoutNumGuia.getEditText().getText().toString().length() < 1) {
                    layoutNumGuia.setError(getString(R.string.error_before_save));
                }
            }
        });
        // Num arma
        layoutNumArma.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (layoutNumArma.getEditText().getText().toString().length() < 1) {
                    layoutNumArma.setError(getString(R.string.error_before_save));
                }
            }
        });
        // Cupo
        layoutCupo.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (layoutCupo.getEditText().getText().toString().length() < 1) {
                    layoutCupo.setError(getString(R.string.error_before_save));
                }
            }
        });

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

    public void fabSaveOnClick(View view) {
        // Validación formulario
        if (!validateForm()) {
            return;
        }

        // Create intent to deliver some kind of result data
        Intent result = new Intent(this, FragmentMainContent.class);
        Bundle bundle = new Bundle();

        if (getIntent().getExtras().getString("tipo_licencia") != null) {
            tipoLicencia = Utils.getLicenciaTipoFromString(GuiaFormActivity.this, getIntent().getExtras().getString("tipo_licencia"));
            if (tipoLicencia == 4) {  // E - Escopeta
                if (checkMaxGuiasForLicenciaTipoE(layoutMarca.getEditText())) { // Maximo: 12 guias
                    return;
                }
                if (tipoArma.getSelectedItemPosition() == 0) { // Arma: escopeta
                    if (checkMaxGuiasEscopeta(layoutMarca.getEditText())) {
                        return;
                    }
                } else if (tipoArma.getSelectedItemPosition() == 1) { // Arma: rifle
                    if (checkMaxGuiasRifle(layoutMarca.getEditText())) {
                        return;
                    }
                }
            } else if (tipoLicencia == 5) { // F - Tiro olimpico
                // Se revisa el numero de guias maximas en funcion de la categoria
                if (checkNumGuiasMaxLicenciaTipoF(layoutMarca.getEditText(), Utils.getMaxCategoria(GuiaFormActivity.this))) {
                    return;
                }
            }
        } else {
            tipoLicencia = ((Guia) getIntent().getExtras().getParcelable("modify_guia")).getTipoLicencia();
            if (tipoLicencia == 4) {  // E - Escopeta
                if (tipoArma.getSelectedItemPosition() == 0) { // Arma: escopeta
                    if (checkMaxGuiasEscopeta(layoutMarca.getEditText())) {
                        return;
                    }
                } else if (tipoArma.getSelectedItemPosition() == 1) { // Arma: rifle
                    if (checkMaxGuiasRifle(layoutMarca.getEditText())) {
                        return;
                    }
                }
            }
        }
        bundle.putInt("tipoLicencia", tipoLicencia);
        bundle.putString("marca", layoutMarca.getEditText().getText().toString());
        bundle.putString("modelo", layoutModelo.getEditText().getText().toString());
        // Control error. He metido vacio porque el campo en BBDD no puede ser  nulo
        if (layoutApodo.getEditText().getText().toString().isEmpty()) {
            layoutApodo.getEditText().setText("");
        }
        bundle.putString("apodo", layoutApodo.getEditText().getText().toString());
        bundle.putInt("tipoArma", tipoArma.getSelectedItemPosition());
        bundle.putString("calibre1", layoutCalibre1.getEditText().getText().toString());
        if (segundoCalibre.isChecked()) {
            // Control error. He metido vacio porque el campo en BBDD no puede ser  nulo
            if (calibre2.getText().toString().isEmpty()) {
                calibre2.setText("");
            }
            bundle.putString("calibre2", calibre2.getText().toString());
        }
        bundle.putString("numGuia", layoutNumGuia.getEditText().getText().toString().trim());
        bundle.putString("numArma", layoutNumArma.getEditText().getText().toString().trim());
        bundle.putInt("cupo", Integer.parseInt(layoutCupo.getEditText().getText().toString()));
        bundle.putInt("gastado", Integer.parseInt(layoutGastado.getEditText().getText().toString()));

        //Paso de vuelta de la posicion del item en el arrayet
        if (getIntent().getExtras() != null)
            bundle.putInt("position", getIntent().getExtras().getInt("position", -1));

        bundle.putString("imagePath", imagePath);

        result.putExtras(bundle);

        setResult(Activity.RESULT_OK, result);
        finish();
    }

    private boolean checkMaxGuiasForLicenciaTipoE(View view) {
        dbSqlHelper = new DataBaseSQLiteHelper(getApplicationContext());
        if (dbSqlHelper.getNumLicenciasTipoE() >= MAXIMO_GUIAS_LICENCIA_TIPO_E) {
            Snackbar.make(view, R.string.dialog_guia_licencia_tipoE, Snackbar.LENGTH_LONG)
                    .setAction(android.R.string.ok, null)
                    .show();
            return true;
        }
        return false;
    }

    private boolean checkMaxGuiasEscopeta(View view) {
        dbSqlHelper = new DataBaseSQLiteHelper(getApplicationContext());
        if (dbSqlHelper.getNumGuiasLicenciaTipoEscopeta() >= MAXIMO_GUIAS_LICENCIA_TIPO_E_ESCOPETA) {
            Snackbar.make(view, R.string.dialog_guia_licencia_tipoE_escopeta, Snackbar.LENGTH_LONG)
                    .setAction(android.R.string.ok, null)
                    .show();
            return true;
        }
        return false;
    }

    private boolean checkMaxGuiasRifle(View view) {
        dbSqlHelper = new DataBaseSQLiteHelper(getApplicationContext());
        if (dbSqlHelper.getNumGuiasLicenciaTipoRifle() >= MAXIMO_GUIAS_LICENCIA_TIPO_E_RIFLE) {
            Snackbar.make(view, R.string.dialog_guia_licencia_tipoE_rifle, Snackbar.LENGTH_LONG)
                    .setAction(android.R.string.ok, null)
                    .show();
            return true;
        }
        return false;
    }

    private boolean checkNumGuiasMaxLicenciaTipoF(View view, int maxCategoria) {
        // 3ª Categoria
        if (maxCategoria == 2 && Utils.getNumGuias(GuiaFormActivity.this) >= GUIAS_MAXIMAS_TERCERA_CATEGORIA) {
            Snackbar.make(view, R.string.dialog_guia_licencia_federativa_categoria3, Snackbar.LENGTH_LONG)
                    .setAction(android.R.string.ok, null)
                    .show();
            return true;
        }
        // 2ª Categoria
        else if (maxCategoria == 1 && Utils.getNumGuias(GuiaFormActivity.this) >= GUIAS_MAXIMAS_SEGUNDA_CATEGORIA) {
            Snackbar.make(view, R.string.dialog_guia_licencia_federativa_categoria2, Snackbar.LENGTH_LONG)
                    .setAction(android.R.string.ok, null)
                    .show();
            return true;
        }
        // 1ª Categoria
        else if (maxCategoria == 0 && Utils.getNumGuias(GuiaFormActivity.this) >= GUIAS_MAXIMAS_PRIMERA_CATEGORIA) {
            Snackbar.make(view, R.string.dialog_guia_licencia_federativa_categoria1, Snackbar.LENGTH_LONG)
                    .setAction(android.R.string.ok, null)
                    .show();
            return true;
        }
        // No se evaluan las categorías maximas
        else if (maxCategoria == -1) {

        }
        return false;
    }

    private boolean validateForm() {
        boolean retorno = true;
        // Validaciones campos formularios
        // Marca
        if (layoutMarca.getEditText().getText().toString().length() < 1) {
            layoutMarca.setError(getString(R.string.error_before_save));
            retorno = false;
        }
        // Modelo
        if (layoutModelo.getEditText().getText().toString().length() < 1) {
            layoutModelo.setError(getString(R.string.error_before_save));
            retorno = false;
        }
        // Apodo
        if (layoutApodo.getEditText().getText().toString().length() < 1) {
            layoutApodo.setError(getString(R.string.error_before_save));
            retorno = false;
        }
        // Calibre1
        if (layoutCalibre1.getEditText().getText().toString().length() < 1) {
            layoutCalibre1.setError(getString(R.string.error_before_save));
            retorno = false;
        }
        // Num Guia
        if (layoutNumGuia.getEditText().getText().toString().length() < 1) {
            layoutNumGuia.setError(getString(R.string.error_before_save));
            retorno = false;
        }
        // Num Arma
        if (layoutNumArma.getEditText().getText().toString().length() < 1) {
            layoutNumArma.setError(getString(R.string.error_before_save));
            retorno = false;
        }
        // Cupo
        if (layoutCupo.getEditText().getText().toString().length() < 1) {
            layoutCupo.setError(getString(R.string.error_before_save));
            retorno = false;
        }
        // Gastado
//        if (layoutGastado.getEditText().getText().toString().length() < 1) {
//            layoutGastado.setError(getString(R.string.error_before_save));
//            retorno = false;
//        }
        if (!retorno) {
            mensajeError.setVisibility(View.VISIBLE);
            mensajeError.setText(getString(R.string.error_mensaje_cabecera));
            mensajeError.setTextColor(Color.parseColor("#0000ff"));
        }
        return retorno;
    }

    private void tipoArmasDisponibles() throws Resources.NotFoundException {
        finalWeapons.clear();
        ArrayAdapter<String> armas = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, android.R.id.text1);
        armas.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tipoArma.setAdapter(armas);
        armas.clear();

        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().get("tipo_licencia") != null) {
                String nombreLicencia = getIntent().getExtras().get("tipo_licencia").toString();
                String idNombreLicencia = nombreLicencia.split(" ")[0];
                armas.addAll(getResources().getStringArray(getResources().getIdentifier(idNombreLicencia, "array", getPackageName())));
            } else { //Deberia entrar en la modificacion de un arma
                try {
                    Guia guia = getIntent().getExtras().getParcelable("modify_guia");
                    String nombreLicencia = Utils.getStringLicenseFromId(GuiaFormActivity.this, guia.getTipoLicencia());
                    String idNombreLicenia = nombreLicencia.split(" ")[0];
                    armas.addAll(getResources().getStringArray(getResources().getIdentifier(idNombreLicenia, "array", getPackageName())));
                } catch (ArrayIndexOutOfBoundsException ex) {
                    Log.e(getPackageName(), "Fallo obteniendo licencia");
                }
            }
        } else {
            Toast.makeText(GuiaFormActivity.this, "Error - No se muestran armas!!!", Toast.LENGTH_SHORT).show();
        }
        armas.notifyDataSetChanged();
        tipoArma.setSelection(0, true); // Default Value

        for (int i = 0; i < armas.getCount(); i++)
            finalWeapons.add(armas.getItem(i));
    }

    /**
     * Este método retorno el cupo por defecto para el arma seleccionada en el desplegable.
     * En caso de estar seleccionado el aumento de cupo, retornamos el valor que tenga el campo (no el de por defecto)
     *
     * @return Retorna el cupo por defecto
     */
    private Integer getDefaultCupo(String arma) {
        Integer defaultCupo;

        if (!aumentoCupo.isChecked()) {
//            String nombreArma = Utils.getStringArmaFromId(GuiaFormActivity.this, tipoArma.getSelectedItemPosition());
            switch (arma) {
                case "Pistola":
                case "Gun":
                    defaultCupo = 100;
                    layoutCupo.getEditText().setInputType(InputType.TYPE_NULL);
                    break;
                case "Escopeta":
                case "Shotgun":
                    defaultCupo = 5000;
                    layoutCupo.getEditText().setInputType(InputType.TYPE_NULL);
                    break;
                case "Rifle":
                    defaultCupo = 1000;
                    layoutCupo.getEditText().setInputType(InputType.TYPE_NULL);
                    break;
                case "Revolver":
                    defaultCupo = 100;
                    layoutCupo.getEditText().setInputType(InputType.TYPE_NULL);
                    break;
                case "Avancarga":
                    defaultCupo = 1000;
                    layoutCupo.getEditText().setInputType(InputType.TYPE_NULL);
                    break;
                default:
                    defaultCupo = 0;
                    aumentoCupo.setChecked(true);
                    layoutCupo.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
            }
        } else {
            defaultCupo = Integer.parseInt(layoutCupo.getEditText().getText().toString());
            layoutCupo.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
        }
        return defaultCupo;
    }
}
