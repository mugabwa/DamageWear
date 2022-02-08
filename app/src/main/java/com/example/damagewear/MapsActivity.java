package com.example.damagewear;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.StrictMode;
import android.util.AttributeSet;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.Permission;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener {


    private GoogleMap mMap;
    private LatLng mOrigin;
    private LatLng mDestination;
    private Polyline mPolyline;
    SearchView searchView;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    ArrayList<LatLng> mMarkerPoints;
    private static final int REQUEST_CODE = 101;
    public static final String EXTRA_MESSAGE = "com.example.damagewear.MESSAGE";
    SupportMapFragment mapView;
    View btnCurrentLocation;
    TextView origin;
    TextView destination, cost_ksh;
    Geocoder place;
    List<Address> addresses;
    JSONObject distance;
    AtomicBoolean processed = new AtomicBoolean(true);

    String domain_name;
    PersistentCookieStore store;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        distance = new JSONObject();
        searchView = findViewById(R.id.idSearchView);
        origin = (TextView) findViewById(R.id.fromPlace);
        destination = (TextView) findViewById(R.id.toPlace);
        cost_ksh = (TextView) findViewById(R.id.ksh_amount);
        domain_name = getString(R.string.web_domain_name);
        store = new PersistentCookieStore(this);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = searchView.getQuery().toString();
                List<Address> addressList = null;
                if (location != null || location.equals("")){
                    Geocoder geocoder = new Geocoder(MapsActivity.this);
                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address address = addressList.get(0);
                    // Empty search box
                    searchView.setQuery("",false);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
//                    Marker options disabled for searched location
//                    MarkerOptions options = new MarkerOptions();
//                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
//                    mMap.addMarker(options.position(latLng).title(location));
//                    Find search location with a zoom level
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        // in below line we are initializing our array list.
        mMarkerPoints = new ArrayList<>();
        addresses = new ArrayList<>();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mapView = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        fetchLocation();
    }

    /*
     * Called on clicking the confirm trip button
     * */
    public void confirmTrip(View view){
        Intent intent = new Intent(this, PaymentMethodActivity.class);
        TextView editText = (TextView) findViewById(R.id.ksh_amount);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    private void fetchLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null){
                    currentLocation = location;
                    Toast.makeText(getApplicationContext(),currentLocation.getLatitude()+", "+currentLocation.getLongitude(),Toast.LENGTH_SHORT).show();
                    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    assert mapFragment != null;
                    mapFragment.getMapAsync(MapsActivity.this);
                }
            }
        });
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        System.out.println("****The map is ready*****");
        place = new Geocoder(this, Locale.getDefault());
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        LatLng latLng = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                // If you already have 2 points
                if (mMarkerPoints.size()>1){
                    mMarkerPoints.clear();
                    addresses.clear();
                    mMap.clear();
                }
                // Adding new item to the list
                mMarkerPoints.add(latLng);
                try {
                    addresses.add(place.getFromLocation(latLng.latitude,latLng.longitude, 1).get(0));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Creating pointer
                MarkerOptions options = new MarkerOptions();

                // Setting position
                options.position(latLng);
                if (mMarkerPoints.size()==1){
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                } else if (mMarkerPoints.size()==2){
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                }
                // Add new marker to the API
                mMap.addMarker(options);
                // Check if start point is captured
                if (mMarkerPoints.size() >= 2){
                    mOrigin = mMarkerPoints.get(0);
                    mDestination = mMarkerPoints.get(1);
                    drawRoute();
                    origin.setText(addresses.get(0).getAddressLine(0));
                    destination.setText(addresses.get(1).getAddressLine(0));
                    synchronized (processed) {
                        String result = "";
                        try {
                            processed.wait();
                            result=FetchData(addresses.get(0).getAddressLine(0), addresses.get(1).getAddressLine(0));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        // ToDo
                        if (result.equals("ERROR:-Not found")) {
                            try {
                                String dist = distance.get("text").toString();
                                double cost = 0.03 * (int) distance.get("value");
                                DecimalFormat df = new DecimalFormat("0.00");
                                df.setRoundingMode(RoundingMode.UP);
                                cost_ksh.setText(df.format(cost));

                                // ToDo
                                LoadData(addresses.get(0).getAddressLine(0), addresses.get(1).getAddressLine(0), dist, cost);
                            } catch (InterruptedException | JSONException interruptedException) {
                                interruptedException.printStackTrace();
                            } catch (MalformedURLException malformedURLException) {
                                malformedURLException.printStackTrace();
                            } catch (URISyntaxException uriSyntaxException) {
                                uriSyntaxException.printStackTrace();
                            }
                        } else {
                            cost_ksh.setText(result);
                        }
                    }

                }
            }
        });

        btnCurrentLocation = ((View) mapView.getView().findViewById(Integer.parseInt("1")).
                getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) btnCurrentLocation.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.setMargins(0, 0, 30, 30);

    }

    void LoadData(String org, String dest, String dist, Double cost) throws MalformedURLException, URISyntaxException, InterruptedException {
        AsyncRequset P = new AsyncRequset("POST",this);
        P.setUrl(domain_name+"load_data/");
        P.setRequestBody("data="+"origin="+org+"#destination="+dest+"#distance="+dist+"#cost="+cost);
        P.start();
        P.join();
        System.out.println("Response:** "+P.getResponseBody());
    }

