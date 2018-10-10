package com.example.onyx.onyx.fcm;

public class FirebaseData {

    public static int CARER_REQUEST_NOTIFICATION_ID;
    public static String RECEIVER_ID;

    public static void setData(int notificationId, String receiverId) {
        CARER_REQUEST_NOTIFICATION_ID = notificationId;
        RECEIVER_ID = receiverId;
    }

    public static String getId(){
        if (RECEIVER_ID != null){
            return RECEIVER_ID;
        }

        return "";
    }

}
