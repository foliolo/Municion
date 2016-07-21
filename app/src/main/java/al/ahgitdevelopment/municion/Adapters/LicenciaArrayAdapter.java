package al.ahgitdevelopment.municion.Adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import al.ahgitdevelopment.municion.DataModel.Licencia;
import al.ahgitdevelopment.municion.R;

/**
 * Created by Alberto on 28/05/2016.
 */
public class LicenciaArrayAdapter extends ArrayAdapter<Licencia> {

    TextView tipo;
    TextView lblTipoPermisoConduccion;
    TextView tipoPermisoConduccion;
    TextView numLicencia;
    TextView expedicion;
    TextView caducidad;
    TextView lblAbonado;
    TextView numAbonado;
    TextView lblNumSeguro;
    TextView numSeguro;
    LinearLayout layoutCCAA;
    TextView autonomia;
    TextView lblAutonomia;

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

        tipo = (TextView) convertView.findViewById(R.id.item_tipo_licencia);
        lblTipoPermisoConduccion = (TextView) convertView.findViewById(R.id.lbl_tipo_permiso_conducir);
        tipoPermisoConduccion = (TextView) convertView.findViewById(R.id.item_tipo_permiso_conducir);
        numLicencia = (TextView) convertView.findViewById(R.id.item_num_guia);
        expedicion = (TextView) convertView.findViewById(R.id.item_expedicion_licencia);
        caducidad = (TextView) convertView.findViewById(R.id.item_caducidad_licencia);
        lblAbonado = (TextView) convertView.findViewById(R.id.lbl_num_abonado);
        numAbonado = (TextView) convertView.findViewById(R.id.item_num_abonado);
        lblNumSeguro = (TextView) convertView.findViewById(R.id.lbl_num_poliza);
        numSeguro = (TextView) convertView.findViewById(R.id.item_num_poliza);
        layoutCCAA = (LinearLayout) convertView.findViewById(R.id.layout_ccaa);
        autonomia = (TextView) convertView.findViewById(R.id.item_ccaa);
        lblAutonomia = (TextView) convertView.findViewById(R.id.form_lbl_ccaa);

//        // El último elemento es el libro de coleccionista y no tiene "-"
//        int lengthArrayLicencias = getContext().getResources().getTextArray(R.array.tipo_licencias).length - 1;
//
//        if (licencia.getTipo() == lengthArrayLicencias) {
//            tipo.setText(getContext().getResources().getTextArray(R.array.tipo_licencias)[licencia.getTipo()].toString());
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
//                tipo.setTextAppearance(android.R.style.TextAppearance_Medium);
//            else
//                tipo.setTextAppearance(getContext(), android.R.style.TextAppearance_Medium);
//        } else {
//            tipo.setText(getContext().getResources().getTextArray(R.array.tipo_licencias)[licencia.getTipo()].toString().split("-")[0].trim());
//            autonomia.setText(getContext().getResources().getTextArray(R.array.ccaa)[licencia.getAutonomia()].toString());
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
//                tipo.setTextAppearance(android.R.style.TextAppearance_Large);
//            else
//                tipo.setTextAppearance(getContext(), android.R.style.TextAppearance_Large);
//        }


//        tipo.setText(getContext().getResources().getTextArray(R.array.tipo_licencias)[licencia.getTipo()].toString());

        String nombreLicencia = licencia.getNombre(getContext());
        if (nombreLicencia.contains(" - ")) {
            tipo.setText(nombreLicencia.split("-")[0].trim());
        } else {
            tipo.setText(nombreLicencia);
        }

        if (licencia.getNombre().equals("Permiso Conducir")) {
            tipoPermisoConduccion.setText(getContext().getResources().getStringArray(R.array.tipo_permiso_conducir)[licencia.getTipoPermisoConduccion()]);
            lblTipoPermisoConduccion.setVisibility(View.VISIBLE);
            tipoPermisoConduccion.setVisibility(View.VISIBLE);
        } else {
            lblTipoPermisoConduccion.setVisibility(View.GONE);
            tipoPermisoConduccion.setVisibility(View.GONE);
        }

//        if (licencia.getTipoPermisoConduccion() >= 0) {
//            tipoPermisoConduccion.setText(getContext().getResources().getStringArray(R.array.tipo_permiso_conducir)[licencia.getTipoPermisoConduccion()]);
//            lblTipoPermisoConduccion.setVisibility(View.VISIBLE);
//            tipoPermisoConduccion.setVisibility(View.VISIBLE);
//        } else {
//            lblTipoPermisoConduccion.setVisibility(View.GONE);
//            tipoPermisoConduccion.setVisibility(View.GONE);
//        }

        numLicencia.setText(String.valueOf(licencia.getNumLicencia()));

        if (licencia.getFechaExpedicion() != null)
            expedicion.setText(new SimpleDateFormat("dd/MM/yyyy").format(licencia.getFechaExpedicion().getTime()));
        if (licencia.getFechaCaducidad() != null)
            caducidad.setText(new SimpleDateFormat("dd/MM/yyyy").format(licencia.getFechaCaducidad().getTime()));

        if (licencia.getNumAbonado() > 0) {
            numAbonado.setText(licencia.getNumAbonado() + "");
            lblAbonado.setVisibility(View.VISIBLE);
            numAbonado.setVisibility(View.VISIBLE);
        } else {
            lblAbonado.setVisibility(View.GONE);
            numAbonado.setVisibility(View.GONE);
        }

        if (licencia.getNumSeguro() != null && !licencia.getNumSeguro().isEmpty()) {
            numSeguro.setText(licencia.getNumSeguro() + "");
            lblNumSeguro.setVisibility(View.VISIBLE);
            numSeguro.setVisibility(View.VISIBLE);
        } else {
            lblNumSeguro.setVisibility(View.GONE);
            numSeguro.setVisibility(View.GONE);
        }

        if (licencia.getAutonomia() >= 0) {
            autonomia.setText(getContext().getResources().getStringArray(R.array.ccaa)[licencia.getAutonomia()]);
            layoutCCAA.setVisibility(View.VISIBLE);
        } else {
            layoutCCAA.setVisibility(View.GONE);
        }

        // Return the completed view to render on screen
        return convertView;
    }
}
