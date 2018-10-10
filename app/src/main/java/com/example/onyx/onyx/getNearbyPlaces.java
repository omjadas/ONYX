package com.example.onyx.onyx;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class getNearbyPlaces extends AsyncTask<Object, String, String> {

    GoogleMap mMap;
    String url;
    String googlePlaceData;

    @Override
    protected String doInBackground(Object... objects) {
        Log.d("getNearbyPlaces: ", "executing...");
        mMap = (GoogleMap) objects[0];
        url = (String) objects[1];

        try{
            googlePlaceData = readUrl(url);
        }
        catch (IOException e){

        }

        return googlePlaceData;
    }

    @Override
    protected void onPostExecute(String s) {
        List<HashMap<String,String>> nearbyPlaces;
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        try{
            jsonObject = new JSONObject(s);
            jsonArray = jsonObject.getJSONArray("results");
        }
        catch(JSONException e){
            e.printStackTrace();
        }

        nearbyPlaces = getPlaces(jsonArray);
        showNearby(nearbyPlaces);
    }

    private List<HashMap<String,String>> getPlaces(JSONArray jsonArray){
        Log.d("Places: ", "parsing");
        System.out.println(jsonArray.toString());
        List<HashMap<String,String>> placeList = new ArrayList<>();
        HashMap<String,String> placeMap = null;

        for(int i = 0; i < jsonArray.length(); i++){
            try{
                placeMap = getPlace((JSONObject) jsonArray.get(i));
                placeList.add(placeMap);
            }
            catch(JSONException e){
                e.printStackTrace();
            }
        }
        return placeList;
    }

    private HashMap<String,String> getPlace(JSONObject jsonObject){
        Log.d("Place: ", "getting");
        HashMap<String,String> googlePlace = new HashMap<>();
        String placeName = "--NA--";
        String vicinity = "--NA--";
        String latitude = "";
        String longitude = "";
        String rating;
        String placeId;

        try{
            if(!jsonObject.isNull("name")){
                placeName = jsonObject.getString("name");
            }
            if(!jsonObject.isNull("vicinity")){
                vicinity = jsonObject.getString("vicinity");
            }

            latitude = jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lng");
            rating = jsonObject.getString("rating");
            placeId = jsonObject.getString("place_id");


            googlePlace.put("place_name", placeName);
            googlePlace.put("vicinity", vicinity);
            googlePlace.put("lat", latitude);
            googlePlace.put("lng", longitude);
            googlePlace.put("rating",rating);
            googlePlace.put("place_id",placeId);
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        return googlePlace;
    }

    private String readUrl(String myUrl) throws IOException{
        Log.d("url: ", "reading");
        String data = "";
        InputStream stream = null;
        HttpURLConnection urlConnection = null;

        try{
            URL url = new URL(myUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            stream = url.openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            StringBuffer sb = new StringBuffer();

            String line = "";

            while ((line = br.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();
            br.close();
        }
        catch (MalformedURLException e){

        }
        catch (IOException e){

        }
        finally{
            stream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private void showNearby(List<HashMap<String,String>> nearbyPlaces){
        for(int i = 0; i < nearbyPlaces.size(); i++){
            Log.d("Nearby: ","showing");
            MarkerOptions markerOptions = new MarkerOptions();
            HashMap<String,String> googlePlace = nearbyPlaces.get(i);

            LatLng latLng = new LatLng(Double.parseDouble(googlePlace.get("lat")),
                    Double.parseDouble(googlePlace.get("lng")));
            markerOptions.position(latLng);
            markerOptions.title(googlePlace.get("place_name"));
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

            ArrayList<String> snipArray = new ArrayList<>();
            snipArray.add(googlePlace.get("rating"));
            snipArray.add("Tap to add this place to favrourites!");
            snipArray.add(googlePlace.get("place_id"));
            snipArray.add(googlePlace.get("vicinity").replaceAll(","," "));
            snipArray.add(latLng.latitude + "");
            snipArray.add(latLng.longitude + "");
            markerOptions.snippet(snipArray.toString());


            mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
        }
    }

}
