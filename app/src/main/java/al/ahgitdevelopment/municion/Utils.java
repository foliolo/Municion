package al.ahgitdevelopment.municion;

import android.content.Context;

import java.util.ArrayList;

import al.ahgitdevelopment.municion.DataModel.Guia;
import al.ahgitdevelopment.municion.DataModel.Licencia;

/**
 * Created by ahidalgog on 07/07/2016.
 */
public final class Utils {

    public static CharSequence[] getLicenseName(Context context) {
        ArrayList<String> list = new ArrayList<>();
        for (Licencia licencia : FragmentMainActivity.licencias) {
            String licenseName = Utils.getStringLicenseFromId(context, licencia.getTipo());
            if (!licenseName.equals("Autonómica de Caza") &&
                    !licenseName.equals("Autonómica de Pesca") &&
                    !licenseName.equals("Federativa de tiro") &&
                    !licenseName.equals("Permiso Conducir"))
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
//        // Si no existen guias, podemos eliminar la licencia sin problemas
//        if(FragmentMainActivity.guias.size() == 0)
//            return false;

        // Si alguna guía tiene el id de la licencia que queremos borrar, no se podra eliminar la licencia
        for (Guia guia : FragmentMainActivity.guias) {
            if (guia.getTipoLicencia() == FragmentMainActivity.licencias.get(position).getTipo()) {
                return true;
            }
        }

        return false;
    }
}
