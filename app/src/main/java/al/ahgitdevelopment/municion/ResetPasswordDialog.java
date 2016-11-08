package al.ahgitdevelopment.municion;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by david.sierra on 07/11/2016.
 */

public class ResetPasswordDialog extends DialogFragment {

    private SharedPreferences preferences;
    private Activity activity;
    private TextInputLayout layoutQuestion;
    private TextInputEditText question;
    private TextInputLayout layoutAnswer;
    private TextInputEditText answer;
    public static TextView textEmptyList = null;
    private boolean isQuestion = false;

    @Nullable
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        preferences = this.getActivity().getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        activity = this.getActivity();
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.reset_password_items, null);

        layoutQuestion = (TextInputLayout) view.findViewById(R.id.text_input_layout_question_reset);
        question = (TextInputEditText) view.findViewById(R.id.question);
        layoutAnswer = (TextInputLayout) view.findViewById(R.id.text_input_layout_answer_reset);
        answer = (TextInputEditText) view.findViewById(R.id.answer);
        question.setText(preferences.getString("question", ""));
        textEmptyList = (TextView) view.findViewById(R.id.textEmptyList);

        if(question == null || question.getText().toString().isEmpty()) {
            textEmptyList.setVisibility(View.VISIBLE);
            textEmptyList.setText(R.string.settings_empty_question_security);
            layoutQuestion.setVisibility(View.GONE);
            layoutAnswer.setVisibility(View.GONE);
        } else {
            isQuestion = true;
            textEmptyList.setVisibility(View.GONE);
            layoutQuestion.setVisibility(View.VISIBLE);
            layoutAnswer.setVisibility(View.VISIBLE);
        }
        // Set custom view
        builder.setView(view).setTitle(R.string.settings_title_reset_password)
                // Add action buttons
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (checkAnswerSecurityPreferences() && isQuestion) {
                            clearPreferences();
                            Toast.makeText(getActivity(), R.string.settings_reset_password, Toast.LENGTH_SHORT).show();
                            dismiss();
                            Intent intent = new Intent(activity, LoginPasswordActivity.class);
                            intent.putExtra("tutorial", false);
                            startActivity(intent);
                        } else if (!isQuestion){
                            dismiss();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ResetPasswordDialog.this.getDialog().cancel();
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
                    if (checkAnswerSecurityPreferences() && isQuestion) {
                        clearPreferences();
                        Toast.makeText(getActivity(), R.string.settings_reset_password, Toast.LENGTH_SHORT).show();
                        dismiss();
                        Intent intent = new Intent(activity, LoginPasswordActivity.class);
                        intent.putExtra("tutorial", false);
                        startActivity(intent);
                    } else if (!isQuestion){
                        dismiss();
                    }
                    //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                }
            });
        }
    }

    private boolean checkAnswerSecurityPreferences() {
        boolean isPassCorrect = false;
        String answerPref = preferences.getString("answer", "");
        if ("".equals(answer)) {
            layoutAnswer.setError(getString(R.string.settings_answer_empty));
        } else if (answerPref.equals(answer.getText().toString())) {
            isPassCorrect = true;
            layoutAnswer.setError(null);
        } else {
            layoutAnswer.setError(getString(R.string.settings_reset_answer_fail));
        }
        return isPassCorrect;
    }

    public void clearPreferences() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

}
