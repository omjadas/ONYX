package com.example.onyx.onyx;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static android.support.constraint.Constraints.TAG;

public class LocationService extends Service {
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationThread thread;

    class LocationThread extends Thread {
        public void run() {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Map<String, Object> locationMap = new HashMap<>();
                while (true) {
                    Location location = getLocation();
                    locationMap.put("currentLocation", new GeoPoint(location.getLatitude(),location.getLongitude()));
                    db.collection("users").document(user.getUid()).update("currentLocation",new GeoPoint(location.getLatitude(), location.getLongitude()));
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Log.e(TAG, "onHandleIntent: user not logged in");
            }
        }
    }

    public void onCreate() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        thread = new LocationThread();
        thread.start();
        return START_STICKY;
    }

    public Location getLocation() {
        final Location[] location = new Location[1];
        final boolean[] locationReady = new boolean[1];
        locationReady[0] = false;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location currentLocation) {
                        if (currentLocation != null) {
                            location[0] = currentLocation;
                        }
                        locationReady[0] = true;
                    }
                });
        while (!locationReady[0]);
        return location[0];
    }

    public void onDestroy() {
        thread.interrupt();
        thread = null;
        Log.e("LocationExit", "Location service has been destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
