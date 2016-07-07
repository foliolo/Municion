package al.ahgitdevelopment.municion;

import android.content.Context;
import android.support.v4.app.FragmentActivity;

/**
 * Created by ahidalgog on 07/07/2016.
 */
public final class Utils {

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
     * Método que devuelve el string de la licencia introduciendo el id (posicion en el array de licencias)
     *
     * @param activity
     * @param tipo
     * @return
     */
    public static String getStringLicenseFromId(FragmentActivity activity, int tipo) {
        return activity.getResources().getStringArray(R.array.tipo_licencias)[tipo];
    }
}
