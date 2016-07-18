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
        TextView numLicencia = (TextView) convertView.findViewById(R.id.item_num_guia);
        TextView expedicion = (TextView) convertView.findViewById(R.id.item_expedicion_licencia);
        TextView caducidad = (TextView) convertView.findViewById(R.id.item_caducidad_licencia);
        TextView lblAbonado = (TextView) convertView.findViewById(R.id.lbl_num_abonado);
        TextView numAbonado = (TextView) convertView.findViewById(R.id.item_num_abonado);
        TextView lblNumSeguro = (TextView) convertView.findViewById(R.id.lbl_num_poliza);
        TextView numSeguro = (TextView) convertView.findViewById(R.id.item_num_poliza);
        TextView autonomia = (TextView) convertView.findViewById(R.id.item_ccaa);
        TextView lblAutonomia = (TextView) convertView.findViewById(R.id.form_lbl_ccaa);

        // El último elemento es el libro de coleccionista y no tiene "-"
        int lengthArrayLicencias = getContext().getResources().getTextArray(R.array.tipo_licencias).length - 1;

        if (licencia.getTipo() == lengthArrayLicencias) { 
            tipo.setText(getContext().getResources().getTextArray(R.array.tipo_licencias)[licencia.getTipo()].toString());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                tipo.setTextAppearance(android.R.style.TextAppearance_Medium);
            else
                tipo.setTextAppearance(getContext(), android.R.style.TextAppearance_Medium);
        } else {
            tipo.setText(getContext().getResources().getTextArray(R.array.tipo_licencias)[licencia.getTipo()].toString().split("-")[0].trim());
            autonomia.setText(getContext().getResources().getTextArray(R.array.ccaa)[licencia.getAutonomia()].toString());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                tipo.setTextAppearance(android.R.style.TextAppearance_Large);
            else
                tipo.setTextAppearance(getContext(), android.R.style.TextAppearance_Large);
        }
        numLicencia.setText(licencia.getNumLicencia() + "");
        if(licencia.getNumAbonado() != 0) {
            numAbonado.setText(licencia.getNumAbonado() + "");
        } else {
            numAbonado.setVisibility(View.GONE);
            lblAbonado.setVisibility(View.GONE);
        }
        if (licencia.getNumSeguro() != null && !licencia.getNumSeguro().isEmpty()) {
            numSeguro.setText(licencia.getNumSeguro() + "");
        } else {
            numSeguro.setVisibility(View.GONE);
            lblNumSeguro.setVisibility(View.GONE);
        }
        if (licencia.getAutonomia() == 0) {
            autonomia.setVisibility(View.GONE);
            lblAutonomia.setVisibility(View.GONE);
        }
        if (licencia.getFechaCaducidad() != null)
            caducidad.setText(new SimpleDateFormat("dd/MM/yyyy").format(licencia.getFechaCaducidad().getTime()));
        if (licencia.getFechaExpedicion() != null)
            expedicion.setText(new SimpleDateFormat("dd/MM/yyyy").format(licencia.getFechaExpedicion().getTime()));

        // Return the completed view to render on screen
        return convertView;
    }
}
