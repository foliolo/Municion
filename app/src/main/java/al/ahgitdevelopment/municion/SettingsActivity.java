package al.ahgitdevelopment.municion;


import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.content.Context;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.MenuItem;

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
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    public static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list_guias preferences, look up the correct display value in
                // the preference's 'entries' list_guias.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
//    private GoogleApiClient client;

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();


    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
//    protected boolean isValidFragment(String fragmentName) {
//        return PreferenceFragment.class.getName().equals(fragmentName)
//                || ChangePasswordDialog.class.getName().equals(fragmentName);
//    }
    @Override
    public void onHeaderClick(Header header, int position) {
        super.onHeaderClick(header, position);
        if (header.id == R.id.change_password) {
            DialogFragment dialog = new ChangePasswordDialog();
            dialog.show(getFragmentManager(), "ChangePasswordDialog");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getFragmentManager().popBackStackImmediate();
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//
//        // ATTENTION: This was auto-generated to implement the App Indexing API.
//        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        client.connect();
//        Action viewAction = Action.newAction(
//                Action.TYPE_VIEW, // TODO: choose an action type.
//                "Settings Page", // TODO: Define a title for the content shown.
//                // TODO: If you have web page content that matches this app activity's content,
//                // make sure this auto-generated web page URL is correct.
//                // Otherwise, set the URL to null.
//                Uri.parse("http://host/path"),
//                // TODO: Make sure this auto-generated app URL is correct.
//                Uri.parse("android-app://al.ahgitdevelopment.municion/http/host/path")
//        );
//        AppIndex.AppIndexApi.start(client, viewAction);
//    }

//    @Override
//    public void onStop() {
//        super.onStop();
//
//        // ATTENTION: This was auto-generated to implement the App Indexing API.
//        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        Action viewAction = Action.newAction(
//                Action.TYPE_VIEW, // TODO: choose an action type.
//                "Settings Page", // TODO: Define a title for the content shown.
//                // TODO: If you have web page content that matches this app activity's content,
//                // make sure this auto-generated web page URL is correct.
//                // Otherwise, set the URL to null.
//                Uri.parse("http://host/path"),
//                // TODO: Make sure this auto-generated app URL is correct.
//                Uri.parse("android-app://al.ahgitdevelopment.municion/http/host/path")
//        );
//        AppIndex.AppIndexApi.end(client, viewAction);
//        client.disconnect();
//    }


//    public static class DialogChangePassword extends DialogFragment {
//        private SharedPreferences preferences;
//        private TextInputEditText passwordOld;
//        private TextInputEditText passwordNew1;
//        private TextInputEditText passwordNew2;
//        private Button cancelar;
//        private Button guardar;
//
//        @Override
//        public void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//            preferences = this.getActivity().getSharedPreferences("Preferences", Context.MODE_PRIVATE);
//            setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Material_Dialog);
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//            final View view = inflater.inflate(R.layout.settings_items, container, false);
//            passwordOld = (TextInputEditText) view.findViewById(R.id.passwordOld);
//            passwordNew1 = (TextInputEditText) view.findViewById(R.id.passwordNew);
//            passwordNew2 = (TextInputEditText) view.findViewById(R.id.passwordNew2);
//            cancelar = (Button) view.findViewById(R.id.button1);
//            guardar = (Button) view.findViewById(R.id.button2);
//
//            cancelar.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    getActivity().finish();
//                }
//            });
//
//            guardar.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    savePassword();
//                }
//            });
//
//            return view;
//        }
//
////        @Override
////        public Dialog onCreateDialog(Bundle savedInstanceState) {
//////            preferences = this.getActivity().getSharedPreferences("Preferences", Context.MODE_PRIVATE);
////            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
////
////            // Get the layout inflater
////            LayoutInflater inflater = getActivity().getLayoutInflater();
////            View view = inflater.inflate(R.layout.settings_items, null);
////
////            passwordOld = (TextInputEditText) view.findViewById(R.id.passwordOld);
////            passwordNew1 = (TextInputEditText) view.findViewById(R.id.passwordNew);
////            passwordNew2 = (TextInputEditText) view.findViewById(R.id.passwordNew2);
////
////
////            // Set custom view
////            builder.setView(view)
////                    // Add action buttons
////                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
////                        @Override
////                        public void onClick(DialogInterface dialog, int id) {
////                            savePassword();
////                        }
////                    })
////                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
////                        public void onClick(DialogInterface dialog, int id) {
////                            DialogChangePassword.this.getDialog().cancel();
////                        }
////                    });
////
////            return builder.create();
////        }
//
//
//        /**
//         * Guarda el password introducido por el usuario para cambiar la contraseña
//         *
//         * @return flag
//         */
//        private boolean savePassword() {
//            boolean flag = false;
//            if (passwordOld.getText() != null && passwordOld.getText().toString().length() >= 4) {
//                if (checkPasswordOld() && checkPasswordNew()) {
//                    // Ha ido correcto
//                    SharedPreferences.Editor editor = preferences.edit();
//                    editor.putString("password", passwordNew1.getText().toString());
//                    editor.commit();
//                    Snackbar.make(getView(), R.string.password_update, Snackbar.LENGTH_INDEFINITE)
//                            .setAction(android.R.string.ok, new View.OnClickListener() {
//                                @Override
//                                public void onClick(View view) {
//                                }
//                            })
//                            .show();
//                    flag = true;
//                }
//            } else {
//                passwordOld.setError(getString(R.string.password_short_fail));
//            }
//            return flag;
//        }
//
//        /**
//         * Valida la contraseña introducida por el usuario frente a la guardada en el sharedPreferences
//         *
//         * @return password valido o invalido
//         */
//        private boolean checkPasswordOld() {
//            boolean isPassCorrect = false;
//            String pass = preferences.getString("password", "");
//            if ("".equals(pass)) {
//                passwordOld.setError(getString(R.string.settings_password_unlogin));
//            } else if (pass.equals(passwordOld.getText().toString())) {
//                isPassCorrect = true;
//            } else {
//                passwordOld.setError(getString(R.string.password_equal_fail));
//            }
//            return isPassCorrect;
//        }
//
//        /**
//         * Valida que la contraseña nueva sea correcta, se introduce en dos campos
//         *
//         * @return password valido o invalido
//         */
//        private boolean checkPasswordNew() {
//            boolean isPassCorrect = false;
//            if ("".equals(passwordNew1.getText().toString())) {
//                passwordNew1.setError(getString(R.string.settings_password_empty));
//            } else if ("".equals(passwordNew2.getText().toString())) {
//                passwordNew2.setError(getString(R.string.settings_password_empty));
//            } else if (passwordNew1.getText().toString().length() < 4) {
//                passwordNew1.setError(getString(R.string.password_short_fail));
//            } else if (passwordNew2.getText().toString().length() < 4) {
//                passwordNew2.setError(getString(R.string.password_short_fail));
//            } else if (!passwordNew1.getText().toString().equals(passwordNew2.getText().toString())) {
//                passwordNew2.setError(getString(R.string.password_equal_fail));
//            } else if (passwordNew1.getText().toString().equals(passwordNew2.getText().toString())) {
//                isPassCorrect = true;
//            }
//            return isPassCorrect;
//        }
//    }
}
