package al.ahgitdevelopment.municion;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by Alberto on 24/05/2016.
 */
public class LicenciaCursorAdapter extends CursorAdapter {

    public LicenciaCursorAdapter(Context context, Cursor c, int flag) {
        super(context, c, flag);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tipo = (TextView) view.findViewById(R.id.item_tipo_licencia);
        TextView numLicencia = (TextView) view.findViewById(R.id.item_num_licencia);
        TextView expedicion = (TextView) view.findViewById(R.id.item_expedicion_licencia);
        TextView caducidad = (TextView) view.findViewById(R.id.item_caducidad_licencia);

        tipo.setText(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_TIPO)));
        numLicencia.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_NUM_LICENCIA))));

//        TODO: Arreglar
//        Calendar t = new GregorianCalendar();
//        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
//        java.util.Date dt = null;

//        try {
//            dt = sdf.parse(String.valueOf(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_FECHA_EXPEDICION)))); //replace 4 with the column index
//            expedicion.setText(dt.getDate());
//
//            dt = sdf.parse(String.valueOf(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_FECHA_CADUCIDAD)))); //replace 4 with the column indexK
////            t.setTime(dt);
//            caducidad.setText(dt.getMonth());
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.licencia_item, null, false);
        return view;
    }
}
