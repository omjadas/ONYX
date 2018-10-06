package com.example.onyx.onyx;

import android.util.Log;

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
import com.google.firebase.firestore.GeoPoint;

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

    private static boolean newLine = true;
    private static ArrayList<Line> lines = new ArrayList<>();

    private static final int POLYLINE_STROKE_WIDTH_PX = 12;
    private static final int POLYGON_STROKE_WIDTH_PX = 8;
    private static final int PATTERN_DASH_LENGTH_PX = 20;
    private static final int PATTERN_GAP_LENGTH_PX = 20;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem DASH = new Dash(PATTERN_DASH_LENGTH_PX);
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);

    private static LatLng lastClickLatLng;

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

    public static void drawMultipleLines(ArrayList<LatLng> p){
        //TODO change gm to mMap
        if(gm != null) {
            for (LatLng point : p) {
                drawLine(point, gm);
            }
        }

    }

    public static void drawLine(LatLng clickLocation, GoogleMap gm){
        //TODO gross
        if(Annotate.gm != null) {
            gm = Annotate.gm;
        }else {
            Annotate.gm = gm;
        }
        if(lines.size() > 0 && !newLine) {
            Line currentLine = lines.get(lines.size() - 1);
            if(currentLine.points.size() > 0) {
                // Add polylines to the map.
                // Polylines are useful to show a route or some other connection between points.
                Log.d("drawLine2", clickLocation.toString());
                //Log.d("drawLine", gm.toString());
                Polyline polyline1 = gm.addPolyline(new PolylineOptions()
                        .clickable(true)
                        .add(
                                clickLocation,
                                currentLine.points.get(currentLine.points.size() - 1)));
                // Store a data object with the polyline, used here to indicate an arbitrary type.
                polyline1.setTag("A");
                // Style the polyline.
                stylePolyline(polyline1);
                currentLine.directions.add(polyline1);
            }
            currentLine.points.add(clickLocation);
        }else {
            Line line = new Line();
            line.addPoint(clickLocation);
            lines.add(line);
            newLine = false;
        }
    }


    public static void undo() {
        if(lines.size() > 0) {
            newLine = false;
            Line currentLine = lines.get(lines.size() - 1);
            int pointsSize = currentLine.points.size();
            if(pointsSize > 0){
                currentLine.points.remove( pointsSize - 1);
                int directionsSize = currentLine.directions.size();
                if(directionsSize > 0) {
                    currentLine.directions.get(directionsSize - 1).remove();
                    currentLine.directions.remove(directionsSize - 1);
                }
                if(pointsSize-1 == 0)
                    lines.remove(currentLine);
            }
        }
    }

    public static void clear(){
        for (Line l : lines){
            l.clear();
        }
        lines = new ArrayList<>();
    }

    public static ArrayList<ArrayList<GeoPoint>> getPoints(){
        ArrayList<ArrayList<GeoPoint>> p = new ArrayList<>();
        for(Line l : lines){
            p.add(l.getPoints());
        }
        return p;
    }

    public static void setMap(GoogleMap mMap) {
        if(gm == null)
            gm = mMap;
    }

    public static void newAnnotation() {
        newLine = true;
    }

    private static class Line {
        private ArrayList<LatLng> points = new ArrayList<>();
        private ArrayList<Polyline> directions = new ArrayList<>();
        public Line(){

        }
        public void addPoint(LatLng point) {
            points.add(point);
        }

        public void clear(){
            for (Polyline p : directions){
                if(p!= null)
                    p.remove();
            }
            directions = new ArrayList<>();
            points = new ArrayList<>();
        }

        public ArrayList<GeoPoint> getPoints(){
            ArrayList<GeoPoint> gp = new ArrayList<>();
            for(LatLng p : points){
                gp.add(new GeoPoint(p.latitude, p.longitude));
            }
            return gp;
        }
    }
}
