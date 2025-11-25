package al.ahgitdevelopment.municion;

import static al.ahgitdevelopment.municion.Utils.getStringLicenseFromId;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Calendar;
import java.util.List;

import al.ahgitdevelopment.municion.databases.DataBaseSQLiteHelper;
import al.ahgitdevelopment.municion.datamodel.Guia;


public class LoginPasswordActivity extends AppCompatActivity {

    public static final int MIN_PASS_LENGTH = 6;
    private final String TAG = "LoginPasswordActivity";
    public Toolbar toolbar;
    // Provides purchase notification while this app is running
    private FirebaseAnalytics mFirebaseAnalytics;
    private SharedPreferences prefs;
    private TextInputLayout textInputLayout1;
    private TextInputEditText password1;
    private TextInputLayout textInputLayout2;
    private TextInputEditText password2;
    private ImageView button;
    private TextView versionLabel;
    private boolean isPurchaseAvailable;

    /**
     * Inicializa la actividad
     *
     * @param savedInstanceState Instancia del estado de la activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);

        //Gestion de mensajes de firebase en el intent de entrada
        // [START handle_data_extras]
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(getLocalClassName(), "Key: " + key + " Value: " + value);
            }
        }
        // [END handle_data_extras]

        // Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_bullseye);
        toolbar.setTitle(R.string.app_name);
        toolbar.setSubtitle(R.string.login);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.VALUE, "Inicio de aplicacion");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle);

        //Instances of UI objects
        textInputLayout1 = findViewById(R.id.text_input_layout1);
        password1 = findViewById(R.id.password1);
        textInputLayout2 = findViewById(R.id.text_input_layout2);
        password2 = findViewById(R.id.password2);
        button = findViewById(R.id.continuar);
        versionLabel = findViewById(R.id.login_version_label);

        versionLabel.setText(Utils.getAppVersion(this));

        // Registro de contraseña
        if (!prefs.contains("password") || prefs.getString("password", "").equals("")) {
            textInputLayout2.setVisibility(View.VISIBLE);
        } else {
            textInputLayout2.setVisibility(View.GONE);
        }

        //Añadimos la contraseña a las preferencias
        button.setOnClickListener(v -> evaluatePassword(prefs));
        password1.setOnEditorActionListener((v, actionId, event) -> {
            switch (actionId) {
                case EditorInfo.IME_ACTION_NEXT:
                    evaluatePassword(prefs);
                    break;
                case EditorInfo.IME_ACTION_DONE:
                    evaluatePassword(prefs);
                    break;
                default:
                    Toast.makeText(LoginPasswordActivity.this, "IME erroneo", Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        password1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    String pass = prefs.getString("password", "");
                    if (!pass.equals("") && !password1.getText().toString().equals(pass) && textInputLayout1.getError() != null) {
                        textInputLayout1.setError(getString(R.string.password_equal_fail));
                    }

                    if (password1.getText().toString().equals(pass) && password1.getText().toString().length() >= MIN_PASS_LENGTH)
                        textInputLayout1.setError(null);
                } catch (Exception ex) {
                    Log.e(TAG, "Error en el onTextChange por la version de android");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        password2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    if (s.toString().equals(password1.getText().toString())) {
                        textInputLayout2.setError(null);
                    } else {
                        textInputLayout2.setError(getString(R.string.password_equal_fail));
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "Error en el onTextChange por la version de android");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    protected void onDestroy() {
        // Gestión del año actual para actualizar los cupos y las compras
        Calendar calendar = Calendar.getInstance();
        int yearPref = calendar.get(Calendar.YEAR);

        prefs.edit().putInt("year", yearPref).apply();

        super.onDestroy();
    }

    private void checkAccountPermission() {
        int accountPermission;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            accountPermission = checkSelfPermission(Manifest.permission.GET_ACCOUNTS);
            if (accountPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{
                                android.Manifest.permission.GET_ACCOUNTS,
                        },
                        100 //Codigo de respuesta de
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0) {
            if (requestCode == 100) {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    checkAccountPermission();
                } else {
                    Log.w(TAG, "Permisos de correo no concedidos");
                }
            }
        }
        //Lanza el tutorial la primera vez
        showTutorial();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(LoginPasswordActivity.this, SettingsFragment.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Validación de la contraseña para poder entrar a la aplicación
     *
     * @param prefs Preferencias
     */
    private void evaluatePassword(SharedPreferences prefs) {
        // Registro de usuario
        if (!prefs.contains("password") || prefs.getString("password", "").equals("")) {
            if (savePassword()) { // Guardamos la contraseña
                Toast.makeText(LoginPasswordActivity.this, R.string.password_save, Toast.LENGTH_LONG).show();
                launchActivity();
                finish();
            } else { // Fallo al guardar la contraseñas
                if (textInputLayout1.getError() == null)
                    textInputLayout1.setError(getString(R.string.password_save_fail));
            }
        }
        // Login de usuario
        else {
            if (checkPassword()) { // Password correcta
//                Toast.makeText(LoginPasswordActivity.this, R.string.login_ok, Toast.LENGTH_SHORT).show();
                launchActivity();
                finish();
            } else { // Password incorrecta
                textInputLayout1.setError(getString(R.string.password_fail));
                password1.setText("");
            }
        }
    }

