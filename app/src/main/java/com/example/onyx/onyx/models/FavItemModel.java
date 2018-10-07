package com.example.onyx.onyx.models;


import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import javax.annotation.Nullable;

public class FavItemModel implements Comparable<FavItemModel>{

    Bitmap image;
    String number,title,view, distance,address,placeID;
    LatLng latlng;

    public FavItemModel(Bitmap image, String number, String title, String view, String address, @Nullable LatLng latlng, @Nullable String placeID) {
        this.image = image;
        this.number = number;
        this.title = title;
        this.view = view;
        this.distance = address;
        this.latlng = latlng;
        this.address = address;
        this.placeID = placeID;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFrequency() {
        return view;
    }

    public void setFrequency(String view) {
        this.view = view;
    }

    public String getAddress() {
        return distance;
    }

    public void setAddress(String distance) {
        this.distance = distance;
    }

    public void setLatlng(LatLng latlng){this.latlng = latlng;}

    public LatLng getLatlng(){return latlng;}

    public String getPlaceID() {
        return placeID;
    }

    public void setPlaceID(String placeID) {
        this.placeID = placeID;
    }

    @Override
    public int compareTo(@NonNull FavItemModel o) {
        //compare with number
        int result = this.number.toLowerCase().compareTo(o.number.toLowerCase());


            return result;

    }
}
