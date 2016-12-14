package h2l.se.uit.placesaroundme;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Luantm on 12/7/2016.
 */

public class GetWeatherDataTask extends AsyncTask<Void, Void,Void> {

    public GetWeatherDataTask(double _lat, double _lng)
    {
        lng = _lng;
        lat = _lat;
    }
    private double lat = 0;
    public String wData = "";
    private double lng = 0;
    private String api = "http://api.openweathermap.org/data/2.5/weather?{arg}&APPID=7dffb65f295bdf08864967f051bc533d";
    public String getWeatherData(double lat, double lng) {
        String arg = "lat="+lat+"&lon=" + lng;
        StringBuilder sb = new StringBuilder(api.replace("{arg}", arg));
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            return null;
        } catch (IOException e) {
            return null;
        } finally {
            try {
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Exception ex) {
                ex = ex;
            }
        }
        try {
            JSONObject JObj = new JSONObject(jsonResults.toString());
            if(JObj.getInt("cod") == 200)
            {
                //weather
                JSONArray arr = JObj.getJSONArray("weather");
                JSONObject rs = arr.getJSONObject(0);
                return rs.getString("description");

            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }

    public String getWeatherDataNew(double lat, double lng) {
        String arg = "lat="+lat+"&lon=" + lng;
        StringBuilder sb = new StringBuilder(api.replace("{arg}", arg));
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            return null;
        } catch (IOException e) {
            return null;
        } finally {
            try {
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Exception ex) {
                ex = ex;
            }
        }
        try {
            JSONObject JObj = new JSONObject(jsonResults.toString());
            if(JObj.getInt("cod") == 200)
            {
                //weather
                JSONArray arr = JObj.getJSONArray("weather");
                JSONObject rs = arr.getJSONObject(0);
                return rs.getString("description");

            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected Void doInBackground(Void... params) {

         wData  = getWeatherData(lat,lng);
        return  null;
    }
}

