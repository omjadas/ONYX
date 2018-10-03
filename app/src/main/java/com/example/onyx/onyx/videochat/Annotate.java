package com.example.onyx.onyx.videochat;

import com.example.onyx.onyx.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Annotate {
    public static GoogleMap gm;

    private static final int COLOR_BLACK_ARGB = 0xff000000;
    private static final int COLOR_WHITE_ARGB = 0xffffffff;
    private static final int COLOR_GREEN_ARGB = 0xff388E3C;
    private static final int COLOR_PURPLE_ARGB = 0xff81C784;
    private static final int COLOR_ORANGE_ARGB = 0xffF57F17;
    private static final int COLOR_BLUE_ARGB = 0xffF9A825;

    private static final int POLYLINE_STROKE_WIDTH_PX = 12;
    private static final int POLYGON_STROKE_WIDTH_PX = 8;
    private static final int PATTERN_DASH_LENGTH_PX = 20;
    private static final int PATTERN_GAP_LENGTH_PX = 20;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem DASH = new Dash(PATTERN_DASH_LENGTH_PX);
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);

    private static LatLng lastClickLatLng;
    private static ArrayList<Polyline> directions = new ArrayList<>();
    private static ArrayList<LatLng> points = new ArrayList<>();

    // Create a stroke pattern of a gap followed by a dot.
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);

    // Create a stroke pattern of a gap followed by a dash.
    private static final List<PatternItem> PATTERN_POLYGON_ALPHA = Arrays.asList(GAP, DASH);
    public static boolean isAnnotating = false;

    /**
     * Styles the polyline, based on type.
     * @param polyline The polyline object that needs styling.
     */
    private static void stylePolyline(Polyline polyline) {
        String type = "";
        // Get the data object stored with the polyline.
        if (polyline.getTag() != null) {
            type = polyline.getTag().toString();
        }

        switch (type) {
            // If no type is given, allow the API to use the default.
            case "A":
                // Use a custom bitmap as the cap at the start of the line.
                polyline.setStartCap(
                        new CustomCap(
                                BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow), 10));
                break;
            case "B":
                // Use a round cap at the start of the line.
                polyline.setStartCap(new RoundCap());
                break;
        }

        polyline.setEndCap(new RoundCap());
        polyline.setWidth(POLYLINE_STROKE_WIDTH_PX);
        polyline.setColor(COLOR_BLACK_ARGB);
        polyline.setJointType(JointType.ROUND);
    }



    public static void drawLine(LatLng clickLocation, GoogleMap gm){
        if (points.size()>0){
            // Add polylines to the map.
            // Polylines are useful to show a route or some other connection between points.
            Polyline polyline1 = gm.addPolyline(new PolylineOptions()
                    .clickable(true)
                    .add(
                            clickLocation,
                            points.get(points.size()-1)));
            // Store a data object with the polyline, used here to indicate an arbitrary type.
            polyline1.setTag("A");
            // Style the polyline.
            stylePolyline(polyline1);
            directions.add(polyline1);

        }

        points.add(clickLocation);
    }

    public static void undo() {
        int size = directions.size();
        if(size > 0) {
            directions.get(size-1).remove();
            directions.remove(size-1);
            points.remove(points.size()-1);
        }else if(points.size()>0){
            points.remove(points.size()-1);
        }

    }

    public static void clear(){
        for (Polyline p : directions){
            p.remove();
        }
        directions = new ArrayList<>();
        points = new ArrayList<>();
    }
}