//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public String FetchData(String org, String dest) throws InterruptedException{
        AsyncRequset P=new AsyncRequset("POST", this);
        P.setUrl(domain_name+"fetch_data/");
        P.setRequestBody("data="+"origin="+org+"#destination="+dest);
        P.start();
        P.join();
        return P.getResponseBody();
    }

    @Override
    public boolean onMyLocationButtonClick(){
        Toast.makeText(this, "Your location ", Toast.LENGTH_SHORT).show();
        return false;
    }




    private void drawRoute(){
        // Get the url from google directions API
        String url = getDirectionsUrl(mOrigin,mDestination);
        DownloadTask downloadTask = new DownloadTask();
        //start downloading
        downloadTask.execute(url);
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest){
        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;
        // Destination of the route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;
        // key
        String key = "key="+getString(R.string.google_maps_key);
        // Web service parameters
        String parameters = str_origin+"&"+str_dest+"&"+key;
        // Output format
        String output = "json";
        // Build the url
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
            // Connection to url
            urlConnection.connect();
            // Read data form url
            inputStream = urlConnection.getInputStream();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while ((line= bufferedReader.readLine()) != null){
                stringBuffer.append(line);
            }
            data = stringBuffer.toString();
            bufferedReader.close();
        } catch (Exception e){
            Log.d("Error on download", e.toString());
        } finally {
            inputStream.close();
            urlConnection.disconnect();
        }
        JSONObject obj;
        try{
            obj = new JSONObject(data);
            JSONArray result_arr = obj.getJSONArray("routes");
            final int n = result_arr.length();
            JSONArray res1 = new JSONArray();
            for (int i=0; i<n;++i){
                res1 = result_arr.getJSONObject(i).getJSONArray("legs");
                if (res1 != null)
                    break;
            }
            synchronized (processed) {
                for (int i = 0; i < res1.length(); ++i) {
                    if (res1.getJSONObject(i).get("distance") != null) {
                        distance = (JSONObject) res1.getJSONObject(i).get("distance");
                    }
                }
                processed.notify();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    /*
    * Download form Google Directions
    * */
    private class DownloadTask extends AsyncTask<String, Void, String>{
        // Download in the background

        @Override
        protected String doInBackground(String... url) {
            //store web services
            String data = "";
            try {
                // Fetch data from the web service
                data = downloadUrl(url[0]);
                Log.d("DownloadTask","DownloadTask : " + data);
            } catch (Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }
        // Foreground processes

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }

    // Parsing Google Directions in JSON format
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>>>{
        // Background process

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String,String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                // Start Parsing
                routes = parser.parse(jObject);
            } catch (Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Foreground Process

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> results) {
            super.onPostExecute(results);
            ArrayList<LatLng> points = null;
            PolylineOptions polylineOptions = null;
            // Traverse all routes
            for (int i=0; i<results.size(); i++){
                points = new ArrayList<LatLng>();
                polylineOptions = new PolylineOptions();
                // Fetch the i-th route
                List<HashMap<String,String>> path = results.get(i);
                // Fetch all points on the i-th route
                for (int j=0; j<path.size(); j++){
                    HashMap<String,String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                // Add all points in the route to polyLineOption
                polylineOptions.addAll(points);
                polylineOptions.width(8);
                polylineOptions.color(Color.BLUE);
            }
            // Draw the polyline
            if(polylineOptions != null){
                if (mPolyline != null){
                    mPolyline.remove();
                }
                mPolyline = mMap.addPolyline(polylineOptions);
            } else {
                Toast.makeText(getApplicationContext(), "No route found", Toast.LENGTH_LONG).show();
            }
        }
    }
}