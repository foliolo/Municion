package al.ahgitdevelopment.municion;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Alberto on 11/04/2016.
 */
public class GuiaAdapter extends ArrayAdapter<Guia> {
    Context context;

    public GuiaAdapter(Context activity, ArrayList<Guia> guias) {
        super(activity, R.layout.guia_item, guias);
        context = activity;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public Guia getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Guia item = getItem(position);

        if (convertView == null)
            convertView = LayoutInflater.from(context).inflate(R.layout.guia_item, null);

        TextView nombreGuia = (TextView) convertView.findViewById(R.id.item_nombre_guia);
        TextView municion = (TextView) convertView.findViewById(R.id.item_municion_consumida);
        TextView porcentaje = (TextView) convertView.findViewById(R.id.item_porcentaje);

        nombreGuia.setText(getItem(position).getNombreArma());
        municion.setText(item.getCartuchosGastados() + "\\" + item.getCartuchosTotales());
        float percentValue = (float) item.cartuchosGastados * 100 / item.cartuchosTotales;
        porcentaje.setText(percentValue + "%");

        return convertView;
    }
}
