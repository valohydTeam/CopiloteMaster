package com.valohyd.copilotemaster.models;

import com.valohyd.copilotemaster.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by parodilaurent on 24/01/2016.
 */
public class WeatherTime {
    public static final int DEFAULT_ICON_ID = R.drawable.temps_9_brouillard; // todo faire une icone par défaut quand erreur

    private long timestamp;
    private double temp;
    private double tempMin;
    private double tempMax;
    private int humidity;
    private String summary;
    private String description;
    private int iconId;
    public int weatherId;

    private static HashMap<String,Integer> ICONS;

    static {
        ICONS = new HashMap<>();
        //SOLEIL
        ICONS.put("01d", R.drawable.temps_1_soleil_feedback);
        ICONS.put("01n", R.drawable.temps_1_soleil_feedback);
        //NUAGEUX/SOLEIL
        ICONS.put("02d", R.drawable.temps_2_soleil_nuage_feedback);
        ICONS.put("02n", R.drawable.temps_2_soleil_nuage_feedback);
        //NUAGEUX
        ICONS.put("03d", R.drawable.temps_3_nuageux_feedback);
        ICONS.put("03n", R.drawable.temps_3_nuageux_feedback);
        //GROS NUAGE
        ICONS.put("04d", R.drawable.temps_4_tres_nuageux_feedback);
        ICONS.put("04n", R.drawable.temps_4_tres_nuageux_feedback);
        //LEGERES PLUIES
        ICONS.put("09d", R.drawable.temps_5_pluie_feedback);
        ICONS.put("09n", R.drawable.temps_5_pluie_feedback);
        //PLUIE/SOLEIL
        ICONS.put("10d", R.drawable.temps_6_pluie_soleil_feedback);
        ICONS.put("10n", R.drawable.temps_6_pluie_soleil_feedback);
        //ORAGES
        ICONS.put("11d", R.drawable.temps_7_orage_feedback);
        ICONS.put("11n", R.drawable.temps_7_orage_feedback);
        //NEIGE
        ICONS.put("13d", R.drawable.temps_8_neige_feedback);
        ICONS.put("13n", R.drawable.temps_8_neige_feedback);
        //BROUILLARD
        ICONS.put("50d", R.drawable.temps_9_brouillard_feedback);
        ICONS.put("50n", R.drawable.temps_9_brouillard_feedback);
    }

    public WeatherTime(long timestamp,double temp,double tempMin, double tempMax, int humidity, String summary, String description, String icon){
        this.timestamp = timestamp;
        this.temp = temp;
        this.tempMin = tempMin;
        this.tempMax = tempMax;
        this.humidity = humidity;
        this.summary = summary;
        this.description = description;
        this.iconId = ICONS.get(icon);
        this.weatherId = iconIdToWeatherId(icon);
    }

    /**
     * converti l'iconId retourné par l'API (e.g. "10d") en int (e.g. 10)
     * @param iconId iconId retourné par l'API
     * @return Les 2 premiers char de l'iconID converti en int
     */
    private int iconIdToWeatherId(String iconId){
        return Integer.parseInt(iconId.substring(0,2));
    }

    /**
     * retourne la chaine donnée en paramètre avec la premiere lettre en majuscules
     * @param toCap
     * @return
     */
    private String firstLetterCap(String toCap){
        String res = toCap;
        char c = toCap.charAt(0);
        if(c >= 'a' && c <= 'z'){
            c += 'A' - 'a'; // mise en majuscule
            res= c + toCap.substring(1); // remplacement du premier char par la majuscule
        }
        return res;
    }

    public WeatherTime(JSONObject json){
        if(json!=null) {
            try {
                this.timestamp = json.getLong("dt");
                JSONObject main = json.getJSONObject("main");
                this.temp = main.getDouble("temp");
                this.tempMin = main.getDouble("temp_min");
                this.tempMax = main.getDouble("temp_max");
                this.humidity = main.getInt("humidity");
                JSONArray weatherArray = json.getJSONArray("weather");
                if(weatherArray.length()>0) {
                    JSONObject weather = (JSONObject)weatherArray.get(0);
                    // récupération du texte avec mise en majuscule de la première lettre
                    this.summary = firstLetterCap(weather.getString("main"));
                    this.description = firstLetterCap(weather.getString("description"));

                    this.iconId = ICONS.get(weather.getString("icon"));
                    this.weatherId = iconIdToWeatherId(weather.getString("icon"));
                }
            } catch (JSONException exc) {
                System.out.println(exc.getMessage());
            }
        }
    }

//    public long getTimestamp(){
//        return timestamp;
//    }

    public long getTimestampInMillis(){
        return timestamp * 1000;
    }

    // *******************************************
    //
    //   G E T T E R S
    //
    // *******************************************
    public double getTemp() {
        return temp;
    }

    public double getTempMin() {
        return tempMin;
    }

    public double getTempMax() {
        return tempMax;
    }

    public int getHumidity() {
        return humidity;
    }

    public String getSummary() {
        return summary;
    }

    public String getDescription() {
        return description;
    }

    public int getIconId() {
        return iconId;
    }

    public static HashMap<String, Integer> getICONS() {
        return ICONS;
    }

    public int getWeatherId() {
        return weatherId;
    }
}
