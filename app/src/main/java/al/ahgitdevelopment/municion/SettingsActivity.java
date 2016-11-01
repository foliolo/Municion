package al.ahgitdevelopment.municion;


import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list_guias. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list_guias of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    private Toolbar toolbar;

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupActionBar();

        // Add a button to the header list.
        if (hasHeaders()) {
            TextView text = new TextView(this);
            text.setText(Utils.getAppVersion(this));
            text.setPadding(20, 0, 0, 10);
            setListFooter(text);
        }
    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {

        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        LinearLayout llc = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.basic_toolbar, root, false);
        root.addView(llc, 0);


        toolbar = (Toolbar) root.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setSubtitle(R.string.title_activity_settings);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_bullseye);

//        ActionBar actionBar = getSupportActionBar(toolbar);
//        if (actionBar != null) {
//            // Show the Up button in the action bar.
//            actionBar.setDisplayHomeAsUpEnabled(true);
//        }
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return ChangePasswordDialog.class.getName().equals(fragmentName);
    }

    @Override
    public void onHeaderClick(Header header, int position) {

//        if (onIsMultiPane()) {
//            switchToHeader(header);
//        }else
        if (header.id == R.id.change_password) {
            DialogFragment dialog = new ChangePasswordDialog();
            dialog.show(getFragmentManager(), "ChangePasswordDialog");
        }
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        final String showFragment = getIntent().getStringExtra(EXTRA_SHOW_FRAGMENT);
//        if (showFragment != null) {
//            for (final Header header : mHeaders) {
//                if (showFragment.equals(header.fragment)) {
//                    switchToHeader(header);
//                    break;
//                }
//            }
//        }
//    }

    @Override
    public void startPreferencePanel(String fragmentClass, Bundle args, int titleRes, CharSequence titleText, Fragment resultTo, int resultRequestCode) {
//        super.startPreferencePanel(fragmentClass, args, titleRes, titleText, resultTo, resultRequestCode);
        super.startPreferencePanel(
                "al.ahgitdevelopment.municion.ChangePasswordDialog",
                null,
                R.string.settings_title_change_password,
                "",
                getFragmentManager().getFragment(null, "al.ahgitdevelopment.municion.ChangePasswordDialog"),
                100);
    }

    @Override
    public void startPreferenceFragment(Fragment fragment, boolean push) {
        super.startPreferenceFragment(new ChangePasswordDialog(), false);
    }

    @Override
    public void startWithFragment(String fragmentName, Bundle args, Fragment resultTo, int resultRequestCode, int titleRes, int shortTitleRes) {
        super.startWithFragment(
                "al.ahgitdevelopment.municion.ChangePasswordDialog",
                null,
                new ChangePasswordDialog(),
                100,
                0,
                0);
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragment caller, Preference pref) {
        return super.onPreferenceStartFragment(caller, pref);
    }
}
