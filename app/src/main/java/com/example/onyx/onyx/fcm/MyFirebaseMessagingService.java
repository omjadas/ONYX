package com.example.onyx.onyx.fcm;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.example.onyx.onyx.ReopenChatActivity;
import com.example.onyx.onyx.R;
import com.example.onyx.onyx.events.PushNotificationEvent;
import com.example.onyx.onyx.ui.activities.ChatActivity;
import com.example.onyx.onyx.utils.Constants;
import com.example.onyx.onyx.utils.SharedPrefUtil;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    static final String GOOGLE_AUTH_TAG = "GOOGLE AUTHENTICATION: ";
    static final String FIRESTORE_WRITE_TAG = "ADDITION TO DATABASE: ";
    static final String SUCCESS = "SUCCESS";
    static final String FAILURE = "FAILURE";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            if (remoteMessage.getData().get("type").equals("carerRequest")) {
                sendCarerNotification(remoteMessage);
                return;
            }

            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            String title = remoteMessage.getData().get("title");
            String message = remoteMessage.getData().get("text");
            String username = remoteMessage.getData().get("username");
            String uid = remoteMessage.getData().get("uid");
            String fcmToken = remoteMessage.getData().get("fcm_token");

            // Don't show notification if chat activity is open.
            if (!ReopenChatActivity.isChatActivityOpen()) {
                sendChatNotification(title,
                        message,
                        username,
                        uid);
            } else {
                EventBus.getDefault().post(new PushNotificationEvent(title,
                        message,
                        username,
                        uid));
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendCarerNotification(RemoteMessage remoteMessage) {
        String senderName = remoteMessage.getData().get("senderName");

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String CHANNEL_ID = "my_channel_01";
        CharSequence name = "my_channel";
        String Description = "This is my channel";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
        mChannel.setDescription(Description);
        mChannel.enableLights(true);
        mChannel.setLightColor(Color.RED);
        mChannel.enableVibration(true);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        mChannel.setShowBadge(false);
        Log.d("chanel","coco");
        notificationManager.createNotificationChannel(mChannel);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification notificationBuilder = new Notification.Builder(this,CHANNEL_ID)
                .setContentTitle("Care requested")
                .setContentText(senderName + " needs assistance")
                .setSmallIcon(R.drawable.ic_messaging)
                .build();


        int uniqID = createID();
        Log.d("aaaaa", String.valueOf(uniqID));
        notificationManager.notify(uniqID, notificationBuilder);
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendChatNotification(String title,
                                      String message,
                                      String receiver,
                                      String receiverUid) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(Constants.ARG_RECEIVER, receiver);
        intent.putExtra(Constants.ARG_RECEIVER_UID, receiverUid);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {


            String CHANNEL_ID = "my_channel_01";
            CharSequence name = "my_channel";
            String Description = "This is my channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(false);
            Log.d("chanel","coco");
            notificationManager.createNotificationChannel(mChannel);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            Notification notificationBuilder = new Notification.Builder(this,CHANNEL_ID)
                    .setContentTitle("New Message from: " + receiver)
                    .setContentText(message)
                    .setStyle(new Notification.BigTextStyle()
                            .bigText(message))
                    .setSmallIcon(R.drawable.ic_messaging)
                    .setContentIntent(pendingIntent)
                    .build();


            int uniqID = createID();
            Log.d("aaaaa", String.valueOf(uniqID));
            notificationManager.notify(uniqID, notificationBuilder);
        }


    }

    //generate notification id
    public int createID(){
        Date now = new Date();
        int id = Integer.parseInt(new SimpleDateFormat("ddHHmmss",  Locale.US).format(now));
        return id;
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.e("NEW_TOKEN", s);
        sendRegistrationToServer(s);
    }

    private void sendRegistrationToServer(final String token) {
        new SharedPrefUtil(getApplicationContext()).saveString(Constants.ARG_FIREBASE_TOKEN, token);

            /*
            FirebaseDatabase.getInstance()
                    .getReference()
                    .child(Constants.ARG_USERS)
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(Constants.ARG_FIREBASE_TOKEN)
                    .setValue(token);
            */

    }

}