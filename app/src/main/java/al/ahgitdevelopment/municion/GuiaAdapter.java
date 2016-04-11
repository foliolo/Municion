package al.ahgitdevelopment.municion;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Created by Alberto on 11/04/2016.
 */
public class GuiaAdapter extends ArrayAdapter<Guia> {
    Context context;

    public GuiaAdapter(FragmentActivity activity, ArrayList<Guia> guias) {
        super(activity, R.layout.guia_item, guias);
        context = activity;
    }
}
