package al.ahgitdevelopment.municion;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by Alberto on 15/04/2016.
 */
public class GuiaCursorAdapter extends CursorAdapter {

    public GuiaCursorAdapter(Context context, Cursor c, int flag) {
        super(context, c, flag);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nombreGuia = (TextView) view.findViewById(R.id.item_nombre_guia);
        TextView municion = (TextView) view.findViewById(R.id.item_precio);
        TextView porcentaje = (TextView) view.findViewById(R.id.item_cartuchos_comprados);

        nombreGuia.setText(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_GUIA_NOMBRE)));

        String aux = cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_GUIA_CARTUCHOS_GASTADOS)) + "//" +
                cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_GUIA_CARTUCHOS_TOTALES));
        municion.setText(aux);

        float percentValue = (float) cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_GUIA_CARTUCHOS_GASTADOS)) * 100 /
                cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_GUIA_CARTUCHOS_TOTALES));
        porcentaje.setText(percentValue + "%");
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.guia_item, null, false);
        return view;
    }
}
