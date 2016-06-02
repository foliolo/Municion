package al.ahgitdevelopment.municion.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import al.ahgitdevelopment.municion.DataBaseSQLiteHelper;
import al.ahgitdevelopment.municion.R;

/**
 * Created by Alberto on 15/04/2016.
 */
public class GuiaCursorAdapter extends CursorAdapter {

    public GuiaCursorAdapter(Context context, Cursor c, int flag) {
        super(context, c, flag);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView imagen = (ImageView) view.findViewById(R.id.imageArma);
        TextView apodo = (TextView) view.findViewById(R.id.item_apodo_guia);
        TextView cupo = (TextView) view.findViewById(R.id.item_cupo_guia);
        TextView gastado = (TextView) view.findViewById(R.id.item_gastados_guia);

//        if (!cursor.getBlob(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_GUIA_IMAGEN)))
//            imagen.
//        )

        apodo.setText(cursor.getString(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_GUIA_APODO)));
        int numCupo = cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_GUIA_CUPO));
        int numGastado = cursor.getInt(cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_GUIA_GASTADO));
        cupo.setText(new StringBuilder().append(numCupo));
        gastado.setText(new StringBuilder().append((1.0 * numGastado / numCupo) * 100 + "%"));

//        Bitmap bitmap = BitmapFactory.decodeByteArray((byte[]) cursor.getBlob(
//                cursor.getColumnIndex(DataBaseSQLiteHelper.KEY_GUIA_IMAGEN)));

//        imagen.setImageBitmap(bitmap);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.guia_item, null, false);
        return view;
    }


//    private byte[] getLogoImage(String url) {
//        try {
//            InputStream is = ucon.getInputStream();
//            BufferedInputStream bis = new BufferedInputStream(is);
//
//            ByteArrayBuffer baf = new ByteArrayBuffer(500);
//            int current = 0;
//            while ((current = bis.read()) != -1) {
//                baf.append((byte) current);
//            }
//
//            return baf.toByteArray();
//        } catch (Exception e) {
//            Log.d("ImageManager", "Error: " + e.toString());
//        }
//        return null;
//    }
}
