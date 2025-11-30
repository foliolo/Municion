package al.ahgitdevelopment.municion;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ahidalgog on 07/07/2016.
 */
public final class Utils {

    public static Date getDateFromString(String fecha) {
        Date date = new Date();
        try {
            date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fecha);
        } catch (Exception ex) {
            Log.e("Utils", "Fallo en el método: getDateFromString", ex);
        }
        return date;
    }

    /**
     * Metodo que lanza el intent de una imagen pasando el bitmap de la imagen
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
     * Metodo que lanza el intent de una imagen pasando la ruta a la imagen
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
