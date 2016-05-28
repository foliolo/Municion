package al.ahgitdevelopment.municion;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Alberto on 11/04/2016.
 */
public class CompraCursorAdapter extends CursorAdapter {

    public CompraCursorAdapter(Context context, Cursor c, int flag) {
        super(context, c, flag);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView imagen = (ImageView) view.findViewById(R.id.imageMunicion);
        TextView calibre = (TextView) view.findViewById(R.id.item_calibre_compra);
        TextView unidades = (TextView) view.findViewById(R.id.item_unidades_compra);
        TextView precio = (TextView) view.findViewById(R.id.item_precio_compra);

        imagen.setImageResource(R.drawable.municion1);
        calibre.setText(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_COMPRA_CALIBRE1)));
        unidades.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_COMPRA_UNIDADES))));
        precio.setText(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_COMPRA_PRECIO)) + "€");
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.compra_item, null, false);
        return view;
    }
}