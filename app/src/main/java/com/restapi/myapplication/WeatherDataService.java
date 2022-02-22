package com.restapi.myapplication;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WeatherDataService {

    public static final String LOCATION_SEARCH_QUERY = "https://www.metaweather.com/api/location/search/?query=";
    public static final String QUERY_FOR_CITY_WEATHER_BY_ID = "https://www.metaweather.com/api/location/";
    Context context;
    String cityID;


    public WeatherDataService(Context context) {
        this.context = context;
    }


    public interface VolleyResponseListener {
        void onError(String message);

        void onResponse(String cityID);
    }

    public void getCityID(String cityName, VolleyResponseListener volleyResponseListener){
        String url = LOCATION_SEARCH_QUERY + cityName;
        //String url = "http://192.168.8.102:8080/dts/towaway/api/v1/provider";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                cityID = "";
                //breaking up teh object value by value
                //get teh first item (0) in the array
                try {
                    JSONObject cityinfo = response.getJSONObject(0);
                    cityID = cityinfo.getString("woeid");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                System.out.println("url " + url);
                //this worked but didnt return the id number to main
               // Toast.makeText(context, "City ID " + cityID, Toast.LENGTH_SHORT).show();
                volleyResponseListener.onResponse(cityID);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
               // Toast.makeText(context, error.toString() , Toast.LENGTH_SHORT).show();
                System.out.println("whats the error " + error.toString());
                volleyResponseListener.onError("something wrong");
            }
        });

        MySingleton.getInstance(context).addToRequestQueue(request);
        //returned a null .problem UI Thread - solution callback a callback is a way to schedule a method call to occur when another method finishes its task
        //return cityID;

    }
    public interface ForeCastByIDResponseChul {
        void onError(String message);

        void onResponse(List<WeatherReportModel> weatherReportModels);
    }
    public void getCityForecastByID(String cityID, ForeCastByIDResponseChul foreCastByIDResponse) {
        List<WeatherReportModel> weatherReportModels = new ArrayList<>();
        String url = QUERY_FOR_CITY_WEATHER_BY_ID + cityID;

        // get the json object
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
               // Toast.makeText(context, response.toString(), Toast.LENGTH_SHORT).show();

                try {
                    JSONArray consolidated_weather_list = response.getJSONArray("consolidated_weather");

                    //get the first item in the array


                    // loop get thru the array

                    for (int i=0; i < consolidated_weather_list.length(); i++) {

                        WeatherReportModel One_day_weather = new WeatherReportModel();
                        JSONObject first_day_from_api = (JSONObject) consolidated_weather_list.get(i);
                        //assign json data to the attributes
                        One_day_weather.setId(first_day_from_api.getInt("id"));
                        One_day_weather.setWeather_state_name(first_day_from_api.getString("weather_state_name"));
                        One_day_weather.setWeather_state_abbr(first_day_from_api.getString("weather_state_abbr"));
                        One_day_weather.setWind_direction_compass(first_day_from_api.getString("wind_direction_compass"));
                        One_day_weather.setCreated(first_day_from_api.getString("created"));
                        One_day_weather.setApplicable_date(first_day_from_api.getString("applicable_date"));
                        One_day_weather.setMin_temp(first_day_from_api.getLong("min_temp"));
                        One_day_weather.setMax_temp(first_day_from_api.getLong("max_temp"));
                        One_day_weather.setThe_temp(first_day_from_api.getLong("the_temp"));
                        One_day_weather.setWind_speed(first_day_from_api.getLong("wind_speed"));
                        One_day_weather.setWind_direction(first_day_from_api.getLong("wind_direction"));
                        One_day_weather.setAir_pressure(first_day_from_api.getInt("air_pressure"));
                        One_day_weather.setAir_pressure(first_day_from_api.getInt("humidity"));
                        One_day_weather.setVisibility(first_day_from_api.getLong("visibility"));
                        One_day_weather.setPredictability(first_day_from_api.getInt("predictability"));
                        weatherReportModels.add(One_day_weather);

                    }
                    foreCastByIDResponse.onResponse(weatherReportModels);






                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show();
            }

        });


        // get the property called "consolidated_weather" which is an array

        //get each item in the arrayand assign it to a new WeatherReposrtModel Object


        //fetches the request
        MySingleton.getInstance(context).addToRequestQueue(request);
    }
    public interface GetCityForeCastByNameCallback {
        void onError(String message);
        void onResponse(List<WeatherReportModel> weatherReportModels);
    }

    public void getCityForecastByName(String cityName, final GetCityForeCastByNameCallback getCityForeCastByNameCallback) {
        //call the API Twice
        // fetch the cityid given the city name
        getCityID(cityName, new VolleyResponseListener() {
            @Override
            public void onError(String message) {

            }

            @Override
            public void onResponse(String cityID) {
                getCityForecastByID(cityID, new ForeCastByIDResponseChul() {
                    @Override
                    public void onError(String message) {

                    }

                    @Override
                    public void onResponse(List<WeatherReportModel> weatherReportModels) {
                        //we have the weather reportget
                        getCityForeCastByNameCallback.onResponse(weatherReportModels);

                    }
                });

            }
        });

        // fetch  the city forcast given the city name

    }
}
