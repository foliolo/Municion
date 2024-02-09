package al.ahgitdevelopment.municion.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import al.ahgitdevelopment.municion.R;
import al.ahgitdevelopment.municion.databases.DataBaseSQLiteHelper;

/**
 * Created by Alberto on 11/04/2016.
 */
public class CompraCursorAdapter extends CursorAdapter {

    public CompraCursorAdapter(Context context, Cursor c, int flag) {
        super(context, c, flag);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView imagen = view.findViewById(R.id.imageMunicion);
        TextView calibre = view.findViewById(R.id.item_calibre_compra);
        TextView unidades = view.findViewById(R.id.item_unidades_compra);
        TextView precio = view.findViewById(R.id.item_precio_compra);
        TextView year = view.findViewById(R.id.item_year_compra);

//        imagen.setImageResource(R.drawable.municion1);
        calibre.setText(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_COMPRA_CALIBRE1)));
        unidades.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_COMPRA_UNIDADES))));
        precio.setText(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_COMPRA_PRECIO)) + "€");
        year.setText(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_COMPRA_FECHA)));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.compra_item, null, false);
        return view;
    }
}