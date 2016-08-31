package al.ahgitdevelopment.municion;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;

import al.ahgitdevelopment.municion.DataModel.Guia;
import al.ahgitdevelopment.municion.DataModel.Licencia;
import al.ahgitdevelopment.municion.DataModel.NotificationData;

/**
 * Created by ahidalgog on 07/07/2016.
 */
public final class Utils {
    public static final String NOTIFICATION_PREFERENCES_FILE = "Notifications";
    public static NotificationData notificationData = new NotificationData();
    public static ArrayList<NotificationData> listNotificationData = new ArrayList<NotificationData>();
    private static SharedPreferences prefs;

    private static FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();

    public static CharSequence[] getLicenseName(Context context) {
        ArrayList<String> list = new ArrayList<>();
        for (Licencia licencia : FragmentMainActivity.licencias) {
            String licenseName = Utils.getStringLicenseFromId(context, licencia.getTipo());
            if (!licenseName.equals("Autonómica de Caza") &&
                    !licenseName.equals("Autonómica de Pesca") &&
                    !licenseName.equals("Permiso Conducir") &&
                    !licenseName.equals("Federativa de tiro"))
                list.add(licenseName);
        }

        return list.toArray(new CharSequence[list.size()]);
    }

    /**
     * Método que devuelve el id (posicion en el Array) de la licencia introducida como parámetro.
     *
     * @param context Necesario para acceder a getResources
     * @param s       Texto de la licencia que se quiere encontrar en el array de licencias para obtener su posicion (id)
     * @return La posicion de la licencia en el array que corresponde con su tipo
     */
    public static int getLicenciaTipoFromString(Context context, String s) {
        String[] listLicencias = context.getResources().getStringArray(R.array.tipo_licencias);
        int idLicencia = -1;
        for (int i = 0; i < listLicencias.length; i++)
            if (listLicencias[i].equals(s))
                idLicencia = i;

        return idLicencia;
    }

    /**
     * Método que devuelve el string de la licencia introduciendo el id (posicion en el array de licencias).
     *
     * @param context
     * @param tipo
     * @return
     */
    public static String getStringLicenseFromId(Context context, int tipo) {
        return context.getResources().getStringArray(R.array.tipo_licencias)[tipo];
    }

    /**
     * Método que devuelve el string del tipo de arma introduciendo el id (posicion en el array de tipos de arma).
     *
     * @param context
     * @param selectedItemPosition
     * @return
     */
    public static String getStringArmaFromId(Context context, int selectedItemPosition) {
        return context.getResources().getStringArray(R.array.tipo_armas)[selectedItemPosition];
    }

    /**
     * Método para comprobar si se puede eliminar una licencia.
     * Para poder eliminar una licencia, antes deben borrarse las guias asociadas a ellas.
     *
     * @param position
     * @return
     */
    public static boolean licenseCanBeDeleted(int position) {
        // Si alguna guía tiene el id de la licencia que queremos borrar, no se podra eliminar la licencia
        for (Guia guia : FragmentMainActivity.guias) {
            if (guia.getTipoLicencia() == FragmentMainActivity.licencias.get(position).getTipo()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Metodo que comprueba si la preferencia existe o no para añadir o modificar la notificacion en Shared Preferences
     */
    public static void addNotificationToSharedPreferences(Context context, int position) {
        //Inicializacion
        prefs = context.getSharedPreferences(NOTIFICATION_PREFERENCES_FILE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();

        //Comprobar si la licencia a añadir es nueva o ya existe
        loadNotificationData(context);
        if (position != -1 && listNotificationData.size() > 0) {
            listNotificationData.get(position).setLicencia(notificationData.getLicencia());
            listNotificationData.get(position).setId(notificationData.getId());
            listNotificationData.get(position).setFecha(notificationData.getFecha());
//            Toast.makeText(context, "Modificada notificacion existente", Toast.LENGTH_SHORT).show();
        } else {
            listNotificationData.add(notificationData);
//            Toast.makeText(context, "Añadida notificacion nueva", Toast.LENGTH_SHORT).show();
        }

        editor.clear().commit();
        editor.putString("notification_data", new Gson().toJson(listNotificationData));
        editor.commit();
    }

    /**
     * Carga la lista de notificaciones del fichero de Shared Preferences a la lista
     */
    public static void loadNotificationData(Context context) {
        if (prefs == null)
            prefs = context.getSharedPreferences(NOTIFICATION_PREFERENCES_FILE, Context.MODE_PRIVATE);

        Gson gson = new Gson();
        if (prefs.contains("notification_data") && !prefs.getString("notification_data", "").equals("")) {
            Type type = new TypeToken<ArrayList<NotificationData>>() {
            }.getType();
            listNotificationData = gson.fromJson(prefs.getString("notification_data", ""), type);
        }
    }


    /**
     * Método que elimina una notificacion del Shared Preferences
     *
     * @param context Contexto de la actividad
     * @param id      Identificador de la notificacion
     */
    public static void removeNotificationFromSharedPreference(Context context, int id) {
        if (prefs == null)
            prefs = context.getSharedPreferences(NOTIFICATION_PREFERENCES_FILE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();

        for (int i = 0; i < listNotificationData.size(); i++) {
            if (listNotificationData.get(i).getId().equals(String.valueOf(id))) {
                listNotificationData.remove(i);
            }
        }

        editor.clear().commit();
        editor.putString("notification_data", new Gson().toJson(listNotificationData));
        editor.commit();
    }

    /**
     * Método que comprueba si existe licencia Federativa de tiro
     *
     * @param context Contexto de la actividad
     */
    public static boolean isLicenciaFederativa(Context context) {
        for (Licencia licencia : FragmentMainActivity.licencias) {
            String licenseName = Utils.getStringLicenseFromId(context, licencia.getTipo());
            if (licenseName.equals("Federativa de tiro")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Método que devuelve la categoria mas alta
     *
     * @param context Contexto de la actividad
     */
    public static int getMaxCategoria(Context context) {
        ArrayList<Integer> list = new ArrayList<>();
        for (Licencia licencia : FragmentMainActivity.licencias) {
            int categoria = Utils.getCategoriaId(context, licencia.getCategoria());
            if (categoria != -1) {
                list.add(categoria);
            }
        } // Devuelve el numero menor que seria la licencia de mas categoria
        return Collections.min(list);
    }

    /**
     * Método que devuelve el id (posicion en el Array) de la categoria introducida como parametro.
     *
     * @param context Necesario para acceder a getResources
     * @return El indice de la categoria en el array que corresponde con su tipo
     */
    public static int getCategoriaId(Context context, int cat) {
        String[] categorias = context.getResources().getStringArray(R.array.categorias);
        for (int i = 0; i < categorias.length; i++) {
            if (i == cat) {
                return cat;
            }
        }
        return -1;
    }


    public static AdRequest getAdRequest(final View view) {
        // Read from the database
        DatabaseReference myRef = mFirebaseDatabase.getReference("settings/ads");

        myRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                AdView mAdView = (AdView) view.findViewById(R.id.adView);
                if (dataSnapshot.getValue(Boolean.class)) {
                    mAdView.setVisibility(View.VISIBLE);
                } else {
                    mAdView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Ads", "Failed to read value.", error.toException());
            }
        });

        return new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                .addTestDevice("19DFD6D99DFA16A1568E51C0698B3E2F")  // An example device ID
                .build();
    }
}
