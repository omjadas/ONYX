package com.example.onyx.onyx;

import android.util.Log;

public class IdGenerator {

    private static final String TAG = "IdGenerator";

    public static String getRoomId(String user1, String user2){
        final String room_id;

        int compare = user1.compareTo(user2);
        if (compare < 0) {
            room_id = user1 + "_" + user2;
        } else if (compare > 0) {
            room_id = user2 + "_" + user1;
        } else {
            room_id = user1 + "_" + user2;
            Log.e(TAG, "Same id");
        }

        return room_id;
    }

}
