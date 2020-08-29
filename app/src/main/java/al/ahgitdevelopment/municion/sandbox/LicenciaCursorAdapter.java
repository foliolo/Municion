package al.ahgitdevelopment.municion.sandbox;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import al.ahgitdevelopment.municion.R;

import static al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_FECHA_CADUCIDAD;
import static al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_FECHA_EXPEDICION;
import static al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_NUM_LICENCIA;
import static al.ahgitdevelopment.municion.repository.dao.DbConstantsKt.KEY_LICENCIAS_NUM_SEGURO;

/**
 * Created by Alberto on 24/05/2016.
 */
public class LicenciaCursorAdapter extends CursorAdapter {

    public LicenciaCursorAdapter(Context context, Cursor c, int flag) {
        super(context, c, flag);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView numLicencia = view.findViewById(R.id.item_license_number);
        TextView expedicion = view.findViewById(R.id.item_license_issue_date);
        TextView caducidad = view.findViewById(R.id.item_license_expiry_date);
        TextView numSeguro = view.findViewById(R.id.item_license_insurance_number);

        numLicencia.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(KEY_LICENCIAS_NUM_LICENCIA))));
        expedicion.setText(String.valueOf(cursor.getString(cursor.getColumnIndex(KEY_LICENCIAS_FECHA_EXPEDICION))));
        caducidad.setText(String.valueOf(cursor.getString(cursor.getColumnIndex(KEY_LICENCIAS_FECHA_CADUCIDAD))));
        numSeguro.setText(String.valueOf(cursor.getString(cursor.getColumnIndex(KEY_LICENCIAS_NUM_SEGURO))));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.licencia_item, parent, false);
        return view;
    }
}
