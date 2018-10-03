package com.example.onyx.onyx.models;


import com.google.android.gms.maps.model.LatLng;

import javax.annotation.Nullable;

public class FavItemModel {

    Integer image;
    String number,title,view, distance;
    LatLng latlng;

    public FavItemModel(Integer image, String number, String title, String view, String distance, @Nullable LatLng latlng) {
        this.image = image;
        this.number = number;
        this.title = title;
        this.view = view;
        this.distance = distance;
        this.latlng = latlng;
    }

    public Integer getImage() {
        return image;
    }

    public void setImage(Integer image) {
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

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public void setLatlng(LatLng latlng){this.latlng = latlng;}

    public LatLng getLatlng(){return latlng;}
}
