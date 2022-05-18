package com.example.damagewear;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DirectionsJSONParser {
    private static final String TAG = DirectionsJSONParser.class.getSimpleName();
    public List<List<HashMap<String,String>>> parse(JSONObject jObject){
        List<List<HashMap<String,String>>> routes = new ArrayList<>();
        JSONArray jRoutes;
        JSONArray jLegs;
        JSONArray jSteps;

        try {
            jRoutes = jObject.getJSONArray("routes");
            // Traverse routes
            for (int i=0; i<jRoutes.length(); i++){
                jLegs = ((JSONObject)jRoutes.get(i)).getJSONArray("legs");
                List path = new ArrayList<HashMap<String,String>>();

                // Traversing legs
                for (int j=0; j<jLegs.length(); j++){
                    jSteps = ((JSONObject)jLegs.get(j)).getJSONArray("steps");

                    //Traversing steps
                    for (int k=0; k<jSteps.length(); k++){
                        String polyline = "";
                        polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");
                        List<LatLng> list = decodePoly(polyline);

                        // Traverse all points
                        for (int l=0; l<list.size();l++){
                            HashMap<String,String> hashMap = new HashMap<String,String>();
                            hashMap.put("lat",Double.toString(((LatLng)list.get(l)).latitude));
                            hashMap.put("lng",Double.toString(((LatLng)list.get(l)).longitude));
                            path.add(hashMap);
                        }
                    }
                    routes.add(path);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return routes;
    }

    /*
    * Decoding polygon points
    * */
    private List<LatLng> decodePoly(String encode){
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encode.length();
        int lat = 0, lng = 0;
        while (index < len){
            int b, shift = 0, result = 0;
            do {
                b = encode.charAt(index++) - 63;
                result |= (b &0x1f) << shift;
                shift += 5;
            }
            while (b >= 0x20);
            int dlat = ((result &1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encode.charAt(index++)-63;
                result |= (b &0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result &1) !=0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            LatLng p = new LatLng((((double) lat / 1E5)), (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }
}
