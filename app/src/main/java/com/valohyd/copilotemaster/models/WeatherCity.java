package com.valohyd.copilotemaster.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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
            this.weatherTimes.put(wt.getTimestamp(),wt);
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
                    this.weatherTimes.put(wt.getTimestamp(), wt);
                }
            } catch (JSONException exc) {
                System.out.println(exc.getMessage());
            }
        }
    }

    public WeatherTime getNextWeatherTime(Long timestamp){
        return this.weatherTimes.ceilingEntry(timestamp).getValue();
    }
}
