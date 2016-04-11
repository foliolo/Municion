package al.ahgitdevelopment.municion;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginPasswordActivity extends AppCompatActivity {

    ActionBar actionBar;
    SharedPreferences prefs;
    EditText password;
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

        final SharedPreferences prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);

        password = (EditText) findViewById(R.id.password);
        button = (Button) findViewById(R.id.continuar);

        // Registro de contraseña
        if (!prefs.contains("password")) {
            password.setHint(getResources().getString(R.string.lbl_password));
            button.setText(R.string.guardar);
        } else {
            password.setHint(getResources().getString(R.string.lbl_insert_password));
            button.setText(R.string.login);
        }
        //Añadimos la contraseña a las preferencias
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckPassword(prefs);
            }
        });
        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_DONE:
                        CheckPassword(prefs);
                        break;
                    default:
                        Toast.makeText(LoginPasswordActivity.this, "IME erroneo", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
    }

    /**
     * Validación de la contraseña para poder entrar a la aplicación
     *
     * @param prefs Preferencias
     */
    private void CheckPassword(SharedPreferences prefs) {
        // Registro de usuario
        if (!prefs.contains("password")) {
            if (savePassword()) { // Guardamos la contraseña
                Toast.makeText(LoginPasswordActivity.this, "Contraseña guardada", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginPasswordActivity.this, FragmentMainActivity.class);
                startActivity(intent);
                finish();
            } else { // Fallo al guardar la contraseñas
                password.setText("");
                password.setError(getResources().getString(R.string.password_save_fail));
            }
        }
        // Login de usuario
        else {
            if (!checkPassword()) { // Password erronea
                password.setText("");
                password.setError(getResources().getString(R.string.password_fail));
            } else { // Password correcta
                Toast.makeText(LoginPasswordActivity.this, "Login Correcto", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginPasswordActivity.this, FragmentMainActivity.class);
                startActivity(intent);
                finish();
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

        if (password.getText().toString().length() > 4) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("password", password.getText().toString());
            editor.commit();
            flag = true;
        } else
            flag = false;

        return flag;
    }

    /**
     * Valida la contraseña introducida por el usuario frente a la guardad en el sharedPreferences
     *
     * @return Contraseña valida o invalida
     */
    private boolean checkPassword() {
        if (prefs == null)
            prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);

        String pass = prefs.getString("password", "");

        return pass.equals(password.getText().toString());
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
}
