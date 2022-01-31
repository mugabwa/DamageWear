package com.example.damagewear;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import android.os.Bundle;
import android.os.Looper;
import android.util.AttributeSet;
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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Permission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        searchView = findViewById(R.id.idSearchView);
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
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
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
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
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
                    mMap.clear();
                }
                // Adding new item to the list
                mMarkerPoints.add(latLng);
                // Creating pointer
                MarkerOptions options = new MarkerOptions();

                // Setting position
                options.position(latLng);
                if (mMarkerPoints.size()==1){
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                } else if (mMarkerPoints.size()==2){
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }
                // Add new marker to the API
                mMap.addMarker(options);
                // Check if start point is captured
                if (mMarkerPoints.size() >= 2){
                    mOrigin = mMarkerPoints.get(0);
                    mDestination = mMarkerPoints.get(1);
                    drawRoute();
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