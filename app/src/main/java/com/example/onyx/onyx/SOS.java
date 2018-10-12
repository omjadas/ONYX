package com.example.onyx.onyx;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class SOS {
    public String id;
    public LatLng location;
    public String name;
    public Marker marker;

    public SOS(String id, LatLng location, String name, Marker marker) {
        this.id = id;
        this.location = location;
        this.name = name;
        this.marker = marker;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof SOS)) return false;
        SOS o = (SOS) obj;
        return this.id.equals(o.id);
    }
}
