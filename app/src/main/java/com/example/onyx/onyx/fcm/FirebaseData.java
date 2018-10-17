package com.example.onyx.onyx.fcm;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Objects;

public class FirebaseData {

    public static int CARER_REQUEST_NOTIFICATION_ID;
    public static String RECEIVER_ID;

    public static void setData(int notificationId, String receiverId) {
        CARER_REQUEST_NOTIFICATION_ID = notificationId;
        RECEIVER_ID = receiverId;
    }

    public static String getId() {
        if (RECEIVER_ID != null) {
            return RECEIVER_ID;
        }

        return "";
    }

    public static String getUserId() {
        return Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    }

}
