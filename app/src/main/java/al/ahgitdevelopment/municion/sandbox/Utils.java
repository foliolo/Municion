package al.ahgitdevelopment.municion.sandbox;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.NoSuchElementException;

import al.ahgitdevelopment.municion.R;
import al.ahgitdevelopment.municion.datamodel.NotificationData;
import al.ahgitdevelopment.municion.datamodel.Property;

import static al.ahgitdevelopment.municion.sandbox.FragmentMainContent.fileImagePath;

/**
 * Created by ahidalgog on 07/07/2016.
 */
public final class Utils {
    //    public static final String PURCHASE_ID_REMOVE_ADS = "android.test.purchased";
//    public static final String PURCHASE_ID_REMOVE_ADS = "android.test.canceled";
    static final String PREFS_PAYLOAD = "payload";
    private static final String NOTIFICATION_PREFERENCES_FILE = "Notifications";
    public static NotificationData notificationData = new NotificationData();
    static ArrayList<NotificationData> listNotificationData = new ArrayList();
    private static SharedPreferences prefs;

    @NonNull
    static CharSequence[] getLicenseName(Context context) {
        ArrayList<String> list = new ArrayList<>();
//        for (Licencia licencia : FragmentMainContent.licencias) {
//            String licenseName = Utils.getStringLicenseFromId(context, licencia.getTipo());
//            if (!licenseName.equals("Autonómica de Caza") &&
//                    !licenseName.equals("Autonómica de Pesca") &&
//                    !licenseName.equals("Permiso Conducir") &&
//                    !licenseName.equals("Federativa de tiro"))
//                list.add(licenseName);
//        }
        return list.toArray(new CharSequence[list.size()]);
    }

