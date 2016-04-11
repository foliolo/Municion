package al.ahgitdevelopment.municion;

import android.content.Context;
import android.widget.ArrayAdapter;

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
}
