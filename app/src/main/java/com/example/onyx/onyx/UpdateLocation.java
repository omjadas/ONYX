package com.example.onyx.onyx;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import static android.support.constraint.Constraints.TAG;

@SuppressLint("Registered")
public class UpdateLocation extends Service {

    public void onCreate() {

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
