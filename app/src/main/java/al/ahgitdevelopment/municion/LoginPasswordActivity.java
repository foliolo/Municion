package al.ahgitdevelopment.municion;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class LoginPasswordActivity extends AppCompatActivity {

    ActionBar actionBar;
    SharedPreferences prefs;

    TextInputLayout textInputLayout1;
    TextInputEditText password1;
    TextInputLayout textInputLayout2;
    TextInputEditText password2;
    Button button;

    /**
     * Inicializa la actividad
     *
     * @param savedInstanceState Instancia del estado de la activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher_2);

        final SharedPreferences prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);

        textInputLayout1 = (TextInputLayout) findViewById(R.id.text_input_layout1);
        password1 = (TextInputEditText) findViewById(R.id.password1);
        textInputLayout2 = (TextInputLayout) findViewById(R.id.text_input_layout2);
        password2 = (TextInputEditText) findViewById(R.id.password2);
        button = (Button) findViewById(R.id.continuar);

        // Registro de contraseña
        if (!prefs.contains("password")) {
            textInputLayout2.setVisibility(View.VISIBLE);
            button.setText(R.string.guardar);
        } else {
            textInputLayout2.setVisibility(View.GONE);
            button.setText(R.string.login);
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
                if (!pass.equals("") && !password1.getText().toString().equals(pass) && textInputLayout1.getError() != null)
                    textInputLayout1.setError(getString(R.string.password_equal_fail));

                if (password1.getText().toString().equals(pass) && password1.getText().toString().length() >= 4)
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
                Intent intent = new Intent(LoginPasswordActivity.this, SettingsActivity.class);
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
            if (checkPassword()) { // Password erronea
                Toast.makeText(LoginPasswordActivity.this, R.string.login_ok, Toast.LENGTH_SHORT).show();
                launchActivity();
                finish();
            } else { // Password incorrecta
                textInputLayout1.setError(getString(R.string.password_fail));
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

        if (password1.getText().toString().length() >= 4) {
            if (password1.getText().toString().equals(password2.getText().toString())) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("password", password1.getText().toString());
                editor.commit();
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

        // Inicialización de datos fake
        if (dbSqlHelper.getCursorGuias().getCount() == 0) {
            dbSqlHelper.addCompras();
            dbSqlHelper.addLicencias();
            dbSqlHelper.addGuias();
        }

        //Lanzamiento del Intent
        Intent intent = new Intent(LoginPasswordActivity.this, FragmentMainActivity.class);
        intent.putParcelableArrayListExtra("guias", dbSqlHelper.getListGuias());
        intent.putParcelableArrayListExtra("compras", dbSqlHelper.getListCompras());
        intent.putParcelableArrayListExtra("licencias", dbSqlHelper.getListLicencias());

        startActivity(intent);
        dbSqlHelper.close();
    }
}
