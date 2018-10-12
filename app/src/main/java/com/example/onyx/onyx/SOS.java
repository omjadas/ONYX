package com.example.onyx.onyx;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class SOS {
    public LatLng location;
    public String name;
    public Marker marker;

    public SOS(LatLng location, String name, Marker marker) {
        this.location = location;
        this.name = name;
        this.marker = marker;
    }

    public SOS(LatLng location, String name) {
        this.location = location;
        this.name = name;
    }
}
