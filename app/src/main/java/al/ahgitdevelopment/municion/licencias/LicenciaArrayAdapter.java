package al.ahgitdevelopment.municion.licencias;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import al.ahgitdevelopment.municion.R;
import al.ahgitdevelopment.municion.datamodel.Licencia;

/**
 * Created by Alberto on 28/05/2016.
 */
@SuppressWarnings("FieldCanBeLocal")
public class LicenciaArrayAdapter extends ArrayAdapter<Licencia> {
    private TextView tipo;
    private TextView lblTipoPermisoConduccion;
    private TextView tipoPermisoConduccion;
    private TextView lblEdad;
    private TextView edad;
    private TextView lblNumLicencia;
    private TextView numLicencia;
    private TextView expedicion;
    private LinearLayout layoutFechaCaducidad;
    private TextView caducidad;
    private TextView lblAbonado;
    private TextView numAbonado;
    private TextView lblNumSeguro;
    private TextView numSeguro;
    private LinearLayout layoutCCAA;
    private TextView autonomia;
    private TextView lblAutonomia;
    private LinearLayout layoutEscala;
    private TextView escala;
    private TextView categoria;
    private TextView lblCategoria;

    public LicenciaArrayAdapter(Context context, int resource, List<Licencia> licencias) {
        super(context, resource, licencias);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Get the data item for this position
        Licencia licencia = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.licencia_item, parent, false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            convertView.setBackground(getContext().getDrawable(R.drawable.selector));
        }

        tipo = convertView.findViewById(R.id.item_tipo_licencia);
        lblTipoPermisoConduccion = convertView.findViewById(R.id.lbl_tipo_permiso_conducir);
        tipoPermisoConduccion = convertView.findViewById(R.id.item_tipo_permiso_conducir);
        lblEdad = convertView.findViewById(R.id.lbl_edad);
        edad = convertView.findViewById(R.id.item_edad);
        lblNumLicencia = convertView.findViewById(R.id.lbl_num_licencia);
        numLicencia = convertView.findViewById(R.id.item_num_guia);
        expedicion = convertView.findViewById(R.id.item_expedicion_licencia);
        layoutFechaCaducidad = convertView.findViewById(R.id.layout_fecha_caducidad);
        caducidad = convertView.findViewById(R.id.item_caducidad_licencia);
        lblAbonado = convertView.findViewById(R.id.lbl_num_abonado);
        numAbonado = convertView.findViewById(R.id.item_num_abonado);
        lblNumSeguro = convertView.findViewById(R.id.lbl_num_poliza);
        numSeguro = convertView.findViewById(R.id.item_num_poliza);
        layoutCCAA = convertView.findViewById(R.id.layout_ccaa);
        autonomia = convertView.findViewById(R.id.item_ccaa);
        lblAutonomia = convertView.findViewById(R.id.form_lbl_ccaa);
        categoria = convertView.findViewById(R.id.item_categoria);
        lblCategoria = convertView.findViewById(R.id.form_lbl_categoria);
        layoutEscala = convertView.findViewById(R.id.layout_escala);
        escala = convertView.findViewById(R.id.item_escala);

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

        String nombreLicencia = licencia.getNombre();
        if (nombreLicencia.contains(" - ")) {
            tipo.setText(nombreLicencia.split("-")[0].trim());
        } else {
            tipo.setText(nombreLicencia);
        }

        //FIXME: Arreglar esta excepción. Ocurre al cambiar una licencia "Permiso de conducir" a cualquier otra
        try {
            if (nombreLicencia.equals("Permiso Conducir")) {
                tipoPermisoConduccion.setText(getContext().getResources().getStringArray(R.array.tipo_permiso_conducir)[licencia.getTipoPermisoConduccion()]);
                lblTipoPermisoConduccion.setVisibility(View.VISIBLE);
                tipoPermisoConduccion.setVisibility(View.VISIBLE);
                edad.setText(licencia.getEdad() + "");
                lblEdad.setVisibility(View.VISIBLE);
                edad.setVisibility(View.VISIBLE);
                lblNumLicencia.setText(R.string.lbl_num_dni);
            } else {
                lblTipoPermisoConduccion.setVisibility(View.GONE);
                tipoPermisoConduccion.setVisibility(View.GONE);
                lblEdad.setVisibility(View.GONE);
                edad.setVisibility(View.GONE);
                lblNumLicencia.setText(R.string.lbl_num_licencia);
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            Log.e(getContext().getPackageName(), "Error al cambiar una permiso de conducir a otra licencia", ex);
        }
        numLicencia.setText(licencia.getNumLicencia());

        if (licencia.getFechaExpedicion() != null)
            expedicion.setText(licencia.getFechaExpedicion());

        //La licencia A no tiene fecha de caducidad pero si escala del agente
        if (licencia.getTipo() == 0) {
            layoutFechaCaducidad.setVisibility(View.GONE);
            layoutEscala.setVisibility(View.VISIBLE);
//            escala.setText(licencia.getStringEscala(getContext()));
        } else {
            layoutFechaCaducidad.setVisibility(View.VISIBLE);
            layoutEscala.setVisibility(View.GONE);
            if (licencia.getFechaCaducidad() != null)
                caducidad.setText(licencia.getFechaCaducidad());
        }

        if (licencia.getNumAbonado() > 0) {
            numAbonado.setText(licencia.getNumAbonado() + "");
            lblAbonado.setVisibility(View.VISIBLE);
            numAbonado.setVisibility(View.VISIBLE);
        } else {
            lblAbonado.setVisibility(View.GONE);
            numAbonado.setVisibility(View.GONE);
        }

        if (licencia.getNumSeguro() != null && !licencia.getNumSeguro().isEmpty() && !licencia.getNumSeguro().equals("null")) {
            lblNumSeguro.setVisibility(View.VISIBLE);
            numSeguro.setText(licencia.getNumSeguro() + "");
            numSeguro.setVisibility(View.VISIBLE);
        } else {
            lblNumSeguro.setVisibility(View.GONE);
            numSeguro.setText("");
            numSeguro.setVisibility(View.GONE);
        }

        if (licencia.getAutonomia() >= 0) {
            autonomia.setText(getContext().getResources().getStringArray(R.array.ccaa)[licencia.getAutonomia()]);
            layoutCCAA.setVisibility(View.VISIBLE);
        } else {
            layoutCCAA.setVisibility(View.GONE);
        }

        if (licencia.getCategoria() >= 0) {
            categoria.setText(getContext().getResources().getStringArray(R.array.categorias)[licencia.getCategoria()]);
            lblCategoria.setVisibility(View.VISIBLE);
            categoria.setVisibility(View.VISIBLE);
        } else {
            lblCategoria.setVisibility(View.GONE);
            categoria.setVisibility(View.GONE);
        }

        // Return the completed view to render on screen
        return convertView;
    }
}
