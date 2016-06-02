package al.ahgitdevelopment.municion.Adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import al.ahgitdevelopment.municion.DataModel.Licencia;
import al.ahgitdevelopment.municion.R;

/**
 * Created by Alberto on 28/05/2016.
 */
public class LicenciaArrayAdapter extends ArrayAdapter<Licencia> {

    public LicenciaArrayAdapter(Context context, int resource, List<Licencia> licencias) {
        super(context, resource, licencias);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Licencia licencia = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.licencia_item, parent, false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            convertView.setBackground(getContext().getDrawable(R.drawable.selector));
        }

        TextView tipo = (TextView) convertView.findViewById(R.id.item_tipo_licencia);
        TextView numLicencia = (TextView) convertView.findViewById(R.id.item_num_licencia);
        TextView expedicion = (TextView) convertView.findViewById(R.id.item_expedicion_licencia);
        TextView caducidad = (TextView) convertView.findViewById(R.id.item_caducidad_licencia);

        // El último elemento es el libro de coleccionista y no tiene "-"
        int lengthArrayLicencias = getContext().getResources().getTextArray(R.array.tipo_licencias).length - 1;
        if (licencia.getTipo().equals(getContext().getResources().getTextArray(R.array.tipo_licencias)[lengthArrayLicencias])) {
            tipo.setText(licencia.getTipo());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                tipo.setTextAppearance(android.R.style.TextAppearance_Medium);
            else
                tipo.setTextAppearance(getContext(), android.R.style.TextAppearance_Medium);
        } else {
            tipo.setText(licencia.getTipo().split("-")[0].trim());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                tipo.setTextAppearance(android.R.style.TextAppearance_Large);
            else
                tipo.setTextAppearance(getContext(), android.R.style.TextAppearance_Large);
        }
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
