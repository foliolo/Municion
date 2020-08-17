package al.ahgitdevelopment.municion.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatSpinner;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import al.ahgitdevelopment.municion.R;
import al.ahgitdevelopment.municion.sandbox.Utils;

/**
 * Created by david.sierra on 07/11/2016.
 */

public class SecurityQuestionDialog extends DialogFragment {

    public static final int MIN_ANSWER_LENGTH = 4;
    private SharedPreferences preferences;
    private TextInputLayout layoutPassword;
    private TextInputEditText password;
    private TextInputLayout layoutQuestion;
    private AppCompatSpinner questionType;
    private TextInputLayout layoutAnswer;
    private TextInputEditText answer;
    private TextInputLayout layoutConfirmAnswer;
    private TextInputEditText confirmAnswer;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        preferences = this.getActivity().getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.security_question_items, null);

        layoutPassword = view.findViewById(R.id.text_input_layout_password);
        password = view.findViewById(R.id.passwordPref);
        layoutQuestion = view.findViewById(R.id.text_input_layout_question_reset);
        questionType = view.findViewById(R.id.form_question);
        layoutAnswer = view.findViewById(R.id.text_input_layout_answer);
        answer = view.findViewById(R.id.answer);
        layoutConfirmAnswer = view.findViewById(R.id.text_input_layout_confirm_answer);
        confirmAnswer = view.findViewById(R.id.confirmAnswer);

        // Set custom view
        builder.setView(view).setTitle(R.string.lbl_question_type)
                // Add action buttons
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (checkPasswordOld() && saveAnswerSecurity()) {
                            Toast.makeText(getActivity(), R.string.answer_save, Toast.LENGTH_SHORT).show();
                            dismiss();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SecurityQuestionDialog.this.getDialog().cancel();
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
                    if (checkPasswordOld() && saveAnswerSecurity()) {
                        Toast.makeText(getActivity(), R.string.answer_save, Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                    //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                }
            });
        }
    }

    /**
     * Guarda la respuesta del usuario a la pregunta de seguridad
     *
     * @return flag
     */
    private boolean saveAnswerSecurity() {
        boolean flag = false;
        if (answer.getText() != null && answer.getText().toString().length() >= MIN_ANSWER_LENGTH) {
            if (checkAnswerSecurity()) {
                // Ha ido correcto
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("question", Utils.getStringTipoPregunta(SecurityQuestionDialog.this,
                        questionType.getSelectedItemPosition()));
                editor.putString("answer", confirmAnswer.getText().toString());
                editor.apply();
                flag = true;
                layoutQuestion.setError(null);
            }
        } else {
            layoutQuestion.setError(getString(R.string.settings_answer_fail));
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
            layoutPassword.setError(getString(R.string.settings_password_unlogin));
        } else if (pass.equals(password.getText().toString())) {
            isPassCorrect = true;
            layoutPassword.setError(null);
        } else {
            layoutPassword.setError(getString(R.string.login_not_matching_password_error));
        }
        return isPassCorrect;
    }

    /**
     * Valida que la respuesta sea correcta, se introduce en dos campos
     *
     * @return answer valida o invalida
     */
    private boolean checkAnswerSecurity() {
        boolean isAnswerCorrect = false;
        if ("".equals(answer.getText().toString())) {
            layoutAnswer.setError(getString(R.string.settings_answer_empty));
        } else if ("".equals(confirmAnswer.getText().toString())) {
            layoutConfirmAnswer.setError(getString(R.string.settings_answer_empty));
        } else if (answer.getText().toString().length() < MIN_ANSWER_LENGTH) {
            layoutAnswer.setError(getString(R.string.settings_answer_fail));
        } else if (confirmAnswer.getText().toString().length() < MIN_ANSWER_LENGTH) {
            layoutConfirmAnswer.setError(getString(R.string.settings_answer_fail));
        } else if (!answer.getText().toString().equals(confirmAnswer.getText().toString())) {
            layoutConfirmAnswer.setError(getString(R.string.settings_answer_equal_fail));
        } else if (answer.getText().toString().equals(confirmAnswer.getText().toString())) {
            isAnswerCorrect = true;
        }
        return isAnswerCorrect;
    }
}
