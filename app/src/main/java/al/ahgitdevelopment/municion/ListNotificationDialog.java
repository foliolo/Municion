package al.ahgitdevelopment.municion;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.Objects;

import al.ahgitdevelopment.municion.datamodel.NotificationData;

/**
 * Created by Alberto on 28/08/2016.
 */
public class ListNotificationDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(android.R.layout.list_content, null);

        try {
            ArrayAdapter<NotificationData> adapter = new ArrayAdapter<NotificationData>(
                    requireContext(),
                    R.layout.notification_data_item,
                    R.id.item_nombre_licencia,
                    Utils.listNotificationData
            ) {
                @NonNull
                @Override
                public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView name = view.findViewById(R.id.item_nombre_licencia);
                    TextView number = view.findViewById(R.id.item_num_licencia);
                    TextView date = view.findViewById(R.id.item_caducidad_licencia);

                    if (position % 2 == 0)
                        view.setBackgroundColor(0xE7DFEBFF);

                    name.setText(Utils.listNotificationData.get(position).getLicencia());
                    number.setText(Utils.listNotificationData.get(position).getId());
                    date.setText(Utils.listNotificationData.get(position).getFecha());

                    return view;
                }
            };

            // Set custom view and custom adapter
            builder.setView(view)
                    .setAdapter(adapter, null)
                    .setTitle(requireContext().getString(R.string.pref_list_notification))
                    .setPositiveButton(android.R.string.ok, null);

        } catch (Exception ex) {
            Log.wtf(requireContext().getPackageName(), "Fallo al listar las notificaciones", ex);
        }
        return builder.create();
    }
}
