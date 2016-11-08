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
 * Created by Luantm on 11/8/2016.
 */

public class AsynThreadTask extends AsyncTask<Void, Void,Void> {

    public   AsynThreadTask(double _lat, double _lng, String _category)
    {
        category = _category;
        lng = _lng;
        lat = _lat;
    }

    public ArrayList<Position> getRs() {
        return rs;
    }

    private ArrayList<Position> rs = null;
    private String category = "";
    private double lat = 0;
    private double lng = 0;
    private String api = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=10.762622,106.660172&key=AIzaSyDLNooeVehb28bYKVglMDjzxCSsGfp1GEE&components=country:vn&radius=10000&type=";
    public ArrayList<Position> getLocations(double lat, double lng, String type) {

        ArrayList<Position> locations = new ArrayList<Position>();
        StringBuilder sb = new StringBuilder(api.replace("{ll}", lat + "," + lng) + type);
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
            JSONArray places = JObj.getJSONArray("results");
            int len = places.length();
            //len = len > 15 ? 15 : len;
            for (int i = 0; i < len; i++) {
                JSONObject jobj = places.getJSONObject(i);
                Position ln = new Position();
                JSONObject jlocation = jobj.getJSONObject("geometry").getJSONObject("location");
                ln._lat = (float) jlocation.getDouble("lat");
                ln._long = (float) jlocation.getDouble("lng");
                ln._name = jobj.getString("name");
                ln._address = jobj.getString("vicinity");
                locations.add(ln);
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return locations;
    }
    @Override
    protected Void doInBackground(Void... params) {

        rs = getLocations(lat,lng,category);
        return  null;
    }
}
