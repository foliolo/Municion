package al.ahgitdevelopment.municion;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by Alberto on 28/08/2016.
 */
public class ListNotificationDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(android.R.layout.list_content, null);

        try {
            ArrayAdapter adapter = new ArrayAdapter(getActivity(), R.layout.notification_data_item, R.id.item_nombre_licencia, new Object[]{}) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView nombre = view.findViewById(R.id.item_nombre_licencia);
                    TextView numero = view.findViewById(R.id.item_num_licencia);
                    TextView fecha = view.findViewById(R.id.item_license_expiry_date);

                    if (position % 2 == 0)
                        view.setBackgroundColor(0xE7DFEBFF);

//                    nombre.setText(Utils.listNotificationData.get(position).getLicencia());
//                    numero.setText(Utils.listNotificationData.get(position).getId());
//                    fecha.setText(Utils.listNotificationData.get(position).getFecha());

                    return view;
                }
            };

            // Set custom view and custom adapter
            builder.setView(view)
                    .setAdapter(adapter, null)
                    .setTitle(getString(R.string.pref_list_notification))
                    .setPositiveButton(android.R.string.ok, null);

        } catch (Exception ex) {
            Log.wtf(getActivity().getPackageName(), "Fallo al listar las notificaciones", ex);
        }
        return builder.create();
    }
}
