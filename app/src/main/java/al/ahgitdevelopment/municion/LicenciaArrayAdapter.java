package al.ahgitdevelopment.municion;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Alberto on 28/05/2016.
 */
public class LicenciaArrayAdapter extends ArrayAdapter<Licencia> {
    private final List<Licencia> licencias;

    public LicenciaArrayAdapter(Context context, int resource, List<Licencia> licencias) {
        super(context, resource, licencias);
        this.licencias = licencias;
    }

//    @Override
//    public Licencia getItem(int position) {
//        Licencia licencia = new Licencia(licencias.get(position));
//        return licencia;
//    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Licencia licencia = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.licencia_item, parent, false);
        }

        TextView tipo = (TextView) convertView.findViewById(R.id.item_tipo_licencia);
        TextView numLicencia = (TextView) convertView.findViewById(R.id.item_num_licencia);
        TextView expedicion = (TextView) convertView.findViewById(R.id.item_expedicion_licencia);
        TextView caducidad = (TextView) convertView.findViewById(R.id.item_caducidad_licencia);

        tipo.setText(licencia.getTipo().split("-")[0].trim());
        numLicencia.setText(licencia.getNumLicencia() + "");
        if (licencia.getFechaCaducidad() != null)
            caducidad.setText(new SimpleDateFormat("dd/MM/yyyy").format(licencia.getFechaCaducidad()).toString());
        if (licencia.getFechaExpedicion() != null)
            expedicion.setText(new SimpleDateFormat("dd/MM/yyyy").format(licencia.getFechaExpedicion()).toString());
//            expedicion.setText(licencia.getFechaExpedicion());

        // Return the completed view to render on screen
        return convertView;
    }
}
