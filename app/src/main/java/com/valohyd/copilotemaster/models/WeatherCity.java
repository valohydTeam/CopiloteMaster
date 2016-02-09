package com.valohyd.copilotemaster.models;

import android.graphics.drawable.GradientDrawable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TreeMap;

/**
 * Created by parodilaurent on 24/01/2016.
 */
public class WeatherCity {

    private String cityName = "";
    private long cityId;
    private double cityLat;
    private double cityLon;
    private TreeMap<Long,WeatherTime> weatherTimes;

    public WeatherCity(long id, String name, double lat,double lon,ArrayList<WeatherTime> weatherTimes){
        this.cityId = id;
        this.cityName = name;
        this.cityLat = lat;
        this.cityLon = lon;
        this.weatherTimes = new TreeMap<>();
        for(WeatherTime wt:weatherTimes){
            this.weatherTimes.put(wt.getTimestampInMillis(),wt);
        }
    }

    public WeatherCity(JSONObject json){
        if(json!=null) {
            try {
                JSONObject city = json.getJSONObject("city");
                this.cityId = city.getLong("id");
                this.cityName = city.getString("name");
                JSONObject cityCoord = city.getJSONObject("coord");
                this.cityLat = cityCoord.getDouble("lat");
                this.cityLon = cityCoord.getDouble("lon");
                JSONArray weathers = json.getJSONArray("list");
                this.weatherTimes = new TreeMap<>();
                for (int i = 0; i < weathers.length() - 1; i++) {
                    WeatherTime wt = new WeatherTime((JSONObject) weathers.get(i));
                    this.weatherTimes.put(wt.getTimestampInMillis(), wt);
                }
            } catch (JSONException exc) {
                System.out.println(exc.getMessage());
            }
        }
    }

    /**
     * retourne la prochaine météo par rapport à l'heure donné
     * @param timestamp
     * @return
     */
    public WeatherTime getNextWeatherTime(Long timestamp){
        return this.weatherTimes.ceilingEntry(timestamp).getValue();
    }

    /**
     * retourne le pire temps du jour
     * @param timestamp timestamp compris dans le jour voulu, /!\ en MILLISECONDES !
     * @return
     */
    public WeatherTime getWorstWeatherOfDay(Long timestamp){
        WeatherTime worst = null;
        if(weatherTimes != null && weatherTimes.size() > 0){
            // on récupère la premiière heure de la journée
            GregorianCalendar calTmp = new GregorianCalendar();
            calTmp.setTimeInMillis(timestamp);
            GregorianCalendar calToday = new GregorianCalendar(calTmp.get(Calendar.YEAR), calTmp.get(Calendar.MONTH), calTmp.get(Calendar.DAY_OF_MONTH));
            // récupérer aussi demain
            GregorianCalendar calTomorrow = (GregorianCalendar)calToday.clone();
            calTomorrow.roll(Calendar.DAY_OF_YEAR, 1);

            // récupérer le pire temps d'aujourd'hui
            for(Long timestp : weatherTimes.keySet()){
                // si la météo concerne bien aujourd'hui
                if( (timestp > calToday.getTimeInMillis()) && (timestp < calTomorrow.getTimeInMillis()) ){
                    // on récupère le pire
                    if(worst == null){
                        worst = weatherTimes.get(timestp);
                    }
                    else{
                        // on regarde le weatherID, plus c'est élevé, plus le temps est pourri
                        WeatherTime wt = weatherTimes.get(timestp);
                        if(wt.getWeatherId() > worst.getWeatherId()){
                            worst = wt;
                        }
                    }
                }
                // todo peut-être arreter la boucle si timestamp >= tomorow, puisque c'est trié (?)
            }
        }
        return worst;
    }

    // *******************************************
    //
    //   G E T T E R S
    //
    // *******************************************
    public String getCityName() {
        return cityName;
    }

    public long getCityId() {
        return cityId;
    }

    public double getCityLat() {
        return cityLat;
    }

    public double getCityLon() {
        return cityLon;
    }

    public TreeMap<Long, WeatherTime> getWeatherTimes() {
        return weatherTimes;
    }
}
