package al.ahgitdevelopment.municion;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by ahidalgog on 06/07/2016.
 */
public class ChangePasswordDialog extends DialogFragment {
    private SharedPreferences preferences;
    private TextInputEditText passwordOld;
    private TextInputEditText passwordNew1;
    private TextInputEditText passwordNew2;
    private boolean saveStatus = false;

    @Nullable
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        preferences = this.getActivity().getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.settings_items, null);

        passwordOld = (TextInputEditText) view.findViewById(R.id.passwordOld);
        passwordNew1 = (TextInputEditText) view.findViewById(R.id.passwordNew);
        passwordNew2 = (TextInputEditText) view.findViewById(R.id.passwordNew2);

        // Set custom view
        builder.setView(view)
                .setTitle(R.string.pref_password_old)
                // Add action buttons
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        saveStatus = savePassword();
                        if (savePassword()) {
                            Toast.makeText(getActivity(), R.string.password_save, Toast.LENGTH_SHORT).show();
                            dismiss();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ChangePasswordDialog.this.getDialog().cancel();
                    }
                });

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();    //super.onStart() is where dialog.show() is actually called on the underlying dialog, so we have to do it after this point
        final AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Do stuff, possibly set wantToCloseDialog to true then...
                    if (savePassword()) {
                        Toast.makeText(getActivity(), R.string.password_save, Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                    //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                }
            });
        }
    }

//    @Override
//    public void onDismiss(DialogInterface dialog) {
//        if(saveStatus)
//            super.onDismiss(dialog);
//    }

    /**
     * Guarda el password introducido por el usuario para cambiar la contraseña
     *
     * @return flag
     */
    private boolean savePassword() {
        boolean flag = false;
        if (passwordOld.getText() != null && passwordOld.getText().toString().length() >= 4) {
            if (checkPasswordOld() && checkPasswordNew()) {
                // Ha ido correcto
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("password", passwordNew1.getText().toString());
                editor.apply();
                flag = true;
            }
        } else {
            passwordOld.setError(getString(R.string.password_short_fail));
        }
        return flag;
    }

    /**
     * Valida la contraseña introducida por el usuario frente a la guardada en el sharedPreferences
     *
     * @return password valido o invalido
     */
    private boolean checkPasswordOld() {
        boolean isPassCorrect = false;
        String pass = preferences.getString("password", "");
        if ("".equals(pass)) {
            passwordOld.setError(getString(R.string.settings_password_unlogin));
        } else if (pass.equals(passwordOld.getText().toString())) {
            isPassCorrect = true;
        } else {
            passwordOld.setError(getString(R.string.password_equal_fail));
        }
        return isPassCorrect;
    }

    /**
     * Valida que la contraseña nueva sea correcta, se introduce en dos campos
     *
     * @return password valido o invalido
     */
    private boolean checkPasswordNew() {
        boolean isPassCorrect = false;
        if ("".equals(passwordNew1.getText().toString())) {
            passwordNew1.setError(getString(R.string.settings_password_empty));
        } else if ("".equals(passwordNew2.getText().toString())) {
            passwordNew2.setError(getString(R.string.settings_password_empty));
        } else if (passwordNew1.getText().toString().length() < 4) {
            passwordNew1.setError(getString(R.string.password_short_fail));
        } else if (passwordNew2.getText().toString().length() < 4) {
            passwordNew2.setError(getString(R.string.password_short_fail));
        } else if (!passwordNew1.getText().toString().equals(passwordNew2.getText().toString())) {
            passwordNew2.setError(getString(R.string.password_equal_fail));
        } else if (passwordNew1.getText().toString().equals(passwordNew2.getText().toString())) {
            isPassCorrect = true;
        }
        return isPassCorrect;
    }
}
