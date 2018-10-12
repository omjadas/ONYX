package com.example.onyx.onyx;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class Permissions {

    private static final String TAG = "Onyx/Permissions";

    private static final int PERMISSION_REQUEST_CODE = 1;

    public static boolean getPermissions(Context context, Activity activity) {
        if (!hasLocationPermission(context) || !hasVoicePermission(context) || !hasCameraPermission(context)) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CODE);
            Log.d(TAG, "Asked for permissions");
            return true;
        }

        return false;
    }

    public static boolean hasLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasVoicePermission(Context context) {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasCameraPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }
}
