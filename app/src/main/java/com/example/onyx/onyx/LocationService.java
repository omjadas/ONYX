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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.concurrent.TimeUnit;

import static android.support.constraint.Constraints.TAG;

public class LocationService extends Service {
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationThread thread;
    private FirebaseUser user;
    private FirebaseFirestore db;

    /**
     *
     */
    public void onCreate() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Creates and starts a thread that runs in the background updating user location.
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    public int onStartCommand(Intent intent, int flags, int startId) {
        thread = new LocationThread();
        thread.start();
        return START_STICKY;
    }

    /**
     * Gets the user location from mFusedLocationClient and passes it to the
     * {@link #updateLocation(Location) updateLocation} method.
     */
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ;
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(currentLocation -> {
                    if (currentLocation != null) {
                        updateLocation(currentLocation);
                    }
                });
    }

    /**
     * Converts location to Firebase GeoPoint and then updates it in Cloud Firestore.
     *
     * @param location Object containing user location.
     */
    private void updateLocation(Location location) {
        db.collection("users").document(user.getUid()).update("currentLocation", new GeoPoint(location.getLatitude(), location.getLongitude()));
    }

    /**
     * Interrupts the thread and sets it to null.
     */
    public void onDestroy() {
        //thread.interrupt();
        //thread = null;
        Log.e("LocationExit", "Location service has been destroyed");
    }

    /**
     * @param intent
     * @return
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class LocationThread extends Thread {
        /**
         * Updates location stored in Cloud Firestore every five seconds.
         */
        public void run() {
            if (user != null) {
                //thread runs constantly
                while (true) {
                    // returns if the thread is interrupted
                    if (isInterrupted()) {
                        return;
                    }
                    getLocation();
                    // sleep for five seconds
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
}
