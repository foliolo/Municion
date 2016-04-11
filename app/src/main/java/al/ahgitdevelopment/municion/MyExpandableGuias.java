package al.ahgitdevelopment.municion;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Alberto on 29/03/2016.
 */
public class MyExpandableGuias extends android.widget.BaseExpandableListAdapter {

    private Context context;
    private Map<String, ArrayList<Guia>> grupoGuias;

    public MyExpandableGuias(Context context, Map<String, ArrayList<Guia>> grupoGuias) {
        this.context = context;
        this.grupoGuias = grupoGuias;
    }

    @Override
    public int getGroupCount() {
        return grupoGuias.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return grupoGuias.get(new ArrayList<String>(grupoGuias.keySet()).get(groupPosition)).size();
    }

    @Override
    public String getGroup(int groupPosition) {
        return new ArrayList<String>(grupoGuias.keySet()).get(groupPosition);
    }

    @Override
    public Guia getChild(int groupPosition, int childPosition) {
        return grupoGuias.get(new ArrayList<String>(grupoGuias.keySet()).get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(android.R.layout.simple_expandable_list_item_1, null);
        }

        TextView title = (TextView) convertView.findViewById(android.R.id.text1);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextSize(20);
        title.setText(headerTitle);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final Guia child = getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.guia_item, null);
        }

        TextView nombreGuia = (TextView) convertView.findViewById(R.id.item_nombre_guia);
        TextView municion = (TextView) convertView.findViewById(R.id.item_precio);
        TextView porcentaje = (TextView) convertView.findViewById(R.id.item_cartuchos_comprados);

        nombreGuia.setText(child.getNombreArma());
        municion.setText(child.getCartuchosGastados() + "\\" + child.getCartuchosTotales());
        float percentValue = (float) child.cartuchosGastados * 100 / child.cartuchosTotales;
        porcentaje.setText(percentValue + "%");

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
