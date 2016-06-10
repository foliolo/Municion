package al.ahgitdevelopment.municion.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import al.ahgitdevelopment.municion.DataBaseSQLiteHelper;
import al.ahgitdevelopment.municion.R;

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
        TextView numLicencia = (TextView) view.findViewById(R.id.item_num_guia);
        TextView expedicion = (TextView) view.findViewById(R.id.item_expedicion_licencia);
        TextView caducidad = (TextView) view.findViewById(R.id.item_caducidad_licencia);

        tipo.setText(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_TIPO)));
        numLicencia.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_NUM_LICENCIA))));
        expedicion.setText(String.valueOf(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_FECHA_EXPEDICION))));
        caducidad.setText(String.valueOf(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_FECHA_CADUCIDAD))));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.licencia_item, null, false);
        return view;
    }
}