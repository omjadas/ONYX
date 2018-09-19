package com.example.onyx.onyx;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
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
                    locationMap.put("currentLocation", location);
                    db.collection("users").document(user.getUid()).set(locationMap);
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
        Task<Location> locationResult = mFusedLocationClient.getLastLocation();
        locationResult.addOnCompleteListener((Executor) this, new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    location[0] = task.getResult();
                }
            }
        });
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
