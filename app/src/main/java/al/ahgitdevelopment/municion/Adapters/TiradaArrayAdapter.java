package al.ahgitdevelopment.municion.Adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import al.ahgitdevelopment.municion.DataModel.Tirada;
import al.ahgitdevelopment.municion.R;

/**
 * Created by Alberto on 28/05/2016.
 */
public class TiradaArrayAdapter extends ArrayAdapter<Tirada> {
    Context context;

    private TextView descripcion;
    private TextView rango;
    private TextView fecha;
    private TextView puntuacion;

    public TiradaArrayAdapter(Context context, int resource, List<Tirada> tiradas) {
        super(context, resource, tiradas);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Tirada tirada = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.tirada_item, parent, false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            convertView.setBackground(getContext().getDrawable(R.drawable.selector));
        }

        descripcion = (TextView) convertView.findViewById(R.id.item_descripcion_tirada);
        rango = (TextView) convertView.findViewById(R.id.item_rango_tirada);
        fecha = (TextView) convertView.findViewById(R.id.item_fecha_tirada);
        puntuacion = (TextView) convertView.findViewById(R.id.item_puntuacion_tirada);

        descripcion.setText(tirada.getDescripcion());
        rango.setText(tirada.getRango());
        fecha.setText(tirada.getFecha());
        puntuacion.setText(String.valueOf(tirada.getPuntuacion()));

        // Return the completed view to render on screen
        return convertView;
    }
}
