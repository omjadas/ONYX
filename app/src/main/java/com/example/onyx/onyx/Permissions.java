package com.example.onyx.onyx;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import static com.example.onyx.onyx.MapsFragment.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;

public class Permissions {

    private static final int CAMERA_MIC_PERMISSION_REQUEST_CODE = 1;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    public static void getPermissions(Context context, Activity activity) {
        // location permission
        if (ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

        }

        // audio and video permissions
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA) ||
                ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        Manifest.permission.RECORD_AUDIO)) {
        } else {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                    CAMERA_MIC_PERMISSION_REQUEST_CODE);
        }
    }

    public static void getLocationPermission(Context context, Activity activity){
        if (!hasLocationPermission(context, activity)){
            ActivityCompat.requestPermissions(activity,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public static boolean hasLocationPermission(Context context, Activity activity) {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasVoicePermission(Context context, Activity activity) {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasVideoPermission(Context context, Activity activity){
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}
