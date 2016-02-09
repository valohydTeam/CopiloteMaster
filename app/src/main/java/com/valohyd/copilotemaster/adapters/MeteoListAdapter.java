package com.valohyd.copilotemaster.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.valohyd.copilotemaster.R;
import com.valohyd.copilotemaster.models.WeatherCity;
import com.valohyd.copilotemaster.models.WeatherTime;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by val on 24/01/2016.
 *
 * Adapter pour afficher la météo d'une ville dans une cardview
 */
public class MeteoListAdapter extends ArrayAdapter<WeatherCity> {
    // tableau des ids des textview pour chaque jours
    private static final int IDS_TEXTVIEWS_JOURS[] = {R.id.tewtview_meteo_j1, R.id.tewtview_meteo_j2, R.id.tewtview_meteo_j3, R.id.tewtview_meteo_j4, R.id.tewtview_meteo_j5};
    private static final int IDS_IMAGEVIEWS_JOURS[] = {R.id.imageview_meteo_j1, R.id.imageview_meteo_j2, R.id.imageview_meteo_j3, R.id.imageview_meteo_j4, R.id.imageview_meteo_j5};

    private Context mContext;
    private ArrayList<WeatherCity> mVilles;


    public MeteoListAdapter(Context context, ArrayList<WeatherCity> villes) {
        super(context, -1, villes);
        mContext= context;
        mVilles = villes;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if(rowView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.meteo_item, parent, false);
        }

        // remplir la vue avec les infos meteo
        // ** nom ville
        TextView textView = (TextView) rowView.findViewById(R.id.textview_ville);
        textView.setText(mVilles.get(position).getCityName());

        // * récupérer le pire temps du jour
        GregorianCalendar today = new GregorianCalendar();
        // on n'a pas de worst si :
// 1. pas de données, donc c'est la merde
// 2. pas de données pour aujourd'hui, donc on affiche à partir de demain
// => donc on essaie avec aujourd'hui, si null, on essaie avec demain, si null alors pas de données
        WeatherTime worstToday = mVilles.get(position).getWorstWeatherOfDay(today.getTimeInMillis());
        if(worstToday == null){
// on avance à demain
            today.roll(GregorianCalendar.DAY_OF_YEAR, 1);
            worstToday = mVilles.get(position).getWorstWeatherOfDay(today.getTimeInMillis());
        }
// si worstToday est toujours null, alors on n'a pas de données => on affiche la carte no data
        if(worstToday == null){
            //rowView
        }
        // ** detail meteo
        textView = (TextView) rowView.findViewById(R.id.textview_detail_meteo);
        String detailMeteo = "-";
        if(worstToday != null){
            // format : <Lun 13h : Ensoleillé>
            SimpleDateFormat formatJour = new SimpleDateFormat("E H", getContext().getResources().getConfiguration().locale);
            StringBuilder builder = new StringBuilder(formatJour.format(new Date(worstToday.getTimestampInMillis())));
            builder.append("h : ");
            builder.append(worstToday.getDescription());
            detailMeteo = builder.toString();
        }
        textView.setText(detailMeteo);

        // ** temperature
        textView = (TextView)  rowView.findViewById(R.id.textview_meteo_temperature);
        textView.setText(worstToday == null ? "-" : Math.round(worstToday.getTemp()) + "°");

        // ** image meteo
        ImageView imv = (ImageView) rowView.findViewById(R.id.image_meteo_big);
        if(worstToday != null) {
            imv.setImageResource(worstToday.getIconId());
        }

        // * remplir les 5 jours qui arrivent
        for(int i = 0; i < 5; i++){
            // récupérer la date du jour concerné
            GregorianCalendar thisDay = (GregorianCalendar) today.clone();
            thisDay.roll(Calendar.DAY_OF_YEAR, i);

            // récupérer le temps de ce jour
            WeatherTime worstThisDay = mVilles.get(position).getWorstWeatherOfDay(thisDay.getTimeInMillis());

            // afficher les infos de ce jour
            // ** image
            ImageView imagView = (ImageView) rowView.findViewById(IDS_IMAGEVIEWS_JOURS[i]);
            imagView.setImageResource(worstThisDay == null ? WeatherTime.DEFAULT_ICON_ID : worstThisDay.getIconId());
            // ** jour
            textView = (TextView) rowView.findViewById(IDS_TEXTVIEWS_JOURS[i]);
            SimpleDateFormat formatJour = new SimpleDateFormat("E", getContext().getResources().getConfiguration().locale);
            textView.setText(formatJour.format(new Date(thisDay.getTimeInMillis())));
        }


        // todo completer la vue avec les infos de WeatherCity
        return rowView;
    }
}
