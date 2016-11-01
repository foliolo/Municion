package al.ahgitdevelopment.municion;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;

import al.ahgitdevelopment.municion.DataBases.DataBaseSQLiteHelper;

import static al.ahgitdevelopment.municion.DataBases.FirebaseDBHelper.accountPermission;

public class LoginPasswordActivity extends AppCompatActivity {
    public static final int MIN_PASS_LENGTH = 6;
    public Toolbar toolbar;
    private FirebaseAnalytics mFirebaseAnalytics;

    private SharedPreferences prefs;
    private TextInputLayout textInputLayout1;
    private TextInputEditText password1;
    private TextInputLayout textInputLayout2;
    private TextInputEditText password2;
    private ImageView button;
    private TextView versionLabel;

    /**
     * Inicializa la actividad
     *
     * @param savedInstanceState Instancia del estado de la activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final SharedPreferences prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);

        // Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setSubtitle(R.string.login);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_bullseye);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.VALUE, "Inicio de aplicacion");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle);

        //Instances of UI objects
        textInputLayout1 = (TextInputLayout) findViewById(R.id.text_input_layout1);
        password1 = (TextInputEditText) findViewById(R.id.password1);
        textInputLayout2 = (TextInputLayout) findViewById(R.id.text_input_layout2);
        password2 = (TextInputEditText) findViewById(R.id.password2);
        button = (ImageView) findViewById(R.id.continuar);
        versionLabel = (TextView) findViewById(R.id.login_version_label);

        versionLabel.setText(Utils.getAppVersion(this));

        // Registro de contraseña
        if (!prefs.contains("password")) {
            textInputLayout2.setVisibility(View.VISIBLE);
        } else {
            textInputLayout2.setVisibility(View.GONE);
        }

        //Añadimos la contraseña a las preferencias
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                evaluatePassword(prefs);
            }
        });
        password1.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
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
            }
        });

        password1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String pass = prefs.getString("password", "");
                if (!pass.equals("") && !password1.getText().toString().equals(pass) && textInputLayout1.getError() != null) {
                    textInputLayout1.setError(getString(R.string.password_equal_fail));
                }

                if (password1.getText().toString().equals(pass) && password1.getText().toString().length() >= MIN_PASS_LENGTH)
                    textInputLayout1.setError(null);
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
                if (s.toString().equals(password1.getText().toString())) {
                    textInputLayout2.setError(null);
                } else {
                    textInputLayout2.setError(getString(R.string.password_equal_fail));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        AdView mAdView = (AdView) findViewById(R.id.adView);
        mAdView.loadAd(Utils.getAdRequest(mAdView));
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkAccountPermission();

        //Lanza el tutorial la primera vez
        showTotorial();
    }

    private void checkAccountPermission() {
        int accountPermission = PackageManager.PERMISSION_GRANTED;
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

        if (grantResults != null && grantResults.length > 0) {
            if (requestCode == 100) {
                accountPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
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
        if (!prefs.contains("password")) {
            if (savePassword()) { // Guardamos la contraseña
                Toast.makeText(LoginPasswordActivity.this, R.string.password_save, Toast.LENGTH_SHORT).show();
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

    private void launchActivity() {
        DataBaseSQLiteHelper dbSqlHelper = new DataBaseSQLiteHelper(getApplicationContext());

        //Lanzamiento del Intent
        Intent intent = new Intent(LoginPasswordActivity.this, FragmentMainActivity.class);
        intent.putParcelableArrayListExtra("guias", dbSqlHelper.getListGuias(null));
        intent.putParcelableArrayListExtra("compras", dbSqlHelper.getListCompras(null));
        intent.putParcelableArrayListExtra("licencias", dbSqlHelper.getListLicencias(null));

        startActivity(intent);
        dbSqlHelper.close();

        // Registrar Login - Analytics
        String android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SIGN_UP_METHOD, android_id);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
    }

    /**
     * Lanza el tutorial la primera vez que se inicia la aplicación.
     */
    private void showTotorial() {
        if (prefs == null)
            prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);

        if (prefs.getBoolean("show_tutorial", true)) {
            Intent intent = new Intent(this, FragmentTutorialActivity.class);
            startActivity(intent);
        }
    }
}
