package com.valohyd.copilotemaster.models;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by parodilaurent on 24/01/2016.
 */
public class WeatherTime {

    private long timestamp;
    private double temp;
    private double tempMin;
    private double tempMax;
    private int humidity;
    private String summary;
    private String description;
    private int iconId;

    private static HashMap<String,Integer> ICONS;

    static {
        ICONS = new HashMap<>();
        //SOLEIL
        ICONS.put("01d", android.R.drawable.btn_star_big_off);
        ICONS.put("01n", android.R.drawable.btn_star_big_off);
        //NUAGEUX/SOLEIL
        ICONS.put("02d", android.R.drawable.btn_star_big_off);
        ICONS.put("02n", android.R.drawable.btn_star_big_off);
        //NUAGEUX
        ICONS.put("03d", android.R.drawable.btn_star_big_off);
        ICONS.put("03n", android.R.drawable.btn_star_big_off);
        //GROS NUAGE
        ICONS.put("04d", android.R.drawable.btn_star_big_off);
        ICONS.put("04n", android.R.drawable.btn_star_big_off);
        //LEGERES PLUIES
        ICONS.put("09d", android.R.drawable.btn_star_big_off);
        ICONS.put("09n", android.R.drawable.btn_star_big_off);
        //PLUIE/SOLEIL
        ICONS.put("10d", android.R.drawable.btn_star_big_off);
        ICONS.put("10n", android.R.drawable.btn_star_big_off);
        //ORAGES
        ICONS.put("11d", android.R.drawable.btn_star_big_off);
        ICONS.put("11n", android.R.drawable.btn_star_big_off);
        //NEIGE
        ICONS.put("13d", android.R.drawable.btn_star_big_off);
        ICONS.put("13n", android.R.drawable.btn_star_big_off);
        //BROUILLARD
        ICONS.put("50d", android.R.drawable.btn_star_big_off);
        ICONS.put("50n", android.R.drawable.btn_star_big_off);
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
                    this.summary = weather.getString("main");
                    this.description = weather.getString("description");
                    this.iconId = ICONS.get(weather.getString("icon"));
                }
            } catch (JSONException exc) {
                System.out.println(exc.getMessage());
            }
        }
    }

    public long getTimestamp(){
        return timestamp;
    }
}
