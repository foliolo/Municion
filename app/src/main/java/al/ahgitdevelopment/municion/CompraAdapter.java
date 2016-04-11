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
public class CompraAdapter extends ArrayAdapter<Compra> {
    private Context context;

    public CompraAdapter(Context context, ArrayList<Compra> resource) {
        super(context, R.layout.compra_item, resource);
        this.context = context;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public Compra getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Compra item = getItem(position);

        if (convertView == null)
            convertView = LayoutInflater.from(context).inflate(R.layout.compra_item, null);

        TextView nombreGuia = (TextView) convertView.findViewById(R.id.item_nombre_guia);
        TextView precio = (TextView) convertView.findViewById(R.id.item_precio);
        TextView cartuchosComprados = (TextView) convertView.findViewById(R.id.item_cartuchos_comprados);

        nombreGuia.setText(item.getNombreArma());
        precio.setText(String.format("%f€", item.getPrecio()));
        cartuchosComprados.setText(String.format("%s\\%", item.getCartuchosComprados()));

        return convertView;
    }
}
