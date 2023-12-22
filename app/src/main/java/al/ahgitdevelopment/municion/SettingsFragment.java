package al.ahgitdevelopment.municion;


import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import al.ahgitdevelopment.municion.dialogs.ChangePasswordDialog;
import al.ahgitdevelopment.municion.dialogs.ResetPasswordDialog;
import al.ahgitdevelopment.municion.dialogs.SecurityQuestionDialog;


/**
 * A simple {@link FragmentActivity} subclass.
 */
public class SettingsFragment extends FragmentActivity {

    private static final String TAG = "SettignsFragment";
    private static final int RC_PURCHASE_FLOW = 100;
    SharedPreferences prefs;
    // Provides purchase notification while this app is running
    private ListView settingOptionList;
    private boolean flagConsume;
    private boolean isPurchaseAvailable;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_settings);

        prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);

        settingOptionList = findViewById(R.id.settings_option_list);
        ((TextView) findViewById(R.id.version_text)).setText(Utils.getAppVersion(this));

        settingOptionList.setOnItemClickListener((parent, view, position, id) -> {
            switch (position) {
                case 0:
                    changePassword();
                    break;
                case 1:
                    showTutorial();
                    break;
                case 2:
                    securityQuestion();
                    break;
                case 3:
                    resetPassword();
                    break;
            }
        });
    }

    public void changePassword() {
        DialogFragment dialog = new ChangePasswordDialog();
        dialog.show(getFragmentManager(), "ChangePasswordDialog");
    }

    public void showTutorial() {
        Intent intent = new Intent(this, FragmentTutorialActivity.class);
        startActivity(intent);
    }

    public void securityQuestion() {
        DialogFragment dialog = new SecurityQuestionDialog();
        dialog.show(getFragmentManager(), "SecurityQuestionDialog");
    }

    public void resetPassword() {
        DialogFragment dialog = new ResetPasswordDialog();
        dialog.show(getFragmentManager(), "ResetPasswordDialog");
    }
}
