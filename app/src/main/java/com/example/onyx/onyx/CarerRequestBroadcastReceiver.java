package com.example.onyx.onyx;

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
        acceptCarerRequest(intentData.getString("senderId")).addOnSuccessListener(s -> {
            Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
        });
    }

    private Task<String> acceptCarerRequest(String id){
        Map<String, Object> data = new HashMap<>();
        data.put("sender", id);
        return mFunctions
                .getHttpsCallable("acceptCarerRequest")
                .call(data)
                .continueWith(task -> (String) task.getResult().getData());
    }

}
