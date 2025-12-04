package al.ahgitdevelopment.municion;

import android.util.Log;

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
}
