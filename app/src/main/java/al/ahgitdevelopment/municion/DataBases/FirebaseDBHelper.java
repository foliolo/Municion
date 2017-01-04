package al.ahgitdevelopment.municion.DataBases;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import al.ahgitdevelopment.municion.DataModel.Compra;
import al.ahgitdevelopment.municion.DataModel.Guia;
import al.ahgitdevelopment.municion.DataModel.Licencia;
import al.ahgitdevelopment.municion.Utils;

import static al.ahgitdevelopment.municion.Utils.PREFS_SHOW_ADS;

/**
 * Created by Alberto on 15/10/2016.
 */

public final class FirebaseDBHelper {
    /**
     * Tag de la aplicacion para los logs
     */
    private static final String TAG = "Firebase";
    /**
     * Instancia a la base de datos de Firebase
     */
    public static FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    /**
     * Constante de la referencia push() del usuario en funcion del correo del dispositivo
     */
    public static DatabaseReference userRef;
    /**
     * Instancia de la autentificacion a Firebase del usuario mediante correo y password
     */
    public static FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public static FirebaseUser user;
    /**
     * Permisos para acceder a la cuenta del usuario (Se solicitan en el método onStart() de LoginActivity
     */
    public static boolean accountPermission;
    /**
     * Context
     */
    private static Context context;

    /**
     * Create a handler to handle the result of the authentication
     */
    public static FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            try {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.w(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    SharedPreferences prefs = context.getSharedPreferences("Preferences", Context.MODE_PRIVATE);

                    //Cargamos la información del usuario
                    userRef = mFirebaseDatabase.getReference().child("users").child(user.getUid());
                    userRef.child("email").setValue(user.getEmail());
                    userRef.child("pass").setValue(prefs.getString("password", ""));
                    userRef.child("settings").child("ads_prefs").setValue(prefs.getString(PREFS_SHOW_ADS, "true"));


                } else {
                    // User is signed out
                    Log.w(TAG, "onAuthStateChanged:signed_out");
                }
            } catch (Exception ex) {
                FirebaseCrash.logcat(Log.ERROR, TAG, "Fallo al obtener el usuario para la inserccion en la BBDD de Firebase.");
                FirebaseCrash.report(ex);
            }
        }
    };

    public static void initFirebaseDBHelper(Context mContext) {
        context = mContext;
        try {
            //Guardado del usuario en las shared preferences del dispositivo
            SharedPreferences prefs = context.getSharedPreferences("Preferences", Context.MODE_PRIVATE);
            String email = Utils.getUserEmail(context);
            String pass = prefs.getString("password", "");

            if (!email.isEmpty()) {
                //Obtención del código de autentificación del usuario
                mAuth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
//                                Toast.makeText(context, R.string.auth_usuario_existente, Toast.LENGTH_SHORT).show();
                                    Log.w(TAG, task.getException().getMessage());
                                }
                            }
                        });

                mAuth.signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Log.w(TAG, "signInWithEmail:failed", task.getException());
//                                Toast.makeText(context, R.string.auth_usuario_logado, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            } else {
                mAuth.signInAnonymously()
                        .addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());

                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Log.w(TAG, "signInAnonymously", task.getException());
//                                Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }

        } catch (Exception ex) {
            FirebaseCrash.logcat(Log.ERROR, TAG, "Fallo al iniciar la base de datos de firebase.");
            FirebaseCrash.report(ex);
        }
    }
/*
    public static ArrayList<Guia> getListGuias() {
        final ArrayList<Guia> guias = new ArrayList<>();
        try {
            userRef.child("db").child("guias").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    guias.add(dataSnapshot.getValue(Guia.class));
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return guias;
    }

    public static ArrayList<Compra> getListCompras() {
        final ArrayList<Compra> compras = new ArrayList<>();
        try {
            userRef.child("db").child("compras").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    compras.add(dataSnapshot.getValue(Compra.class));
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return compras;
    }

    public static ArrayList<Licencia> getListLicencias() {
        final ArrayList<Licencia> licencias = new ArrayList<>();

        try {
            userRef.child("db").child("licencias").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    licencias.add(dataSnapshot.getValue(Licencia.class));
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return licencias;
    }
*/

    public static boolean saveLists(final ArrayList<Guia> guias, final ArrayList<Compra> compras, final ArrayList<Licencia> licencias) {
        try {
            //Borrado de la vase de datos actual;
            userRef.child("db").removeValue(new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    userRef.child("db").child("guias").setValue(guias);
                    userRef.child("db").child("compras").setValue(compras);
                    userRef.child("db").child("licencias").setValue(licencias);

                    Log.i(TAG, "Guardado de listas en Firebase");
                }
            });

            return true;
        } catch (Exception ex) {
            FirebaseCrash.logcat(Log.ERROR, TAG, "Fallo guardando las listas");
            FirebaseCrash.report(ex);
            return false;
        }
    }

    public static void updateFirebaseAdsConfig() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Read from the database
//        if (mFirebaseDatabase == null)
//            mFirebaseDatabase = FirebaseDatabase.getInstance();
//
//        DatabaseReference myRef = mFirebaseDatabase.getReference("global_settings/ads");
//        myRef.addValueEventListener(new ValueEventListener() {
//
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                // This method is called once with the initial value and again
//                // whenever data at this location is updated.
//                AdView mAdView = (AdView) view.findViewById(R.id.adView);
//                if (dataSnapshot.getValue() == null ? false : Boolean.valueOf(dataSnapshot.getValue().toString())) {
//                    mAdView.setVisibility(View.VISIBLE);
//                } else {
//                    mAdView.setVisibility(View.GONE);
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                // Failed to read value
//                Log.w("Ads", "Failed to read value.", error.toException());
//            }
//        });
    }
}