    /**
     * Método que devuelve el id (posicion en el Array) de la licencia introducida como parámetro.
     *
     * @param context Necesario para acceder a getResources
     * @param s       Texto de la licencia que se quiere encontrar en el array de licencias para obtener su posicion (id)
     * @return La posicion de la licencia en el array que corresponde con su tipo
     */
    public static Long getLicenciaTipoFromString(Context context, String s) {
        String[] listLicencias = context.getResources().getStringArray(R.array.tipo_licencias);
        long idLicencia = -1L;
        for (long i = 0; i < listLicencias.length; i++)
            if (listLicencias[(int) i].equals(s))
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
    public static String getStringLicenseFromId(Context context, long tipo) {
        return context.getResources().getStringArray(R.array.tipo_licencias)[(int) tipo];
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
     * Método que devuelve el string del tipo de pregunta introduciendo el id (posicion en el array de tipos de pregunta).
     *
     * @param dialogFragment
     * @param selectedItemPosition
     * @return
     */
    public static String getStringTipoPregunta(DialogFragment dialogFragment, int selectedItemPosition) {
        return dialogFragment.getResources().getStringArray(R.array.preguntas_seguridad)[selectedItemPosition];
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
        for (Property property : FragmentMainContent.properties) {
//            if (guia.getTipoLicencia() == FragmentMainContent.licencias.get(position).getTipo()) {
//                return true;
//            }
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
            // Para evitar un IndexOutOfBoundsException ya que hay casos en que position no coincide
            // con el size de la lista y se produce esta excepcion cuando se invoca
            // listNotificationData.get(position). No se si meter en el if lo mismo que el else
            // Por eso lo he comentado, entiendo que no.
            if (listNotificationData.size() <= position || listNotificationData.get(position) == null) {
                Log.e("Utils.class", "No coincide position con el size de listNotificationData");
                // listNotificationData.add(notificationData);
            } else {
                listNotificationData.get(position).setLicencia(notificationData.getLicencia());
                listNotificationData.get(position).setId(notificationData.getId());
                listNotificationData.get(position).setFecha(notificationData.getFecha());
//              Toast.makeText(context, "Modificada notificacion existente", Toast.LENGTH_SHORT).show();
            }
        } else {
            listNotificationData.add(notificationData);
//            Toast.makeText(context, "Añadida notificacion nueva", Toast.LENGTH_SHORT).show();
        }

        editor.clear().apply();
        editor.putString("notification_data", new Gson().toJson(listNotificationData));
        editor.commit();
    }

    /**
     * Carga la lista de notificaciones del fichero de Shared Preferences a la lista
     */
    private static void loadNotificationData(Context context) {
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
    static void removeNotificationFromSharedPreference(Context context, String id) {
        if (prefs == null)
            prefs = context.getSharedPreferences(NOTIFICATION_PREFERENCES_FILE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();

        for (int i = 0; i < listNotificationData.size(); i++) {
            if (listNotificationData.get(i).getId().equals(id)) {
                listNotificationData.remove(i);
            }
        }

        editor.clear().apply();
        editor.putString("notification_data", new Gson().toJson(listNotificationData));
        editor.commit();
    }

    /**
     * Método que comprueba si existe licencia Federativa de tiro
     *
     * @param context Contexto de la actividad
     */
    public static boolean isLicenciaFederativa(Context context) {
//        for (Licencia licencia : FragmentMainContent.licencias) {
//            String licenseName = Utils.getStringLicenseFromId(context, licencia.getTipo());
//            if (licenseName.equals("Federativa de tiro")) {
//                return true;
//            }
//        }
        return false;
    }

    /**
     * Método que devuelve la categoria mas alta
     *
     * @param context Contexto de la actividad
     */
    public static int getMaxCategoria(Context context) {
        try {
            ArrayList<Integer> list = new ArrayList<>();
//            for (Licencia licencia : FragmentMainContent.licencias) {
//                int categoria = Utils.getCategoriaId(context, licencia.getCategoria());
//                if (categoria != -1) {
//                    list.add(categoria);
//                }
//            } // Devuelve el numero menor que seria la licencia de mas categoria
            return Collections.min(list);

        } catch (NoSuchElementException ex) {
            Log.e("Utils", "No hay categoria maxima que comprobar", ex);
            return -1;
        } catch (Exception ex) {
            Log.e("Utils", "Fallo comprobando categoría maxima", ex);
            return -1;
        }
    }

    /**
     * Método que devuelve el id (posicion en el Array) de la categoria introducida como parametro.
     *
     * @param context Contexto necesario para acceder a getResources
     * @param cat     Categoria a buscar en el array de categorias.
     * @return
     */
    private static int getCategoriaId(Context context, int cat) {
        String[] categorias = context.getResources().getStringArray(R.array.categorias);
        for (int i = 0; i < categorias.length; i++) {
            if (i == cat) {
                return cat;
            }
        }
        return -1;
    }

    /**
     * Metodo que devuelve el numero de guias de tipo F creadas
     *
     * @param context Necesario para acceder a getResources
     * @return tamaño de la lista de guias
     */
    public static int getNumGuias(Context context) {
        ArrayList<String> list = new ArrayList<>();
//        for (Property property : FragmentMainContent.properties) {
//            String licenseName = Utils.getStringLicenseFromId(context, (int) property.getTipoLicencia());
//            if (licenseName.equals("F - Tiro olimpico")) {
//                list.add(property.getNumGuia());
//            }
//        }
        return list.size();
    }

    /**
     * Método para obtener un anuncion en función de la variable de firebase database.
     *
     * @param view Vista donde se localiza el anuncio
     * @return Retorna el AdRequest done se encuentra que se visualizará en la view en caso de estar activa
     */
    public static AdRequest getAdRequest(final View view) {
//        // Read from the database
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
        return new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                .addTestDevice("19DFD6D99DFA16A1568E51C0698B3E2F")  // Alberto device ID
                .addTestDevice("01D5A4AECD505D328B19CE0B8D462CB4")  // David device ID
                .build();
    }

    /**
     * Metodo para obtener los recursos de las armas en funcion del tipo de licencia y su tipo de arma.
     *
     * @param tipoLicencia Tipo de licencia
     * @param tipoArma     Tipo de arma
     * @return Retorna el número entero perteneciente al recurso en cuestión.
     */
    public static int getResourceWeapon(int tipoLicencia, int tipoArma) {
        if (tipoLicencia == 1) { // B - Defensa
            switch (tipoArma) {
                case 0:
                    return R.drawable.ic_pistola;
                case 1:
                    return R.drawable.ic_revolver;
            }
        } else if (tipoLicencia == 2) { // C - Vigilante de seguridad / Escoltas
            return R.drawable.ic_revolver;
        } else if (tipoLicencia == 3) { // D - Caza mayor
            return R.drawable.ic_rifle;
        } else if (tipoLicencia == 4) { // E - Escopeta
            switch (tipoArma) {
                case 0:
                    return R.drawable.ic_shotgun;
                case 1:
                    return R.drawable.ic_rifle;
            }
        } else if (tipoLicencia == 6) { // AE - Autorización especial
            return R.drawable.ic_avancarga;
        } else if (tipoLicencia == 7) { // AER - Autorización especial replicas
            switch (tipoArma) {
                case 0:
                    return R.drawable.ic_pistola;
                case 1:
                    return R.drawable.ic_rifle;
                case 2:
                    return R.drawable.ic_revolver;
            }
        } else { // Resto Licencias
            switch (tipoArma) {
                case 0:
                    return R.drawable.ic_pistola;
                case 1:
                    return R.drawable.ic_shotgun;
                case 2:
                    return R.drawable.ic_rifle;
                case 3:
                    return R.drawable.ic_revolver;
                case 4:
                    return R.drawable.ic_avancarga;
            }
        }
        return -1;
    }

    /**
     * Método para obtener los recursos de los cartuchos en función del tipo de licencia y el tipo de armas.
     *
     * @param tipoLicencia Tipo de licencia
     * @param tipoArma     Tipo de arma
     * @return Retorna el número entero perteneciente al tipo de licencia y el tipo de arma.
     */
    public static int getResourceCartucho(int tipoLicencia, int tipoArma) {
        if (tipoLicencia == 1) { // B - Defensa
            switch (tipoArma) {
                case 0:
                    return R.drawable.ic_balas;
                case 1:
                    return R.drawable.ic_balas;
            }
        } else if (tipoLicencia == 2) { // C - Vigilante de seguridad / Escoltas
            return R.drawable.ic_balas;
        } else if (tipoLicencia == 3) { // D - Caza mayor
            return R.drawable.ic_balas_rifle;
        } else if (tipoLicencia == 4) { // E - Escopeta
            switch (tipoArma) {
                case 0:
                    return R.drawable.ic_cartuchos;
                case 1:
                    return R.drawable.ic_balas_rifle;
            }
        } else if (tipoLicencia == 6) { // AE - Autorización especial
            return R.drawable.ic_balas;
        } else if (tipoLicencia == 7) { // AER - Autorización especial replicas
            switch (tipoArma) {
                case 0:
                    return R.drawable.ic_balas;
                case 1:
                    return R.drawable.ic_balas_rifle;
                case 2:
                    return R.drawable.ic_balas;
            }
        } else { // Resto Licencias
            switch (tipoArma) {
                case 0:
                    return R.drawable.ic_balas;
                case 1:
                    return R.drawable.ic_cartuchos;
                case 2:
                    return R.drawable.ic_balas_rifle;
                case 3:
                    return R.drawable.ic_balas;
                case 4:
                    return R.drawable.ic_balas;
            }
        }
        return -1;
    }

    /**
     * Obtención de la cuenta principal del dispositivo para usarlo como usuario
     *
     * @param context
     * @return Email de la primera cuenta de google registrada en el dispositivo
     */
    static String getUserEmail(Context context) {
        String email = "";
        try {
            Account[] accounts = AccountManager.get(context).getAccountsByType("com.google");
            if (accounts.length > 0) {
                email = accounts[0].name;
                for (Account account : accounts) {
                    Log.i("Email_accounts", "Type => Account Name: " + account.name + " - Account Type: " + account.type);
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return email;
    }

    public static Date getDateFromString(String fecha) {
        Date date = new Date();
        try {
            date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fecha);
        } catch (Exception ex) {
            Log.e("Utils", "Fallo en el método: getDateFromString", ex);
        }
        return date;
    }

    static boolean isGooglePlayServicesAvailable(Context context) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        return resultCode == ConnectionResult.SUCCESS;
    }


    static void saveBitmapToFile(Bitmap imageBitmap) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(fileImagePath);
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 70, out); // imageBitmap is your Bitmap instance
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Función para subir las imagenes a Firebase
     *
     * @param storage       Instancia de la bbdd de firebase
     * @param imageBitmap   Bitmap de la imagen (No es necesaria)
     * @param fileImagePath Ruta de la imagen local en el dispositivo
     * @param userId        Id del usuario para crear la carpeta con su imagen
     */
    static void saveBitmapToFirebase(
            FirebaseStorage storage, Bitmap imageBitmap, String fileImagePath, @NonNull String userId) {

        File file = new File(fileImagePath);

        StorageReference riversRef = storage.getReference()
                .child("UserImages").child(userId + "/" + file.getName());
//        UploadTask uploadTask = riversRef.putFile(Uri.fromFile(file));
        UploadTask uploadTask = riversRef.putBytes(bitmapToByteArray(imageBitmap));

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.e("UploadImage", "Fallo en la subida de las imagenes a Firebase", exception);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getUploadSessionUri();
                Log.i("UploadImage", "Imagen subida: " + downloadUrl);
            }
        });
    }

    public static Bitmap resizeImage(Bitmap original, ImageView view) {
        // Get the dimensions of the View
        int targetW, targetH;
        if (view != null /*&& view.getWidth()!=0 && view.getHeight()!=0*/) {
            targetW = view.getLayoutParams().width;// getWidth();
            targetH = view.getLayoutParams().height;
        } else {
            targetW = 300;
            targetH = 200;
        }
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

//        BitmapFactory.decodeFile(FragmentMainActivity.fileImagePath, bmOptions);
        byte[] imageByte = bitmapToByteArray(original);
        BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

//        Bitmap bitmap = BitmapFactory.decodeFile(FragmentMainActivity.fileImagePath, bmOptions);
        return BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length, bmOptions);
//        view.setImageBitmap(bitmap);
    }

