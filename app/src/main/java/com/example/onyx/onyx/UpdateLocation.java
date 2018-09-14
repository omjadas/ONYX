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

import static android.support.constraint.Constraints.TAG;

public class UpdateLocation extends Service {
    private FusedLocationProviderClient mFusedLocationClient;


    public void onCreate() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Map<String, Object> location = new HashMap<>();
            db.collection("users").document(user.getUid()).set(location);
        } else {
            Log.e(TAG, "onHandleIntent: user not logged in");
        }
        Task<Location> locationResult = mFusedLocationClient.getLastLocation();
        final Location[] location = new Location[1];
        locationResult.addOnCompleteListener((Executor) this, new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    location[0] = task.getResult();
                } else {

                }
            }
        });
        return START_STICKY;
    }


    public void onDestroy() {
        Log.e("LocationExit", "Location service has been destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
