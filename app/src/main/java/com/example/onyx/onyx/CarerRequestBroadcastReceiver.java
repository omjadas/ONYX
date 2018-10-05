package com.example.onyx.onyx;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.HashMap;
import java.util.Map;

public class CarerRequestBroadcastReceiver extends BroadcastReceiver{

    private FirebaseFunctions mFunctions;

    @Override
    public void onReceive(Context context, Intent intent){
        mFunctions = FirebaseFunctions.getInstance();
        Bundle intentData = intent.getExtras();
        String id = intentData.getString("senderId");
        if(!id.equalsIgnoreCase("")){
            acceptCarerRequest(id).addOnSuccessListener(s -> {
                Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
            });
        }
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationId = intent.getIntExtra("notificationId", 0);
        manager.cancel(22);
    }

    private Task<String> acceptCarerRequest(String id){
        Map<String, Object> data = new HashMap<>();
        data.put("receiver", id);
        return mFunctions
                .getHttpsCallable("acceptCarerRequest")
                .call(data)
                .continueWith(task -> (String) task.getResult().getData());
    }

}
