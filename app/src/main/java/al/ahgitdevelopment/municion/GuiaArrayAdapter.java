package al.ahgitdevelopment.municion;

import android.content.Context;
import android.os.Build;
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
public class GuiaArrayAdapter extends ArrayAdapter<Guia> {
    public GuiaArrayAdapter(Context context, int resource, List<Guia> guias) {
        super(context, resource, guias);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Guia guia = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.guia_item, parent, false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            convertView.setBackground(getContext().getDrawable(R.drawable.selector));
        }

        ImageView imagen = (ImageView) convertView.findViewById(R.id.imageArma);
        TextView apodo = (TextView) convertView.findViewById(R.id.item_apodo_guia);
        TextView cupo = (TextView) convertView.findViewById(R.id.item_cupo_guia);
        TextView gastado = (TextView) convertView.findViewById(R.id.item_gastados_guia);

        if (guia.getImagen() != null)
            imagen.setImageBitmap(guia.getImagen());
        else
            imagen.setImageResource(R.drawable.pistola);
        apodo.setText(guia.getApodo());
        cupo.setText(guia.getGastado() + "/" + guia.getCupo());
        gastado.setText(new StringBuilder().append((1.0 * guia.getGastado() / guia.getCupo()) * 100 + "%"));

        // Return the completed view to render on screen
        return convertView;
    }
}
