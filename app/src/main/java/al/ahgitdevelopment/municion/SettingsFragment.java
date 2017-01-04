package al.ahgitdevelopment.municion;


import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.security.SecureRandom;
import java.util.ArrayList;

import al.ahgitdevelopment.municion.BillingUtil.IabHelper;
import al.ahgitdevelopment.municion.BillingUtil.IabResult;
import al.ahgitdevelopment.municion.BillingUtil.Inventory;
import al.ahgitdevelopment.municion.BillingUtil.Purchase;
import al.ahgitdevelopment.municion.Dialogs.ChangePasswordDialog;
import al.ahgitdevelopment.municion.Dialogs.ResetPasswordDialog;
import al.ahgitdevelopment.municion.Dialogs.SecurityQuestionDialog;

import static al.ahgitdevelopment.municion.Utils.PREFS_PAYLOAD;
import static al.ahgitdevelopment.municion.Utils.PREFS_SHOW_ADS;
import static al.ahgitdevelopment.municion.Utils.PURCHASE_ID_REMOVE_ADS;

/**
 * A simple {@link FragmentActivity} subclass.
 */
public class SettingsFragment extends FragmentActivity implements IabHelper.QueryInventoryFinishedListener,
        IabHelper.OnIabPurchaseFinishedListener {

    private static final String TAG = "SettignsFragment";
    private static final int RC_PURCHASE_FLOW = 100;
    SharedPreferences prefs;
    private IabHelper mHelper;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_settings);

        prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);

        ((TextView) findViewById(R.id.version_text)).setText(Utils.getAppVersion(this));

        String base64EncodedPublicKey = getString(R.string.app_public_key);
        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh no, there was a problem.
                    Log.d(TAG, "Problem setting up In-app Billing: " + result.getMessage());
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (mHelper != null)
            mHelper.dispose();
        mHelper = null;

        super.onDestroy();
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

    public void removeAds(View view) {
//        Utils.setUpInAppBilling(this/*Context*/, this/*QueryInventoryFinishedListener*/);
        ArrayList<String> additionalSkuList = new ArrayList<>();
        additionalSkuList.add(PURCHASE_ID_REMOVE_ADS);
        mHelper.queryInventoryAsync(true, additionalSkuList, this /*QueryFinishedListener*/);
    }

    @Override
    public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
        // if we were disposed of in the meantime, quit.
        if (mHelper == null) return;

        if (result.isFailure()) {
            Log.e(TAG, "Error obteniendo los detalles de productos" + result.getMessage());
            return;
        }

        if (inventory.hasPurchase(PURCHASE_ID_REMOVE_ADS)) {
            //pero no tiene actualizado su shared prefs
            if (prefs.getBoolean("show_ads", true)) {
                // Actualizamos las preferencias
                prefs.edit().putBoolean("show_ads", false);
            }
        } else {
            //Generar PREFS_PAYLOAD del usuario: Numero aleatorio que identifica al usuario
            SecureRandom random = new SecureRandom();
            String payload = new java.math.BigInteger(130, random).toString(32);
            if (!prefs.contains(PREFS_PAYLOAD))
                prefs.edit().putString(PREFS_PAYLOAD, payload).apply();

            // Realizar compra para eliminar publicidad
            mHelper.launchPurchaseFlow(this, PURCHASE_ID_REMOVE_ADS, RC_PURCHASE_FLOW,
                    this /*PurchaseFinishedListener*/, payload);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
    }

    @Override
    public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
        Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

        // if we were disposed of in the meantime, quit.
        if (mHelper == null) return;

        if (result.isFailure()) {
            Log.d(TAG, "Error purchasing: " + result.toString());
            new AlertDialog.Builder(SettingsFragment.this)
                    .setTitle(getString(R.string.purchase_cancel))
                    .setMessage(result.getMessage())
                    .setPositiveButton(android.R.string.ok, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return;
        }

        // TASK TO DO
        prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        String payload = "";
        if (prefs.contains(PREFS_PAYLOAD))
            payload = prefs.getString(PREFS_PAYLOAD, "");

        if (purchase.getSku().equals(PURCHASE_ID_REMOVE_ADS) && purchase.getDeveloperPayload().equals(payload)) {
            // Compra realizada con existo
            // Actualizar Shared Prefs
            prefs.edit().putBoolean(PREFS_SHOW_ADS, false).apply();
        }
    }
}
