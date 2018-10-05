package com.example.onyx.onyx.models;

import android.graphics.Bitmap;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.firestore.GeoPoint;

@IgnoreExtraProperties
public class FBFav {

    public String placeID;
    public Bitmap image;
    public String title;
    public GeoPoint latlng;
    public String address;
    public long timestamp;
    public int freq;


    public FBFav() {
    }

    public FBFav(String placeID,String title, Bitmap image, GeoPoint latlng, String address, int freq,long timestamp) {
        this.placeID = placeID;
        this.title = title;
        this.image = image;
        this.latlng = latlng;
        this.address = address;
        this.freq = freq;
        this.timestamp = timestamp;

    }

}
