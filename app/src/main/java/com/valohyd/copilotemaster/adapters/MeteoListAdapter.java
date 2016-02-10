package com.valohyd.copilotemaster.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SeekBar;
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
    public View getView(final int position, View convertView, ViewGroup parent) {
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

        // ** récupérer le pire temps du jour
        final GregorianCalendar today = new GregorianCalendar();
        // on n'a pas de worst si :
        //   1. pas de données, donc c'est la merde
        //   2. pas de données pour aujourd'hui, donc on affiche à partir de demain
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
            detailMeteo = getDetailMeteo(getContext(), worstToday);
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

        // ajouter le listener du seekbar pour les heures et le limiter à l'heure actuelle
        final SeekBar seekBar = (SeekBar) rowView.findViewById(R.id.seekbar_meteo_heure);
        final View finalRowView = rowView;
        if(seekBar != null){
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    // on récupère le holder pour savoir si un jour a été séléctionné
                    HolderMeteo holder = null;
                    if(finalRowView.getTag() != null){
                        holder = (HolderMeteo) finalRowView.getTag();
                    }
                    int jourDemandé = (holder == null ? 0 : holder.jourSelectionne);

                    // récupérer la météo de l'heure demandée
                    GregorianCalendar premierJourMeteo = (GregorianCalendar) today.clone();
                    premierJourMeteo.roll(GregorianCalendar.DAY_OF_YEAR, jourDemandé);
                    premierJourMeteo.set(GregorianCalendar.HOUR_OF_DAY, (i * 3)); // todo je pense que ca ne va pas marcher, puisque si il est 13, que tu cherches 11h, on l'a pas...

                    WeatherTime meteo = mVilles.get(position).getNextWeatherTime(premierJourMeteo.getTimeInMillis());

                    // changer la grosse image
                    String detailMeteo = "-";
                    TextView textView = (TextView) finalRowView.findViewById(R.id.textview_detail_meteo);
                    if(meteo != null){
                        detailMeteo = getDetailMeteo(getContext(), meteo);
                    }
                    textView.setText(detailMeteo);

                    // ** temperature
                    textView = (TextView)  finalRowView.findViewById(R.id.textview_meteo_temperature);
                    textView.setText(meteo == null ? "-" : Math.round(meteo.getTemp()) + "°");

                    // ** image meteo
                    ImageView imv = (ImageView) finalRowView.findViewById(R.id.image_meteo_big);
                    if(meteo != null) {
                        imv.setImageResource(meteo.getIconId());
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // ballec
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // ballec
                }
            });
        }

        // clic sur les jours en bas => changer le jour affiché
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HolderMeteo holder = new HolderMeteo();
                // on teste l'id et on set le jour qui correspond
                if(view.getId() == R.id.imageview_meteo_j1){
                    holder.jourSelectionne = 0;
                }
                else if(view.getId() == R.id.imageview_meteo_j2){
                    holder.jourSelectionne = 1;
                }
                else if(view.getId() == R.id.imageview_meteo_j3){
                    holder.jourSelectionne = 2;
                }
                else if(view.getId() == R.id.imageview_meteo_j4){
                    holder.jourSelectionne = 3;
                }
                else if(view.getId() == R.id.imageview_meteo_j5){
                    holder.jourSelectionne = 4;
                }

                // donner le holder
                finalRowView.setTag(holder);
                // on change le progress pour déclencher le listener
                if(seekBar != null){
                    if(seekBar.getProgress() == 0){
                        seekBar.setProgress(1);
                    }
                    seekBar.setProgress(0);
                }
            }
        };
        ImageView imageJ = (ImageView) rowView.findViewById(R.id.imageview_meteo_j1);
        imageJ.setOnClickListener(listener);
        imageJ = (ImageView) rowView.findViewById(R.id.imageview_meteo_j2);
        imageJ.setOnClickListener(listener);
        imageJ = (ImageView) rowView.findViewById(R.id.imageview_meteo_j3);
        imageJ.setOnClickListener(listener);
        imageJ = (ImageView) rowView.findViewById(R.id.imageview_meteo_j4);
        imageJ.setOnClickListener(listener);
        imageJ = (ImageView) rowView.findViewById(R.id.imageview_meteo_j5);
        imageJ.setOnClickListener(listener);



        // todo completer la vue avec les infos de WeatherCity
        return rowView;
    }

    /**
     * retourne la chaine "détail" formatée de la météo donnée en paramètre
     * @param ctxt context
     * @param toFormat meteo à formater
     * @return string formatée prête à être affichée
     */
    private static String getDetailMeteo(Context ctxt, WeatherTime toFormat){
        // format : <Lun 13h : Ensoleillé>
        SimpleDateFormat formatJour = new SimpleDateFormat("E H", ctxt.getResources().getConfiguration().locale);
        StringBuilder builder = new StringBuilder(formatJour.format(new Date(toFormat.getTimestampInMillis())));
        builder.append("h : ");
        builder.append(toFormat.getDescription());
        return builder.toString();
    }

    private class HolderMeteo {
        int jourSelectionne; // index du jour cliqué
    }
}
