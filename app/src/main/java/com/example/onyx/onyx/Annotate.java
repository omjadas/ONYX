package com.example.onyx.onyx;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Annotate {
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
    // Create a stroke pattern of a gap followed by a dot.
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);
    // Create a stroke pattern of a gap followed by a dash.
    private static final List<PatternItem> PATTERN_POLYGON_ALPHA = Arrays.asList(GAP, DASH);
    private static boolean newLine = true;
    private static ArrayList<Line> lines = new ArrayList<>();
    private GoogleMap gm;
    private boolean annotating = false;
    private boolean undoHasOccurred = false;

    Annotate(GoogleMap gm) {
        this.gm = gm;
    }

    /**
     * Styles the polyline, based on type.
     *
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

    public void drawMultipleLines(ArrayList<LatLng> p) {
        for (LatLng point : p) {
            drawLine(point);
        }
    }

    public void drawLine(LatLng clickLocation) {
        if (gm != null) {
            if (lines.size() > 0 && !newLine) {
                Line currentLine = lines.get(lines.size() - 1);
                //Can't draw a point with only one point
                if (currentLine.points.size() > 0) {
                    // Add polyline to the map.
                    Polyline polyline = gm.addPolyline(new PolylineOptions()
                            .clickable(true)
                            .add(
                                    clickLocation,
                                    currentLine.points.get(currentLine.points.size() - 1)));
                    // Store a data object with the polyline, used here to indicate an arbitrary type.
                    polyline.setTag("A");
                    // Style the polyline.
                    stylePolyline(polyline);
                    currentLine.directions.add(polyline);
                }

                currentLine.points.add(clickLocation);
            } else {
                //Create a new line and add it to list
                Line line = new Line();
                line.addPoint(clickLocation);
                lines.add(line);
                newLine = false;
            }
        }
    }


    public void undo() {
        //undo new Line and stay with current line
        if (newLine) {
            newLine = false;
        }
        //otherwise, undo latest point in current line
        else if (lines.size() > 0) {

            Line currentLine = lines.get(lines.size() - 1);
            int pointsSize = currentLine.points.size();

            if (pointsSize > 0) {
                currentLine.points.remove(pointsSize - 1);
                int directionsSize = currentLine.directions.size();
                //Remove direction if one exists
                if (directionsSize > 0) {
                    currentLine.directions.get(directionsSize - 1).remove();
                    currentLine.directions.remove(directionsSize - 1);
                }
            }

            //Destroy polyline if no points exist
            if (pointsSize - 1 <= 0) {
                newLine = true;
                lines.remove(currentLine);
            }

        }

        //if undo has occured, lines may need to be resent
        setUndoHasOccurred(true);
    }

    //clear all lines
    public void clear() {
        for (Line l : lines) {
            l.clear();
        }
        lines = new ArrayList<>();
    }

    //Returns annotations as arraylist of arraylist of geopoints
    public ArrayList<ArrayList<GeoPoint>> getAnnotations() {
        ArrayList<ArrayList<GeoPoint>> p = new ArrayList<>();
        for (Line l : lines) {
            if (!l.hasBeenSent) {
                p.add(l.getPoints());
            }
        }
        return p;
    }

    public void setMap(GoogleMap mMap) {
        if (gm == null)
            gm = mMap;
    }

    public void newAnnotation() {
        newLine = true;
    }

    public void successfulSend(ArrayList<GeoPoint> p) {
        for (Line l : lines) {
            if (!l.hasBeenSent) {
                if (l.getPoints().equals(p)) {
                    l.hasBeenSent = true;
                }
            }
        }
    }

    public boolean hasUndoOccurred() {
        return undoHasOccurred;
    }

    public boolean isAnnotating() {
        return annotating;
    }

    public void setAnnotating(boolean annotating) {
        this.annotating = annotating;
    }

    public void setUndoHasOccurred(boolean b) {
        undoHasOccurred = b;
        for (Line l : lines) {
            l.hasBeenSent = false;
        }
    }

    //Created custom line class for more control
    private class Line {
        public boolean hasBeenSent = false;
        private ArrayList<LatLng> points = new ArrayList<>();
        private ArrayList<Polyline> directions = new ArrayList<>();

        public Line() {

        }

        public void addPoint(LatLng point) {
            points.add(point);
        }

        public void clear() {
            for (Polyline p : directions) {
                if (p != null)
                    p.remove();
            }
            directions = new ArrayList<>();
            points = new ArrayList<>();
        }

        public ArrayList<GeoPoint> getPoints() {
            ArrayList<GeoPoint> gp = new ArrayList<>();
            for (LatLng p : points) {
                gp.add(new GeoPoint(p.latitude, p.longitude));
            }
            return gp;
        }
    }
}
