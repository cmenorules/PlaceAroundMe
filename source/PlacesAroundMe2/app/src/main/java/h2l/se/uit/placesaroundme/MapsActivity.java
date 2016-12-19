package h2l.se.uit.placesaroundme;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.vision.text.Text;

import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

//import h2l.se.uit.placesaroundme.dialogs.DetailsMarker;


public class MapsActivity extends AppCompatActivity implements LocationListener,GoogleMap.OnMarkerClickListener {
    private GoogleMap myMap;
    private ProgressDialog myProgress;
    TextView tvDistanceDuration;

    private static final String MYTAG = "MYTAG";

    // Mã yêu cầu uhỏi người dùng cho phép xem vị trí hiện tại của họ (***).
    // Giá trị mã 8bit (value < 256).
    public static final int REQUEST_ID_ACCESS_COURSE_FINE_LOCATION = 100;

    //x,y touch
    private LatLng latLng;

    //x,y GPS
    private LatLng latLngGPS;


    private Marker marker;
    Geocoder geocoder;
    private Handler hd_timer;
    private CheckWeatherForcastRunable checkWeatherTask;
    private ImageView imageView;

    @Override
    public boolean onMarkerClick(Marker marker) {
        LatLng origin = currentlocation.getPosition();
        LatLng dest = marker.getPosition();
        String url = getDirectionsUrl(origin, dest);

        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
        return true;

    }


    private class CheckWeatherForcastRunable implements Runnable {
        @Override
        public void run() {
            ChecktWeatherForcast();
        }
    }

