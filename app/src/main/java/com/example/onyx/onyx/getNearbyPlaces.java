package com.example.onyx.onyx;

import android.os.AsyncTask;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


/**
 * Asynchronous class for processing Google Place data for nearby searches
 * This class was made following the tutorial of Tech Academy
 */
public class getNearbyPlaces extends AsyncTask<Object, String, String> {

    GoogleMap mMap;
    String url;
    String googlePlaceData;


    /**
     * Perform asynchronous task
     *
     * @param objects contains nMap (reference to map from map fragment) and url (URL for querying Google Places, built in map fragment)
     * @return
     */
    @Override
    protected String doInBackground(Object... objects) {
        mMap = (GoogleMap) objects[0];
        url = (String) objects[1];

        try {
            googlePlaceData = readUrl(url);
        } catch (IOException ignored) {

        }

        return googlePlaceData;
    }


    /**
     * Begin parsing of JSON file by handing it to getPlaces
     * Once parsing is complete, hand list of places to showNearby to be drawn
     *
     * @param s string version of JSON file received in readUrl
     */
    @Override
    protected void onPostExecute(String s) {
        List<HashMap<String, String>> nearbyPlaces;
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(s);
            jsonArray = jsonObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        nearbyPlaces = getPlaces(Objects.requireNonNull(jsonArray));
        showNearby(nearbyPlaces);
    }


    /**
     * Loop over list of given locations from the received JSON file
     * Delegate parsing to getPlace
     *
     * @param jsonArray Collection of all locations in JSON file
     * @return list of formatted locations
     */
    private List<HashMap<String, String>> getPlaces(JSONArray jsonArray) {
        List<HashMap<String, String>> placeList = new ArrayList<>();
        HashMap<String, String> placeMap;

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                placeMap = getPlace((JSONObject) jsonArray.get(i));
                placeList.add(placeMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return placeList;
    }


    /**
     * Format data for a single location from given location data in a JSON object
     * Format models the way markers are drawn in the maps fragment
     *
     * @param jsonObject Representation of a single location in JSON file
     * @return
     */
    private HashMap<String, String> getPlace(JSONObject jsonObject) {
        HashMap<String, String> googlePlace = new HashMap<>();
        String placeName = "--NA--";
        String vicinity = "--NA--";
        String latitude;
        String longitude;
        String rating;
        String placeId;

        try {
            if (!jsonObject.isNull("name")) {
                placeName = jsonObject.getString("name");
            }
            if (!jsonObject.isNull("vicinity")) {
                vicinity = jsonObject.getString("vicinity");
            }
            if (!jsonObject.isNull("rating")) {
                rating = jsonObject.getString("rating");
            } else {
                rating = "0.0";
            }

            latitude = jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lng");
            placeId = jsonObject.getString("place_id");


            googlePlace.put("place_name", placeName);
            googlePlace.put("vicinity", vicinity);
            googlePlace.put("lat", latitude);
            googlePlace.put("lng", longitude);
            googlePlace.put("rating", rating);
            googlePlace.put("place_id", placeId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return googlePlace;
    }


    /**
     * Use given url to query Google Places and receive JSON file
     *
     * @param myUrl URL formatted to query Google Places
     * @return
     * @throws IOException
     */
    private String readUrl(String myUrl) throws IOException {
        String data = "";
        InputStream stream = null;
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL(myUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            stream = url.openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            StringBuffer sb = new StringBuffer();

            String line;

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            br.close();
        } catch (IOException ignored) {

        } finally {
            Objects.requireNonNull(stream).close();
            Objects.requireNonNull(urlConnection).disconnect();
        }
        return data;
    }


    /**
     * Create a marker for each location in the list and add it to the map
     *
     * @param nearbyPlaces A list of locations, with data stored in a hashmap
     */
    private void showNearby(List<HashMap<String, String>> nearbyPlaces) {
        for (int i = 0; i < nearbyPlaces.size(); i++) {
            MarkerOptions markerOptions = new MarkerOptions();
            HashMap<String, String> googlePlace = nearbyPlaces.get(i);

            LatLng latLng = new LatLng(Double.parseDouble(googlePlace.get("lat")),
                    Double.parseDouble(googlePlace.get("lng")));
            markerOptions.position(latLng);
            markerOptions.title(googlePlace.get("place_name"));
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            markerOptions.draggable(true);

            ArrayList<String> snipArray = new ArrayList<>();
            snipArray.add(googlePlace.get("rating"));
            snipArray.add("Tap to add this place to favrourites!");
            snipArray.add(googlePlace.get("place_id"));
            snipArray.add(googlePlace.get("vicinity").replaceAll(",", " "));
            snipArray.add(latLng.latitude + "");
            snipArray.add(latLng.longitude + "");
            markerOptions.snippet(snipArray.toString());

            mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
    }

}