    /**
     * Guarda la contraseña en el sharedPreference
     *
     * @return Contraseña valida o no
     */
    private boolean savePassword() {
        boolean flag = false;

        if (prefs == null)
            prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);

        if (password1.getText().toString().length() >= MIN_PASS_LENGTH) {
            if (password1.getText().toString().equals(password2.getText().toString())) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("password", password1.getText().toString());
                editor.apply();
                flag = true;
            } else {
                textInputLayout2.setError(getString(R.string.password_equal_fail));
            }
        } else {
            flag = false;
            textInputLayout1.setError(getString(R.string.password_short_fail));
        }

        return flag;
    }

    /**
     * Valida la contraseña introducida por el usuario frente a la guardada en el sharedPreferences
     *
     * @return Contraseña valida o invalida
     */
    private boolean checkPassword() {
        boolean isPassCorrect = false;

        if (prefs == null)
            prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);

        String pass = prefs.getString("password", "");
        if (pass.equals(password1.getText().toString())) {
            isPassCorrect = true;
        } else {
            textInputLayout2.setError(getString(R.string.password_equal_fail));
        }

        return isPassCorrect;
    }

    //    @AddTrace(name = "launchActivity", enabled = true/*Optional*/)
    private void launchActivity() {
        DataBaseSQLiteHelper dbSqlHelper = new DataBaseSQLiteHelper(getApplicationContext());

        //Lanzamiento del Intent
        Intent intent = new Intent(LoginPasswordActivity.this, FragmentMainActivity.class);
        intent.putParcelableArrayListExtra("guias", dbSqlHelper.getListGuias(null));
        intent.putParcelableArrayListExtra("compras", dbSqlHelper.getListCompras(null));
        intent.putParcelableArrayListExtra("licencias", dbSqlHelper.getListLicencias(null));
        intent.putParcelableArrayListExtra("tiradas", dbSqlHelper.getListTiradas(null));

        checkYearCupo(intent);

        startActivity(intent);
        dbSqlHelper.close();

        // Registrar Login - Analytics
        String android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.VALUE, android_id);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
    }

    private void checkYearCupo(Intent intent) {
        // Comprobar year para renovar los cupos
        int yearPref = prefs.getInt("year", 0);
        int year = Calendar.getInstance().get(Calendar.YEAR); // Year actual

        if (yearPref != 0 && year > yearPref) {
            List<Guia> listaGuias = intent.getParcelableArrayListExtra("guias");
            if (listaGuias.size() > 0) {
                for (Guia guia : listaGuias) {
                    String nombreLicencia = getStringLicenseFromId(LoginPasswordActivity.this, guia.getTipoLicencia());
                    String idNombreLicencia = nombreLicencia.split(" ")[0];
                    int tipoArma = guia.getTipoArma();

                    switch (idNombreLicencia) {
                        case "A":
                        case "Libro":
                            switch (tipoArma) {
                                case 0: // Pistola
                                case 3: // Revolver
                                    guia.setCupo(100);
                                    break;
                                case 1: // Escopeta
                                    guia.setCupo(5000);
                                    break;
                                case 2: // Rifle
                                case 4: // Avancarga
                                    guia.setCupo(1000);
                                    break;
                            }
                            break;
                        case "B":
                            switch (tipoArma) {
                                case 0: // Pistola
                                case 1: // Revolver
                                    guia.setCupo(100);
                                    break;
                            }
                            break;
                        case "C":
                            guia.setCupo(100);
                            break;
                        case "D":
                            guia.setCupo(1000);
                            break;
                        case "E":
                            switch (tipoArma) {
                                case 0: // Escopeta
                                    guia.setCupo(5000);
                                    break;
                                case 1: // Rifle
                                    guia.setCupo(1000);
                                    break;
                            }
                            break;
                        case "F":
                        case "Federativa":
                            switch (tipoArma) {
                                case 0: // Pistola
                                case 3: // Revolver
                                    guia.setCupo(100);
                                    break;
                                case 1: // Escopeta
                                    guia.setCupo(5000);
                                    break;
                                case 2: // Rifle
                                    guia.setCupo(1000);
                                    break;
                            }
                            break;
                        case "AE":
                            guia.setCupo(1000);
                            break;
                        case "AER":
                            switch (tipoArma) {
                                case 0: // Pistola
                                case 2: // Revolver
                                    guia.setCupo(100);
                                case 1: // Rifle
                                    guia.setCupo(1000);
                                    break;
                            }
                            break;
                    }
                }
            }
        }
    }

    /**
     * Lanza el tutorial la primera vez que se inicia la aplicación.
     */
    private void showTutorial() {
        // Para que no se muestre el tutorial cuando se ha reseteado el password
        boolean isTutorial = true;
        if (getIntent().hasExtra("tutorial"))
            isTutorial = getIntent().getBooleanExtra("tutorial", true);

        if (prefs == null)
            prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);

        if (prefs.getBoolean("show_tutorial", true) && isTutorial) {
            Intent intent = new Intent(this, FragmentTutorialActivity.class);
            startActivity(intent);
        }
    }
}