    /**
     * Metodo para convertir un Bitmap en un byte[]
     *
     * @param image Imagen
     * @return Byte[] de la imagen
     */
    private static byte[] bitmapToByteArray(Bitmap image) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 70 /*ignored for PNG*/, bos);
        return bos.toByteArray();
    }

    /**
     * Indica si el dispositivo esta conectado a internet
     *
     * @param context Contexto de la aplicación
     * @return true si tiene conexión a internet, false en caso contrario
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Método que lanza el intent de una imagen pasando el bitmap de la imagen
     *
     * @param context  Contexto
     * @param bitmap   Id del recurso que queremos mostrar
     * @param fileName Nombre de la imagen que vamos a guardar en el dispositivo
     */
    public static void showImage(Context context, Bitmap bitmap, String fileName) {

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        File f = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + fileName + ".jpg");

        try {
            //noinspection ResultOfMethodCallIgnored
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Uri path = FileProvider.getUriForFile(context, "al.ahgitdevelopment.municion.fileprovider", f);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(path, "image/*");
        context.startActivity(intent);
    }


    /**
     * Método que lanza el intent de una imagen pasando la ruta a la imagen
     *
     * @param context   Contexto
     * @param imagePath Id del recurso que queremos mostrar
     */
    public static void showImage(Context context, String imagePath) {

        Uri path = FileProvider.getUriForFile(context, "al.ahgitdevelopment.municion.fileprovider", new File(imagePath));
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(path, "image/*");
        context.startActivity(intent);
    }
}
