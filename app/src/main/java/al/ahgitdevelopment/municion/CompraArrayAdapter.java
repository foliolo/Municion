package al.ahgitdevelopment.municion;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Alberto on 28/05/2016.
 */
public class CompraArrayAdapter extends ArrayAdapter<Compra> {
    public CompraArrayAdapter(Context context, int resource, List<Compra> compras) {
        super(context, resource, compras);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Compra compra = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.compra_item, parent, false);
        }

        ImageView imagen = (ImageView) convertView.findViewById(R.id.imageMunicion);
        TextView calibre = (TextView) convertView.findViewById(R.id.item_calibre_compra);
        TextView unidades = (TextView) convertView.findViewById(R.id.item_unidades_compra);
        TextView precio = (TextView) convertView.findViewById(R.id.item_precio_compra);

        if (compra.getImagen() != null)
            imagen.setImageBitmap(compra.getImagen());
        else
            imagen.setImageResource(R.drawable.municion1);

        if (compra.getCalibre2() == null || "".equals(compra.getCalibre2()))
            calibre.setText(compra.getCalibre1());
        else
            calibre.setText(compra.getCalibre1() + " / " + compra.getCalibre2());

        unidades.setText(compra.getUnidades() + "");
        precio.setText(compra.getPrecio() + "€");

        // Return the completed view to render on screen
        return convertView;
    }
}