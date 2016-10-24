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
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import al.ahgitdevelopment.municion.DataModel.Compra;
import al.ahgitdevelopment.municion.DataModel.Guia;
import al.ahgitdevelopment.municion.DataModel.Licencia;
import al.ahgitdevelopment.municion.Utils;

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
            user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // User is signed in
                Log.w(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                //Cargamos la información del usuario
                userRef = mFirebaseDatabase.getReference().child("users").child(user.getUid());
                userRef.child("email").setValue(user.getEmail());
                userRef.child("pass").setValue(context.getSharedPreferences("Preferences", Context.MODE_PRIVATE).getString("password", ""));

            } else {
                // User is signed out
                Log.w(TAG, "onAuthStateChanged:signed_out");
            }
        }
    };

    public static void initFirebaseDBHelper(Context mContext) {
        context = mContext;

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
                                task.getException().printStackTrace();
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
                        if (guias.size() != 0)
                            textEmptyList.setVisibility(View.GONE);
                        else
                            textEmptyList.setVisibility(View.VISIBLE);
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
                        if (compras.size() != 0)
                            textEmptyList.setVisibility(View.GONE);
                        else
                            textEmptyList.setVisibility(View.VISIBLE);
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
                        if (licencias.size() != 0)
                            textEmptyList.setVisibility(View.GONE);
                        else
                            textEmptyList.setVisibility(View.VISIBLE);
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
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
