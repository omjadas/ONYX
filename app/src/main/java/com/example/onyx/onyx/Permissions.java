package com.example.onyx.onyx;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class Permissions {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int PERMISSIONS_REQUEST_ACCESS_MIC = 1;
    private static final int PERMISSIONS_REQUEST_ACCESS_CAMERA = 1;
    private static final int CAMERA_MIC_PERMISSION_REQUEST_CODE = 1;

    public static void getPermissions(Context context, Activity activity) {
        getLocationPermission(context, activity);
        getVoicePermission(context, activity);
        getCameraPermission(context, activity);
    }

    public static void getLocationPermission(Context context, Activity activity){
        if (!hasLocationPermission(context, activity)){
            ActivityCompat.requestPermissions(activity,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public static void getVoicePermission(Context context, Activity activity){
        if (!hasLocationPermission(context, activity)){
            ActivityCompat.requestPermissions(activity,
                    new String[]{android.Manifest.permission.RECORD_AUDIO},
                    PERMISSIONS_REQUEST_ACCESS_MIC);
        }
    }

    public static void getCameraPermission(Context context, Activity activity){
        if (!hasLocationPermission(context, activity)){
            ActivityCompat.requestPermissions(activity,
                    new String[]{android.Manifest.permission.CAMERA},
                    PERMISSIONS_REQUEST_ACCESS_CAMERA);
        }
    }

    public static boolean hasLocationPermission(Context context, Activity activity) {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasVoicePermission(Context context, Activity activity) {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasCameraPermission(Context context, Activity activity){
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}
