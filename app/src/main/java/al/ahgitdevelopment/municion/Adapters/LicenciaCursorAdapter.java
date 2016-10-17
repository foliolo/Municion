package al.ahgitdevelopment.municion.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import al.ahgitdevelopment.municion.DataBases.DataBaseSQLiteHelper;
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
        TextView numAbonado = (TextView) view.findViewById(R.id.item_num_abonado);
        TextView numSeguro = (TextView) view.findViewById(R.id.item_num_poliza);
        TextView autonomia = (TextView) view.findViewById(R.id.item_ccaa);
        TextView tipoPermisoConducir = (TextView) view.findViewById(R.id.item_tipo_permiso_conducir);
        TextView edad = (TextView) view.findViewById(R.id.item_edad);
        TextView categoria = (TextView) view.findViewById(R.id.item_categoria);

        tipo.setText(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_TIPO)));
        numLicencia.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_NUM_LICENCIA))));
        expedicion.setText(String.valueOf(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_FECHA_EXPEDICION))));
        caducidad.setText(String.valueOf(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_FECHA_CADUCIDAD))));
        numAbonado.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_NUM_ABONADO))));
        numSeguro.setText(String.valueOf(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_NUM_SEGURO))));
        autonomia.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_AUTONOMIA))));
        tipoPermisoConducir.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_TIPO_PERMISO_CONDUCCION))));
        edad.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_EDAD))));
        categoria.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_LICENCIAS_CATEGORIA))));

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.licencia_item, null, false);
        return view;
    }
}
