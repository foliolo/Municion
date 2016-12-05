package al.ahgitdevelopment.municion;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import al.ahgitdevelopment.municion.Dialogs.ChangePasswordDialog;
import al.ahgitdevelopment.municion.Dialogs.ResetPasswordDialog;
import al.ahgitdevelopment.municion.Dialogs.SecurityQuestionDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends FragmentActivity {

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_settings);

        ((TextView) findViewById(R.id.version_text)).setText(Utils.getAppVersion(this));
    }

    public void changePassword(View view) {
        DialogFragment dialog = new ChangePasswordDialog();
        dialog.show(getFragmentManager(), "ChangePasswordDialog");
    }

    public void showTutorial(View view) {
        Intent intent = new Intent(this, FragmentTutorialActivity.class);
        startActivity(intent);
    }

    public void securityQuestion(View view) {
        DialogFragment dialog = new SecurityQuestionDialog();
        dialog.show(getFragmentManager(), "SecurityQuestionDialog");
    }

    public void resetPassword(View view) {
        DialogFragment dialog = new ResetPasswordDialog();
        dialog.show(getFragmentManager(), "ResetPasswordDialog");
    }

    /**
     * Borrar es solo para pruebas
     *
     * @param view
     */
    public void changeYear(View view) {
        DialogFragment dialog = new ChangeYearDialog();
        dialog.show(getFragmentManager(), "ResetPasswordDialog");
    }

    public static class ChangeYearDialog extends DialogFragment {

        private SharedPreferences preferences;
        private EditText year;

        @Nullable
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            preferences = this.getActivity().getSharedPreferences("Preferences", Context.MODE_PRIVATE);
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            //Layout programmatically
            year = new EditText(getActivity());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            year.setLayoutParams(lp);
            year.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED);
            InputFilter[] maxLenght = new InputFilter[1];
            maxLenght[0] = new InputFilter.LengthFilter(4); //4 digitos para el año
            year.setFilters(maxLenght);

            String hint = "Actual: " + preferences.getInt("year", 0);
            year.setHint(hint);

            // Set custom view
            builder.setView(year)
                    .setTitle("Cambiar año")
                    // Add action buttons
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            SharedPreferences.Editor editor = preferences.edit();
                            if (preferences.contains("year")) {
                                int yearPref = Integer.parseInt(year.getText().toString());
                                editor.putInt("year", yearPref);
                                editor.apply();
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null);

            return builder.create();
        }
    }
}
