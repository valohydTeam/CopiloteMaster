package com.valohyd.copilotemaster.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.valohyd.copilotemaster.R;

/**
 * Created by val on 24/01/2016.
 */
public class MeteoListAdapter extends ArrayAdapter<String> {
    private Context mContext;
    private String[] mVilles;

    // todo changer le type de l'adapter avec les clases modèles de lolo

    public MeteoListAdapter(Context context, String[] villes) {
        super(context, -1, villes);
        mContext= context;
        mVilles = villes;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.meteo_item, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.textview_ville);
        textView.setText(mVilles[position]);

        return rowView;
    }
}
