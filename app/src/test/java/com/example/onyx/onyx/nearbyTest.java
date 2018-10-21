package com.example.onyx.onyx;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class nearbyTest {

    String expected = "Lydiard Street North, Ballarat Central";
    String expectedNearbyStyle = "rating*place_name*place_id*lat*lng*vicinity*";
    String url;
    String data;
    MapsFragment mapsFragment = new MapsFragment();
    getNearbyPlaces nearbyPlaces = new getNearbyPlaces();

    /**
     * This is a test to check the building and production of URLs for nearby places
     * It uses two URLs - Ballarat Central, and The State Library
     * The test shows that the two URLs provide data and that their data is distinct from each other
     */
    @Test
    public void builtUrlAvailable(){
        url = mapsFragment.buildUrl(-37.5619376,143.8551657,"train_station");
        String url2 = mapsFragment.buildUrl(-37.8098,144.9652,"train_station");
        assertNotNull(url);
        assertNotNull(url2);
        assertNotSame(url2,url);
    }

    /**
     * The second test focuses on the correctness of data rather than it's existence
     * The received data is analysed to see if it contains the substring that defines the vicinity
     * of Ballarat station
     * If it contains the substring, the test is passed
     */
    @Test
    public void readUrlTest(){
        try{
            url = mapsFragment.buildUrl(-37.5619376,143.8551657,"train_station");
            data = nearbyPlaces.readUrl(url);
            assertNotNull(data);
            assertTrue(data.contains(expected));
        }
        catch(IOException e){

        }
    }

    /**
     * This final test sees if the output custom JSONarray matches the desired output
     */
    @Test
    public void parseDataTest(){
        List<HashMap<String, String>> nearbyPlaceData;
        JSONObject jsonObject;
        JSONArray jsonArray = null;
        try {
            jsonObject = new JSONObject(data);
            jsonArray = jsonObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(jsonArray != null){
            nearbyPlaceData = nearbyPlaces.getPlaces(jsonArray);
            assertNotNull(nearbyPlaceData);
            assertTrue(nearbyPlaceData.toString().contains(expectedNearbyStyle));
        }
    }
}
