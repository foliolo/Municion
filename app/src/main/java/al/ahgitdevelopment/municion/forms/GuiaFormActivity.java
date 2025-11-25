package al.ahgitdevelopment.municion.forms;

import android.app.Activity;
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

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Objects;

import al.ahgitdevelopment.municion.FragmentMainActivity;
import al.ahgitdevelopment.municion.R;
import al.ahgitdevelopment.municion.Utils;
import al.ahgitdevelopment.municion.databases.DataBaseSQLiteHelper;
import al.ahgitdevelopment.municion.datamodel.Guia;

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
        layoutMarca = findViewById(R.id.form_marca);
        layoutModelo = findViewById(R.id.form_modelo);
        layoutApodo = findViewById(R.id.form_apodo_arma);
        layoutNumGuia = findViewById(R.id.text_input_layout_num_guia);
        layoutNumArma = findViewById(R.id.text_input_layout_num_arma);
        layoutCupo = findViewById(R.id.layout_cupo);
        layoutGastado = findViewById(R.id.text_input_layout_cartuchos_gastados);

        //Municion gastada por defecto = 0
        if (layoutGastado.getEditText().getText().toString().equals("")) {
            layoutGastado.getEditText().setText("0");
        }

        imagePath = null;

        //Carga de calibres
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(GuiaFormActivity.this,
                        android.R.layout.simple_dropdown_item_1line,
                        getResources().getStringArray(R.array.calibres));
        calibre1.setAdapter(adapter);
        calibre2.setAdapter(adapter);

        //Mostrar la lista de tipos de armas en funcion de la licencia
        tipoArmasDisponibles();

        //Inicializacion del cupo por defecto
        tipoArma.setSelection(0);
        layoutCupo.getEditText().setText(
                String.valueOf(getDefaultCupo(finalWeapons.get(tipoArma.getSelectedItemPosition()))
                )
        );

        //Carga de datos (en caso de modificacion)
        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().get("tipo_licencia") == null) {
                try {
                    Guia guia = getIntent().getExtras().getParcelable("modify_guia");
                    assert guia != null;
                    tipoLicencia = guia.getTipoLicencia();
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

        aumentoCupo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
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
        Objects.requireNonNull(layoutCalibre1.getEditText()).addTextChangedListener(new TextWatcher() {
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
        Objects.requireNonNull(layoutNumGuia.getEditText()).addTextChangedListener(new TextWatcher() {
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
        Objects.requireNonNull(layoutNumArma.getEditText()).addTextChangedListener(new TextWatcher() {
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
    }

    public void fabSaveOnClick(View view) {
        // Validación formulario
        if (!validateForm()) {
            return;
        }

        // Create intent to deliver some kind of result data
        Intent result = new Intent(this, FragmentMainActivity.class);
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
        // Crear objeto Guia con todos los campos del formulario
        bundle.putParcelable("modify_guia", getCurrentGuia());

        //Paso de vuelta de la posicion del item en el arrayet
        if (getIntent().getExtras() != null)
            bundle.putInt("position", getIntent().getExtras().getInt("position", -1));

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

    /**
     * Recoge todos los campos del formulario y crea un objeto Guia.
     * Similar al patrón usado en LicenciaFormActivity.getCurrenteLicense()
     *
     * @return Guia con los datos del formulario
     */
    private Guia getCurrentGuia() {
        Guia guia = new Guia();
        guia.setTipoLicencia(tipoLicencia);
        guia.setMarca(layoutMarca.getEditText().getText().toString());
        guia.setModelo(layoutModelo.getEditText().getText().toString());

        // Control error: campo en BBDD no puede ser nulo
        String apodo = layoutApodo.getEditText().getText().toString();
        guia.setApodo(apodo.isEmpty() ? "" : apodo);

        guia.setTipoArma(tipoArma.getSelectedItemPosition());
        guia.setCalibre1(layoutCalibre1.getEditText().getText().toString());

        // Segundo calibre solo si está marcado
        if (segundoCalibre.isChecked()) {
            String cal2 = calibre2.getText().toString();
            guia.setCalibre2(cal2.isEmpty() ? "" : cal2);
        } else {
            guia.setCalibre2("");
        }

        guia.setNumGuia(layoutNumGuia.getEditText().getText().toString().trim());
        guia.setNumArma(layoutNumArma.getEditText().getText().toString().trim());
        guia.setCupo(Integer.parseInt(layoutCupo.getEditText().getText().toString()));
        guia.setGastado(Integer.parseInt(layoutGastado.getEditText().getText().toString()));
        guia.setImagePath(imagePath);

        return guia;
    }
}
