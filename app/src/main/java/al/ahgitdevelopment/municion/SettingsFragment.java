package al.ahgitdevelopment.municion;


import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.google.firebase.crash.FirebaseCrash;

import java.security.SecureRandom;

import al.ahgitdevelopment.municion.billingutil.IabBroadcastReceiver;
import al.ahgitdevelopment.municion.billingutil.IabHelper;
import al.ahgitdevelopment.municion.billingutil.IabResult;
import al.ahgitdevelopment.municion.billingutil.Inventory;
import al.ahgitdevelopment.municion.billingutil.Purchase;
import al.ahgitdevelopment.municion.dialogs.ChangePasswordDialog;
import al.ahgitdevelopment.municion.dialogs.ResetPasswordDialog;
import al.ahgitdevelopment.municion.dialogs.SecurityQuestionDialog;

import static al.ahgitdevelopment.municion.Utils.PREFS_PAYLOAD;
import static al.ahgitdevelopment.municion.Utils.PREFS_SHOW_ADS;
import static al.ahgitdevelopment.municion.login.LoginPasswordFragment.PURCHASE_ID_REMOVE_ADS;

/**
 * A simple {@link FragmentActivity} subclass.
 */
public class SettingsFragment extends FragmentActivity implements
        IabBroadcastReceiver.IabBroadcastListener, IabHelper.QueryInventoryFinishedListener,
        IabHelper.OnIabPurchaseFinishedListener, IabHelper.OnConsumeFinishedListener {

    private static final String TAG = "SettignsFragment";
    private static final int RC_PURCHASE_FLOW = 100;
    SharedPreferences prefs;
    // Provides purchase notification while this app is running
    IabBroadcastReceiver mBroadcastReceiver;
    private IabHelper mHelper;
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

        String base64EncodedPublicKey = getString(R.string.app_public_key);
        mHelper = new IabHelper(this, base64EncodedPublicKey);

        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(true);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh no, there was a problem.
                    Log.d(TAG, "Problem setting up In-app Billing: " + result.getMessage());
                    isPurchaseAvailable = false;
                } else {
                    isPurchaseAvailable = true;
                }
            }
        });

        settingOptionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
                    case 4:
                        removeAds();
                        break;
                    case 5:
                        consumeAds();
                        break;
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Important: Dynamically register for broadcast messages about updated purchases.
        // We register the receiver here instead of as a <receiver> in the Manifest
        // because we always call getPurchases() at startup, so therefore we can ignore
        // any broadcasts sent while the app isn't running.
        // Note: registering this listener in an Activity is a bad idea, but is done here
        // because this is a SAMPLE. Regardless, the receiver must be registered after
        // IabHelper is setup, but before first call to getPurchases().
        mBroadcastReceiver = new IabBroadcastReceiver(this /*IabBroadcastListener*/);
        IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
        registerReceiver(mBroadcastReceiver, broadcastFilter);
    }

    /**
     * We're being destroyed. It's important to dispose of the helper here!
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // very important:
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }

        // very important:
        try {
            Log.d(TAG, "Destroying helper.");
            if (mHelper != null) {
                mHelper.disposeWhenFinished();
                mHelper = null;
            }
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "Fallo en el dispose de IabHelper");
            FirebaseCrash.report(ex);
        }
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

    public void removeAds() {
        try {
            if (isPurchaseAvailable && Utils.isGooglePlayServicesAvailable(this)) {
                flagConsume = false;
//                ArrayList<String> additionalSkuList = new ArrayList<>();
//                additionalSkuList.add(PURCHASE_ID_REMOVE_ADS);
                try {
                    mHelper.queryInventoryAsync(this /*QueryInventoryFinishedListener*/);
                } catch (IabHelper.IabAsyncInProgressException ex) {
                    FirebaseCrash.logcat(Log.ERROR, TAG, "Error querying inventory. Another async operation in progress.");
                    FirebaseCrash.report(ex);
                }
            } else {
                Toast.makeText(this, R.string.purchase_not_support, Toast.LENGTH_SHORT).show();
            }
        } catch (IllegalStateException ex) {
            Toast.makeText(this, "Ex: " + R.string.purchase_not_support, Toast.LENGTH_SHORT).show();
            FirebaseCrash.report(ex);
        }
    }

    public void consumeAds() {
        flagConsume = true;
//        ArrayList<String> additionalSkuList = new ArrayList<>();
//        additionalSkuList.add(PURCHASE_ID_REMOVE_ADS);
        try {
            mHelper.queryInventoryAsync(this /*QueryInventoryFinishedListener*/);
        } catch (IabHelper.IabAsyncInProgressException ex) {
            FirebaseCrash.logcat(Log.ERROR, TAG, "Error querying inventory. Another async operation in progress.");
            FirebaseCrash.report(ex);
        }
    }

    @Override
    public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
        try {
            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                Log.e(TAG, "Error obteniendo los detalles de productos" + result);
                return;
            }

            // Ya se ha realizado la compra
            if (inventory.hasPurchase(PURCHASE_ID_REMOVE_ADS)) {
                //pero no tiene actualizado su shared prefs
                if (prefs.getBoolean("show_ads", true)) {
                    // Actualizamos las preferencias
                    prefs.edit().putBoolean(PREFS_SHOW_ADS, false).apply();
                }
                Toast.makeText(SettingsFragment.this, R.string.purchase_done, Toast.LENGTH_SHORT).show();

                //TODO: Remove this!!! Just for testing
                try {
                    if (flagConsume) {
                        mHelper.consumeAsync(inventory.getPurchase(PURCHASE_ID_REMOVE_ADS), this);
                        prefs.edit().putBoolean(PREFS_SHOW_ADS, true).apply();
                    }
                } catch (IabHelper.IabAsyncInProgressException ex) {
                    FirebaseCrash.logcat(Log.ERROR, TAG, "Error consuming gas. Another async operation in progress.");
                    FirebaseCrash.report(ex);
                }
            } else { //No se ha realizado la compra y procedemos a ello
                //Generar PREFS_PAYLOAD del usuario: Numero aleatorio que identifica al usuario
                SecureRandom random = new SecureRandom();
                String payload = new java.math.BigInteger(130, random).toString(32);

                if (!prefs.contains(PREFS_PAYLOAD))
                    prefs.edit().putString(PREFS_PAYLOAD, payload).apply();
                else
                    payload = prefs.getString(PREFS_PAYLOAD, "");

                // Realizar compra para eliminar publicidad
                try {
                    mHelper.launchPurchaseFlow(this, PURCHASE_ID_REMOVE_ADS, RC_PURCHASE_FLOW,
                            this /*PurchaseFinishedListener*/, payload);
                } catch (IabHelper.IabAsyncInProgressException ex) {
                    FirebaseCrash.logcat(Log.ERROR, TAG, "Error launching purchase flow. Another async operation in progress.");
                    FirebaseCrash.report(ex);
                }
            }
        } catch (Exception ex) {
            FirebaseCrash.logcat(Log.ERROR, TAG, "Error en el proceso de onQueryInventoryFinished");
            FirebaseCrash.report(ex);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
            if (mHelper == null) return;

            // Pass on the activity result to the helper for handling
            if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
                // not handled, so handle it ourselves (here's where you'd
                // perform any handling of activity results not related to in-app
                // billing...
                super.onActivityResult(requestCode, resultCode, data);
            } else {
                Log.d(TAG, "onActivityResult handled by IABUtil.");
            }
        } catch (Exception ex) {
            FirebaseCrash.logcat(Log.ERROR, TAG, ex.getMessage());
            FirebaseCrash.report(ex);
        }
    }

    @Override
    public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
        try {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                Log.d(TAG, "Error purchasing: " + result.toString());
                new AlertDialog.Builder(SettingsFragment.this)
                        .setTitle(getString(R.string.purchase_cancel))
                        .setPositiveButton(android.R.string.ok, null)
                        .setIcon(R.drawable.ic_warning_amber_48pt_3x)
                        .show();
                return;
            }

            // TASK TO DO
            String payload = "";
            if (prefs.contains(PREFS_PAYLOAD))
                payload = prefs.getString(PREFS_PAYLOAD, "");

            if (purchase.getSku().equals(PURCHASE_ID_REMOVE_ADS) && purchase.getDeveloperPayload().equals(payload)) {
                // Compra realizada con existo
                // Actualizar Shared Prefs
                prefs.edit().putBoolean(PREFS_SHOW_ADS, false).apply();
            } else {
                Log.w(TAG, "Error purchasing. Authenticity verification failed.");
            }
        } catch (Exception ex) {
            FirebaseCrash.logcat(Log.ERROR, TAG, ex.getMessage());
            FirebaseCrash.report(ex);
        }
    }

    @Override
    public void onConsumeFinished(Purchase purchase, IabResult result) {
        try {
            if (result.isSuccess()) {
                // provision the in-app purchase to the user
                // (for example, credit 50 gold coins to player's character)
                Log.i(TAG, "Compra consumida");
                Toast.makeText(this, "Compra consumida", Toast.LENGTH_SHORT).show();
                prefs.edit().putBoolean(PREFS_SHOW_ADS, true).apply();
            } else {
                // handle error
                Log.w(TAG, "Error cosumiendo la compra!");
                Toast.makeText(this, "Error consumiendo compra", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex) {
            FirebaseCrash.logcat(Log.ERROR, TAG, ex.getMessage());
            FirebaseCrash.report(ex);
        }
    }

    @Override
    public void receivedBroadcast() {
        // Received a broadcast notification that the inventory of items has changed
        Log.d(TAG, "Received broadcast notification. Querying inventory.");
        try {
            mHelper.queryInventoryAsync(SettingsFragment.this /*QueryInventoryFinishedListener*/);
        } catch (IabHelper.IabAsyncInProgressException ex) {
            Log.e(TAG, "Error querying inventory. Another async operation in progress.", ex);
        }
    }
}
