package al.ahgitdevelopment.municion;


import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

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
}