    private void CheckWeatherForcast()
    {
        try
        {
            if(IsConnected())
            {
                GetWeatherDataTask task = new GetWeatherDataTask(_lat,_long);
                task.execute().get();
                txt_location.setText(task.wData);
            }
            hd_timer.postDelayed(checkWeatherTask,60000);
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }


    private com.google.android.gms.common.api.GoogleApiClient client;

    private void ChecktWeatherForcast() {
        try {
            if(IsConnected())
            {
                GetWeatherDataTask gwdt = new GetWeatherDataTask(_lat, _long);
                gwdt.execute().get();
                switch (gwdt.wData)
                {
                    case "Clouds" :
                        imageView.setImageResource(R.drawable.cloulds);
                        break;

                    case "Clear" :
                        imageView.setImageResource(R.drawable.clear);
                        break;
                    case "Rain" :
                        imageView.setImageResource(R.drawable.rain);
                        break;
                    case "Snow" :
                        imageView.setImageResource(R.drawable.snow);
                        break;
                    case "Thunderstorm" :
                        imageView.setImageResource(R.drawable.thunderstorm);
                        break;
                    default:
                        imageView.setImageResource(R.drawable.clear);
                        break;
                }
            }
            hd_timer.postDelayed(checkWeatherTask, 60000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        geocoder = new Geocoder(this, Locale.getDefault());
        // Tạo Progress Bar
        myProgress = new ProgressDialog(this);
        myProgress.setTitle("Map Loading ...");
        myProgress.setMessage("Please wait...");
        myProgress.setCancelable(true);

        // Hiển thị Progress Bar
        myProgress.show();


        SupportMapFragment mapFragment
                = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        tvDistanceDuration = (TextView) findViewById(R.id.tv_distance_time);

        // Sét đặt sự kiện thời điểm GoogleMap đã sẵn sàng.
        mapFragment.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(GoogleMap googleMap) {
                onMyMapReady(googleMap);
            }
        });


        try {
            client = new com.google.android.gms.common.api.GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
            init();
            hd_timer = new Handler();
            checkWeatherTask = new CheckWeatherForcastRunable();
            hd_timer.removeCallbacks(checkWeatherTask);
            hd_timer.postDelayed(checkWeatherTask, 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private Marker currentlocation = null;


    private  double _long = 106.7664525;
    private  double _lat = 10.8864698;

    private void onMyMapReady(GoogleMap googleMap) {

        // Lấy đối tượng Google Map ra:
        myMap = googleMap;

        // Thiết lập sự kiện đã tải Map thành công
        myMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {

            @Override
            public void onMapLoaded() {

                // Đã tải thành công thì tắt Dialog Progress đi
                myProgress.dismiss();

                // Hiển thị vị trí người dùng.
                try {
                    askPermissionsAndShowMyLocation();
                } catch (Exception e) {

                }
            }
        });
        myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        myMap.getUiSettings().setZoomControlsEnabled(true);

        //not the error, you can build normally
        myMap.setMyLocationEnabled(true);
        myMap.setContentDescription("I dont know");

        if (myMap != null) {
            myMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

                @Override
                public void onMyLocationChange(Location arg0) {
                    // TODO Auto-generated method stub
                    try {
                        latLngGPS = new LatLng(arg0.getLatitude(), arg0.getLongitude());
                        if(currentlocation != null)
                        {
                            currentlocation.remove();
                        }
                        currentlocation = myMap.addMarker(new MarkerOptions().position(new LatLng(arg0.getLatitude(), arg0.getLongitude())).title("It's Me!").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                        Geocoder gcd = new Geocoder(MapsActivity.this, Locale.getDefault());
                        if (latLngGPS != null) {
                            try {
                                List<Address> addresses = geocoder.getFromLocation(arg0.getLatitude(), arg0.getLongitude(), 1);
                                if (addresses.size() > 0) {
                                    txt_location.setText(addresses.get(0).getLocality() ) ;
                                } else {

                                    txt_location.setText("Searching...");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    } catch (Exception e) {

                    }
                }
            });
            myMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

                @Override
                public void onMapClick(LatLng point) {
                    //save current location
                    try {
                        latLng = point;

                        List<android.location.Address> addresses = new ArrayList<>();
                        try {
                            addresses = geocoder.getFromLocation(point.latitude, point.longitude, 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        android.location.Address address = addresses.get(0);

                        if (address != null) {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                                sb.append(address.getAddressLine(i) + "\n");
                            }
                            Toast.makeText(MapsActivity.this, sb.toString(), Toast.LENGTH_LONG).show();
                        }
                        veduongdi();

                        //remove previously placed Marker
                        if (marker != null) {
                            marker.remove();
                        }

                        //place marker where user just clicked
                        marker = myMap.addMarker(new MarkerOptions().position(point).title("Marker")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));


                    } catch (Exception ex) {

                    }
                }


            });

            myMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
            {

                @Override
                public boolean onMarkerClick(Marker arg0) {
                    LatLng origin = currentlocation.getPosition();
                    LatLng dest = arg0.getPosition();
                    String url = getDirectionsUrl(origin, dest);

                    DownloadTask downloadTask = new DownloadTask();

                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);
                    return true;
                }

            });
        }

    }


    private void askPermissionsAndShowMyLocation() {

        try {
            // Với API >= 23, bạn phải hỏi người dùng cho phép xem vị trí của họ.
            if (Build.VERSION.SDK_INT >= 23) {
                int accessCoarsePermission
                        = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
                int accessFinePermission
                        = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);


                if (accessCoarsePermission != PackageManager.PERMISSION_GRANTED
                        || accessFinePermission != PackageManager.PERMISSION_GRANTED) {

                    // Các quyền cần người dùng cho phép.
                    String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION};

                    // Hiển thị một Dialog hỏi người dùng cho phép các quyền trên.
                    ActivityCompat.requestPermissions(this, permissions,
                            REQUEST_ID_ACCESS_COURSE_FINE_LOCATION);

                    return;
                }

            }

            // Hiển thị vị trí hiện thời trên bản đồ.
            //this.showMyLocation();
        } catch (Exception ex) {

        }
    }


    // Khi người dùng trả lời yêu cầu cấp quyền (cho phép hoặc từ chối).
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        try {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            //
            switch (requestCode) {
                case REQUEST_ID_ACCESS_COURSE_FINE_LOCATION: {


                    // Chú ý: Nếu yêu cầu bị bỏ qua, mảng kết quả là rỗng.
                    if (grantResults.length > 1
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED
                            && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                        Toast.makeText(this, "Permission granted!", Toast.LENGTH_LONG).show();

                        // Hiển thị vị trí hiện thời trên bản đồ.
                        this.showMyLocation();
                    }
                    // Hủy bỏ hoặc từ chối.
                    else {
                        Toast.makeText(this, "Permission denied!", Toast.LENGTH_LONG).show();
                    }
                    break;
                }
            }
        } catch (Exception ex) {

        }
    }

    // Tìm một nhà cung cấp vị trị hiện thời đang được mở.
    private String getEnabledLocationProvider() {
        try {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            // Tiêu chí để tìm một nhà cung cấp vị trí.
            Criteria criteria = new Criteria();
            // Tìm một nhà cung vị trí hiện thời tốt nhất theo tiêu chí trên.
            // ==> "gps", "network",...
            String bestProvider = locationManager.getBestProvider(criteria, true);
            boolean enabled = locationManager.isProviderEnabled(bestProvider);
            if (!enabled) {
                Toast.makeText(this, "No location provider enabled!", Toast.LENGTH_LONG).show();
                Log.i(MYTAG, "No location provider enabled!");
                return null;
            }
            return bestProvider;
        } catch (Exception ex) {
            return null;
        }
    }

    // Chỉ gọi phương thức này khi đã có quyền xem vị trí người dùng.
    private void showMyLocation() {

        try {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            String locationProvider = this.getEnabledLocationProvider();

            if (locationProvider == null) {
                return;
            }

            // Millisecond
            final long MIN_TIME_BW_UPDATES = 1000;
            // Met
            final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;

            Location myLocation = null;
            try {

                // Đoạn code nay cần người dùng cho phép (Hỏi ở trên ***).
                locationManager.requestLocationUpdates(
                        locationProvider,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, (LocationListener) this);

                // Lấy ra vị trí.
                myLocation = locationManager
                        .getLastKnownLocation(locationProvider);
            }
            // Với Android API >= 23 phải catch SecurityException.
            catch (SecurityException e) {
                Toast.makeText(this, "Show My Location Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(MYTAG, "Show My Location Error:" + e.getMessage());
                e.printStackTrace();
                return;
            }

            try {
                if (myLocation != null) {
                    myMap.clear();
                    latLngGPS = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngGPS, 13));

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(latLngGPS)             // Sets the center of the map to location user
                            .zoom(15)                   // Sets the zoom
                            .bearing(90)                // Sets the orientation of the camera to east
                            .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                            .build();                   // Creates a CameraPosition from the builder
                    myMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                    MarkerOptions option = new MarkerOptions();
                    option.title("My Location");
                    option.snippet("....");
                    option.position(latLngGPS);
                    Marker currentMarker = myMap.addMarker(option);
                    currentMarker.showInfoWindow();
                } else {
                    Toast.makeText(this, "Location not found!", Toast.LENGTH_LONG).show();
                    Log.i(MYTAG, "Location not found");
                }
            } catch (Exception ex) {

            }
        } catch (Exception ex) {

        }


    }

    private AutoCompleteTextView txt_place = null;
    private TextView txt_location = null;

    private void init() {
        txt_place = (AutoCompleteTextView) findViewById(R.id.txt_place);
        txt_location = (TextView) findViewById(R.id.txt_city);
        imageView = (ImageView)findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (latLngGPS == null) {
                    try {
                        Toast.makeText(MapsActivity.this, "Please select your location first!", Toast.LENGTH_LONG).show();
                    } catch (Exception ex) {
                                          }
                }
                else
                {
                    try {
                        Intent i = new Intent(getApplicationContext(),WheatherActivity.class);
                        i.putExtra("LONG", latLngGPS.longitude);
                        i.putExtra("LAT", latLngGPS.latitude);
                        startActivity(i);

                    }catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
        });
        txt_place.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (latLngGPS == null) {
                    try {
                        txt_place.setText("");
                        Toast.makeText(MapsActivity.this, "Please select your location first!", Toast.LENGTH_LONG).show();
                    } catch (Exception ex) {

                    }
                }
            }
        });
        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, PlaceCD.places);
        txt_place.setAdapter(adapter);
        txt_place.setThreshold(1);
        txt_place.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    if (!IsConnected()) {
                        try {
                            txt_place.setText("");
                            Toast.makeText(MapsActivity.this, "Please check your internet connection!", Toast.LENGTH_LONG).show();
                        } catch (Exception ex) {
                            ex = ex;
                        }
                    }
                    if (latLngGPS != null) {
                        AsynThreadTask task = new AsynThreadTask(latLngGPS.latitude, latLngGPS.longitude, adapter.getItem(position).toString().replace(" ", "_").toLowerCase());
                        try {
                            task.execute().get();
                            ArrayList<Position> poss = task.getRs();
                            myMap.clear();
                            int count = 0;
                            for (Position i : poss) {
                                LatLng latL = new LatLng(i._lat, i._long);
                                MarkerOptions option = new MarkerOptions();
                                option.title(i._name + ". " + i._address);
                                option.snippet("....");
                                option.position(latL);
                                Marker currentMarker = myMap.addMarker(option);
                                currentMarker.showInfoWindow();
                                count += 1;
                                if (count > 10) {
                                    break;
                                }
                            }

                            if (poss.size() > 0) {
                                CameraPosition cameraPosition = new CameraPosition.Builder()
                                        .target(latLngGPS)             // Sets the center of the map to location user
                                        .zoom(13)                   // Sets the zoom
                                        .bearing(90)                // Sets the orientation of the camera to east
                                        .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                                        .build();                   // Creates a CameraPosition from the builder
                                myMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                            }


                        } catch (InterruptedException e) {

                        } catch (ExecutionException e) {
                        }
                        //getLocations(latLngGPS.latitude, latLngGPS.longitude, "atm");
                    } else {
                        txt_place.setText("");
                        Toast.makeText(MapsActivity.this, "Can't find your location, please check your location again!", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception ex) {

                }
            }
        });

    }


    private void selectAnItem() {

    }

    @Override
    public void onLocationChanged(Location location) {
        latLngGPS = new LatLng(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    public boolean IsConnected() {
        try {
            ConnectivityManager connectivity = (ConnectivityManager) this.getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo[] info = connectivity.getAllNetworkInfo();
                if (info != null)
                    for (int i = 0; i < info.length; i++)
                        if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                            return true;
                        }
            }
        } catch (Exception ex) {

        }

        return false;
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        try {
            super.onStart();
            client.connect();
            AppIndex.AppIndexApi.start(client, getIndexApiAction());
        } catch (Exception ex) {


        }
    }

    @Override
    public void onStop() {
        super.onStop();

        try {
            AppIndex.AppIndexApi.end(client, getIndexApiAction());
            client.disconnect();
        } catch (Exception e) {

        }
    }

    class veduongdixml extends AsyncTask<Double, Void, Void> {
        ArrayList<LatLng> mangtoado;

        @Override
        protected Void doInBackground(Double... params) {
            // TODO Auto-generated method stub
            Direction md = new Direction();
            LatLng x = new LatLng(params[0], params[1]);
            LatLng y = new LatLng(params[2], params[3]);
            Document doc = md.getDocument(x, y, Direction.MODE_DRIVING);
            mangtoado = md.getDirection(doc);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            PolylineOptions rectLine = new PolylineOptions().width(10).color(Color.YELLOW); // Màu và độrộng

            for (int i = 0; i < mangtoado.size(); i++) {
                rectLine.add(mangtoado.get(i));
            }

            try {
                path.remove();
            } catch (Exception e) {

            }
            path = myMap.addPolyline(rectLine);

        }

    }

    private Polyline path = null;

    public void veduongdi() {
        veduongdixml a = new veduongdixml();
        a.execute(latLng.latitude,
                latLng.longitude,
                latLngGPS.latitude,
                latLngGPS.longitude);
    }

    private ArrayList<Marker> markers = new ArrayList<Marker>();
    Marker mym;
    Marker marker_b;



    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception while downloading url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }
    Polyline polyline=null;
    /**
     * A class to parse the Google Places in JSON format
     */
    class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            String distance = "";
            String duration = "";

            if (result.size() < 1) {
                Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                return;
            }

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    if (j == 0) {    // Get distance from the list
                        distance = (String) point.get("distance");
                        continue;
                    } else if (j == 1) { // Get duration from the list
                        duration = (String) point.get("duration");
                        continue;
                    }

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.BLUE);
            }

            tvDistanceDuration.setText("Distance:" + distance + ", Duration:" + duration);

            // Drawing polyline in the Google Map for the i-th route
            if(polyline!=null)
            {
                polyline.remove();
                polyline = myMap.addPolyline(lineOptions);
            }
            else{
                polyline = myMap.addPolyline(lineOptions);
            }


        }
    }

}


