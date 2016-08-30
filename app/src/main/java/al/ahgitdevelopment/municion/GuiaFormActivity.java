package al.ahgitdevelopment.municion;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import al.ahgitdevelopment.municion.DataModel.Guia;

/**
 * Created by Alberto on 25/03/2016.
 */
public class GuiaFormActivity extends AppCompatActivity {
    private ArrayList<String> finalWeapons = new ArrayList<>();
    private int tipoLicencia;
    private EditText marca;
    private EditText modelo;
    private EditText apodo;
    private AppCompatSpinner tipoArma;
    private AutoCompleteTextView calibre1;
    private CheckBox segundoCalibre;
    private AutoCompleteTextView calibre2;
    private EditText numGuia;
    private EditText numArma;
    private CheckBox aumentoCupo;
    private EditText cupo;
    private EditText gastado;
    // Mensaje de error antes de guardar
    private TextView mensajeError;
    private String imagePath;
    private DataBaseSQLiteHelper dbSqlHelper;

    /**
     * Inicializa la actividad
     *
     * @param savedInstanceState Instancia del estado de la activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_guia);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher_4_transparent);

        marca = (EditText) findViewById(R.id.form_marca);
        modelo = (EditText) findViewById(R.id.form_modelo);
        apodo = (EditText) findViewById(R.id.form_apodo_arma);
        tipoArma = (AppCompatSpinner) findViewById(R.id.form_tipo_arma);
        calibre1 = (AutoCompleteTextView) findViewById(R.id.form_calibre1);
        segundoCalibre = (CheckBox) findViewById(R.id.form_check_segundo_calibre);
        calibre2 = (AutoCompleteTextView) findViewById(R.id.form_calibre2);
        numGuia = (EditText) findViewById(R.id.form_num_guia);
        numArma = (EditText) findViewById(R.id.form_num_arma);
        aumentoCupo = (CheckBox) findViewById(R.id.form_check_aumento_cupo);
        cupo = (EditText) findViewById(R.id.form_cupo_anual);
        gastado = (EditText) findViewById(R.id.form_cartuchos_gastados);
        mensajeError = (TextView) findViewById(R.id.form_mensaje_guia);
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
        cupo.setText(String.valueOf(getDefaultCupo(finalWeapons.get(tipoArma.getSelectedItemPosition()))));

        //Carga de datos (en caso de modificacion)
        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().get("tipo_licencia") == null) {
                try {
                    Guia guia = getIntent().getExtras().getParcelable("modify_guia");
                    assert guia != null;
                    tipoLicencia = guia.getTipoLicencia();
                    marca.setText(guia.getMarca());
                    modelo.setText(guia.getModelo());
                    apodo.setText(guia.getApodo());
                    calibre1.setText(guia.getCalibre1());
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
                    numGuia.setText(String.valueOf(guia.getNumGuia()));
                    numArma.setText(String.valueOf(guia.getNumArma()));
                    gastado.setText(String.valueOf(guia.getGastado()));
                    cupo.setText(String.valueOf(guia.getCupo()));
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
                    cupo.setInputType(InputType.TYPE_CLASS_NUMBER);
                } else {
                    cupo.setInputType(InputType.TYPE_NULL);
                }
            }
        });

        tipoArma.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                cupo.setText(String.valueOf(getDefaultCupo(finalWeapons.get(tipoArma.getSelectedItemPosition()))));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // Validaciones de campos obligatorios antes de guardar
        // Marca
        marca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (marca.getText().toString().length() < 1) {
                    marca.setError(getString(R.string.error_before_save));
                }
            }
        });
        // Modelo
        modelo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (modelo.getText().toString().length() < 1) {
                    modelo.setError(getString(R.string.error_before_save));
                }
            }
        });
        // Calibre1
        calibre1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (calibre1.getText().toString().length() < 1) {
                    calibre1.setError(getString(R.string.error_before_save));
                }
            }
        });
        // Num Guia
        numGuia.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (numGuia.getText().toString().length() < 1) {
                    numGuia.setError(getString(R.string.error_before_save));
                }
            }
        });
        // Num arma
        numArma.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (numArma.getText().toString().length() < 1) {
                    numArma.setError(getString(R.string.error_before_save));
                }
            }
        });
        // Cupo
        cupo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (cupo.getText().toString().length() < 1) {
                    cupo.setError(getString(R.string.error_before_save));
                }
            }
        });
        // Gastado
        gastado.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (gastado.getText().toString().length() < 1) {
                    gastado.setError(getString(R.string.error_before_save));
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
        int id = item.getItemId();

        if (id == R.id.action_save) {
            // Validación formulario
            if (!validateForm()) {
                return false;
            }

            // Create intent to deliver some kind of result data
            Intent result = new Intent(this, FragmentMainActivity.class);

            Bundle bundle = new Bundle();
            if (getIntent().getExtras().getString("tipo_licencia") != null) {
                tipoLicencia = Utils.getLicenciaTipoFromString(GuiaFormActivity.this, getIntent().getExtras().getString("tipo_licencia"));
                if (tipoLicencia == 4) {  // E - Escopeta
                    if(checkMaxGuiasForLicenciaTipoE(marca))
                        return false;
                } else if(tipoLicencia == 5) { // F - Tiro olimpico
                    // TODO Queda contar el numero de guias guardadas en BBDD para la maxCategoria e ir actualizando el limite cada vez que se guarda una
                    // TODO En funcion de la categoria, el nº de guias asocias a F
                    int maxCategoria = Utils.getMaxCategoria(GuiaFormActivity.this);
                }
            } else {
                tipoLicencia = ((Guia) getIntent().getExtras().getParcelable("modify_guia")).getTipoLicencia();
                if (tipoLicencia == 4) {  // E - Escopeta
                    if(checkMaxGuiasForLicenciaTipoE(marca)) {
                        return false;
                    }
                }else if(tipoLicencia == 5) { // F - Tiro olimpico
                    // TODO Queda contar el numero de guias guardadas en BBDD para la maxCategoria e ir actualizando el limite cada vez que se guarda una
                    // TODO En funcion de la categoria, el nº de guias asocias a F
                    int maxCategoria = Utils.getMaxCategoria(GuiaFormActivity.this);
                }
            }
            bundle.putInt("tipoLicencia", tipoLicencia);
            bundle.putString("marca", marca.getText().toString());
            bundle.putString("modelo", modelo.getText().toString());
            // Control error. He metido vacio porque el campo en BBDD no puede ser  nulo
            if (apodo.getText().toString().isEmpty()) {
                apodo.setText("");
            }
            bundle.putString("apodo", apodo.getText().toString());
            bundle.putInt("tipoArma", tipoArma.getSelectedItemPosition());
            bundle.putString("calibre1", calibre1.getText().toString());
            if (segundoCalibre.isChecked()) {
                // Control error. He metido vacio porque el campo en BBDD no puede ser  nulo
                if (calibre2.getText().toString().isEmpty()) {
                    calibre2.setText("");
                }
                bundle.putString("calibre2", calibre2.getText().toString());
            }
            bundle.putInt("numGuia", Integer.parseInt(numGuia.getText().toString()));
            bundle.putInt("numArma", Integer.parseInt(numArma.getText().toString()));
            bundle.putInt("cupo", Integer.parseInt(cupo.getText().toString()));
            bundle.putInt("gastado", Integer.parseInt(gastado.getText().toString()));

            //Paso de vuelta de la posicion del item en el array
            if (getIntent().getExtras() != null)
                bundle.putInt("position", getIntent().getExtras().getInt("position", -1));

            bundle.putString("imagePath", imagePath);

            result.putExtras(bundle);

            setResult(Activity.RESULT_OK, result);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean checkMaxGuiasForLicenciaTipoE(View view) {
        dbSqlHelper = new DataBaseSQLiteHelper(getApplicationContext());
        if(dbSqlHelper.getNumLicenciasTipoE() >= 6) {
            Snackbar.make(view, R.string.dialog_guia_licencia_tipoE, Snackbar.LENGTH_LONG)
                    .setAction(android.R.string.ok, null)
                    .show();
            return true;
        }
        return false;
    }
    // TODO Queda contar el numero de guias guardadas en BBDD para la maxCategoria e ir actualizando el limite cada vez que se guarda una
//    private boolean checkMaxGuiasCategoria(View view) {
//        dbSqlHelper = new DataBaseSQLiteHelper(getApplicationContext());
        // 1ª Categoria
//        if(dbSqlHelper.getLicenciasFederativas() >= 1) {
//            Snackbar.make(view, R.string.dialog_guia_licencia_federativa_categoria3, Snackbar.LENGTH_LONG)
//                    .setAction(android.R.string.ok, null)
//                    .show();
//            return true;
//        }
//        // 2ª Categoria
//        else if(dbSqlHelper.getLicenciasFederativas() >= 6) {
//            Snackbar.make(view, R.string.dialog_guia_licencia_federativa_categoria2, Snackbar.LENGTH_LONG)
//                    .setAction(android.R.string.ok, null)
//                    .show();
//            return true;
//        }
        // 3ª Categoria
//        else if(dbSqlHelper.getGuiasCategoria1() >= 10) {
//            Snackbar.make(view, R.string.dialog_guia_licencia_federativa_categoria1, Snackbar.LENGTH_LONG)
//                    .setAction(android.R.string.ok, null)
//                    .show();
//            return true;
//        }
//        ArrayList<Guia> lista = dbSqlHelper.getListGuiasCategorias();
//        return false;
//    }

    private boolean validateForm() {
        boolean retorno = true;
        // Validaciones campos formularios
        // Marca
        if (marca.getText().toString().length() < 1) {
            marca.setError(getString(R.string.error_before_save));
            retorno = false;
        }
        // Modelo
        if (modelo.getText().toString().length() < 1) {
            modelo.setError(getString(R.string.error_before_save));
            retorno = false;
        }
        // Apodo
        if (apodo.getText().toString().length() < 1) {
            apodo.setError(getString(R.string.error_before_save));
            retorno = false;
        }
        // Calibre1
        if (calibre1.getText().toString().length() < 1) {
            calibre1.setError(getString(R.string.error_before_save));
            retorno = false;
        }
        // Num Guia
        if (numGuia.getText().toString().length() < 1) {
            numGuia.setError(getString(R.string.error_before_save));
            retorno = false;
        }
        // Num Arma
        if (numArma.getText().toString().length() < 1) {
            numArma.setError(getString(R.string.error_before_save));
            retorno = false;
        }
        // Cupo
        if (cupo.getText().toString().length() < 1) {
            cupo.setError(getString(R.string.error_before_save));
            retorno = false;
        }
        // Gastado
        if (gastado.getText().toString().length() < 1) {
            gastado.setError(getString(R.string.error_before_save));
            retorno = false;
        }
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
                    defaultCupo = 100;
                    cupo.setInputType(InputType.TYPE_NULL);
                    break;
                case "Escopeta":
                    defaultCupo = 5000;
                    cupo.setInputType(InputType.TYPE_NULL);
                    break;
                case "Rifle":
                    defaultCupo = 1000;
                    cupo.setInputType(InputType.TYPE_NULL);
                    break;
                case "Revolver":
                    defaultCupo = 100;
                    cupo.setInputType(InputType.TYPE_NULL);
                    break;
                case "Avancarga":
                    defaultCupo = 1000;
                    cupo.setInputType(InputType.TYPE_NULL);
                    break;
                default:
                    defaultCupo = 0;
                    aumentoCupo.setChecked(true);
                    cupo.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
        } else {
            defaultCupo = Integer.parseInt(cupo.getText().toString());
            cupo.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
        return defaultCupo;
    }
}
