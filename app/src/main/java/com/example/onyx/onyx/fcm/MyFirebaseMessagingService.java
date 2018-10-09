package com.example.onyx.onyx.fcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.onyx.onyx.CarerRequestAcceptBroadcastReceiver;
import com.example.onyx.onyx.CarerRequestDismissBroadcastReceiver;
import com.example.onyx.onyx.MapsFragment;
import com.example.onyx.onyx.R;
import com.example.onyx.onyx.ReopenChatActivity;
import com.example.onyx.onyx.events.PushNotificationEvent;
import com.example.onyx.onyx.ui.activities.ChatActivity;
import com.example.onyx.onyx.utils.Constants;
import com.example.onyx.onyx.utils.SharedPrefUtil;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private LocalBroadcastManager broadcaster;
    private FirebaseUser user;
    private FirebaseFirestore db;

    @Override
    public void onCreate() {
        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d("tag", "message recieved");
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            if (remoteMessage.getData().get("type").equals("carerRequest")) {
                sendCarerNotification(remoteMessage);
            } else if (remoteMessage.getData().get("type").equals("SOS")) {
                sendSOSNotification(remoteMessage);
            } else if (remoteMessage.getData().get("type").equals("chat")) {
                handleChat(remoteMessage);
            } else if (remoteMessage.getData().get("type").equals("annotation")) {
                handleAnnotation(remoteMessage);
            } else if (remoteMessage.getData().get("type").equals("locationUpdate")) {
                handleLocation(remoteMessage);
            }
            return;
        }
    }

    private void handleLocation(RemoteMessage remoteMessage) {
        Double latitude = Double.parseDouble(remoteMessage.getData().get("latitude"));
        Double longitude = Double.parseDouble(remoteMessage.getData().get("longitude"));

        Bundle args = new Bundle();
        args.putParcelable("location", new LatLng(latitude, longitude));

        Intent intent = new Intent("location");
        intent.putExtra("bundle", args);
        broadcaster.sendBroadcast(intent);
    }

    private void handleAnnotation(RemoteMessage remoteMessage) {
        String pointsAsString = remoteMessage.getData().get("points");

        Intent intent = new Intent("MyData");
        intent.putExtra("points", remoteMessage.getData().get("points"));
        broadcaster.sendBroadcast(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendCarerNotification(RemoteMessage remoteMessage) {
        String senderName = remoteMessage.getData().get("senderName");

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String CHANNEL_ID = "carer_requests";
        CharSequence name = "Carer Requests";
        String Description = "Notifications for incoming carer requests";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
        mChannel.setDescription(Description);
        mChannel.enableLights(true);
        mChannel.setLightColor(Color.RED);
        mChannel.enableVibration(true);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        mChannel.setShowBadge(false);
        Log.d("chanel", "coco");
        notificationManager.createNotificationChannel(mChannel);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

//        Intent intent = new Intent(this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // generate id
        int uniqID = createID();
        FirebaseData.setData(uniqID, remoteMessage.getData().get("senderId"));

        // accept button
        Intent acceptIntent = new Intent(this, CarerRequestAcceptBroadcastReceiver.class);
        acceptIntent.setAction("accept");
        PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(this, 0, acceptIntent, 0);
        Notification.Action acceptAction = new Notification.Action.Builder(Icon.createWithResource(this, R.drawable.ic_mic_off_black_24dp), "ACCEPT", acceptPendingIntent).build();


        Log.d("Onyx1", Integer.toString(uniqID));

        // dismiss button
        Intent dismissIntent = new Intent(this, CarerRequestDismissBroadcastReceiver.class);
        dismissIntent.setAction("dismiss");
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(this, 0, dismissIntent, 0);
        Notification.Action dismissAction = new Notification.Action.Builder(Icon.createWithResource(this, R.drawable.ic_mic_off_black_24dp), "DISMISS", dismissPendingIntent).build();

        Log.d("Onyx2", Integer.toString(uniqID));

        Notification notificationBuilder = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Care requested")
                .setContentText(senderName + " needs assistance")
                .setSmallIcon(R.drawable.ic_messaging)
                .addAction(acceptAction)
                .addAction(dismissAction)
                .build();

        Log.d("Onyx3", Integer.toString(uniqID));
        notificationManager.notify(uniqID, notificationBuilder);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendSOSNotification(RemoteMessage remoteMessage) {
        String senderName = remoteMessage.getData().get("senderName");

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String CHANNEL_ID = "sos_requests";
        CharSequence name = "SOS";
        String Description = "Notifications for incoming sos requests";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
        mChannel.setDescription(Description);
        mChannel.enableLights(true);
        mChannel.setLightColor(Color.RED);
        mChannel.enableVibration(true);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        mChannel.setShowBadge(false);
        Log.d("chanel", "coco");
        notificationManager.createNotificationChannel(mChannel);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification notificationBuilder = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("SOS request!")
                .setContentText(senderName + " needs assistance")
                .setSmallIcon(R.drawable.ic_messaging)
                .build();


        int uniqID = createID();
        Log.d("aaaaa", String.valueOf(uniqID));
        notificationManager.notify(uniqID, notificationBuilder);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void handleChat(RemoteMessage remoteMessage) {
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
            String CHANNEL_ID = "chat_messages";
            CharSequence name = "Chat Messages";
            String Description = "Notifications for incoming chat messages";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(false);
            Log.d("chanel", "coco");
            notificationManager.createNotificationChannel(mChannel);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            Notification notificationBuilder = new Notification.Builder(this, CHANNEL_ID)
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

    //generate notification id for messages
    public int createID() {
        Date now = new Date();
        int id = Integer.parseInt(new SimpleDateFormat("ddHHmmss", Locale.US).format(now));
        return id;
    }

    @Override
    public void onNewToken(String s) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        super.onNewToken(s);
        Log.e(TAG, "new token: " + s);
        sendRegistrationToServer(s);
    }

    private void sendRegistrationToServer(final String token) {
        new SharedPrefUtil(getApplicationContext()).saveString(Constants.ARG_FIREBASE_TOKEN, token);

        if (user != null) {
            Log.d(TAG, "sendRegistrationToServer: " + token);
            db.collection("users")
                    .document(user.getUid())
                    .update("firebaseToken", token);
        }
    }
}