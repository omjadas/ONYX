package com.example.onyx.onyx.core.chat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.example.onyx.onyx.fcm.FcmNotificationBuilder;
import com.example.onyx.onyx.models.Chat;
import com.example.onyx.onyx.models.User;
import com.example.onyx.onyx.utils.Constants;
import com.example.onyx.onyx.utils.SharedPrefUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.firebase.analytics.FirebaseAnalytics.Param.SUCCESS;


public class ChatInteractor implements ChatInterface.Interactor {
    private static final String TAG = "ChatInteractor";

    private ChatInterface.OnSendMessageListener mOnSendMessageListener;
    private ChatInterface.OnGetMessagesListener mOnGetMessagesListener;

    public ChatInteractor(ChatInterface.OnSendMessageListener onSendMessageListener) {
        this.mOnSendMessageListener = onSendMessageListener;
    }

    public ChatInteractor(ChatInterface.OnGetMessagesListener onGetMessagesListener) {
        this.mOnGetMessagesListener = onGetMessagesListener;
    }

    public ChatInteractor(ChatInterface.OnSendMessageListener onSendMessageListener,
                          ChatInterface.OnGetMessagesListener onGetMessagesListener) {
        this.mOnSendMessageListener = onSendMessageListener;
        this.mOnGetMessagesListener = onGetMessagesListener;
    }

    @Override
    public void sendMessageToFirebaseUser(final Context context, final Chat chat, final String receiverFirebaseToken) {
        final String room_type_1 = chat.senderUid + "_" + chat.receiverUid;
        final String room_type_2 = chat.receiverUid + "_" + chat.senderUid;



        String timestamp =Long.toString(chat.timestamp);
        DocumentReference reference1 = FirebaseFirestore.getInstance().collection("chat_rooms").document(room_type_1);
        DocumentReference reference2 = FirebaseFirestore.getInstance().collection("chat_rooms").document(room_type_2);
        if (reference1 != null){
            reference1.set(chat, SetOptions.merge());
            reference1.collection("message").document(timestamp).set(chat);

        }else if(reference2 != null){
            reference2.collection("message").document(timestamp).set(chat);
        }else{
            FirebaseFirestore.getInstance().collection("chat_rooms").document().set(room_type_1);
        }
        sendPushNotificationToReceiver(chat.sender,
                chat.message,
                chat.senderUid,
                new SharedPrefUtil(context).getString(Constants.ARG_FIREBASE_TOKEN),
                receiverFirebaseToken);
        mOnSendMessageListener.onSendMessageSuccess();
        mOnGetMessagesListener.onGetMessagesSuccess(chat);



    }

    private void sendPushNotificationToReceiver(String username,
                                                String message,
                                                String uid,
                                                String firebaseToken,
                                                String receiverFirebaseToken) {
        FcmNotificationBuilder.initialize()
                .title(username)
                .message(message)
                .username(username)
                .uid(uid)
                .firebaseToken(firebaseToken)
                .receiverFirebaseToken(receiverFirebaseToken)
                .send();
    }

    @Override
    public void getMessageFromFirebaseUser(String senderUid, String receiverUid) {
        final String room_type_1 = senderUid + "_" + receiverUid;
        final String room_type_2 = receiverUid + "_" + senderUid;

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();


        DocumentReference reference1 = FirebaseFirestore.getInstance().collection("chat_rooms").document(room_type_1);
        DocumentReference reference2 = FirebaseFirestore.getInstance().collection("chat_rooms").document(room_type_2);
        if (reference1 != null){

            Log.d("rrfffffff1111111",room_type_1);
            reference1.collection("message").get().
                    addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                List<DocumentSnapshot> myListOfDocuments = task.getResult().getDocuments();

                                for (DocumentSnapshot dss : myListOfDocuments) {

                                    Chat msg = dss.toObject(Chat.class);
                                    Log.d("rrfffffff1111111",msg.message);
                                    mOnGetMessagesListener.onGetMessagesSuccess(msg);
                                }
                            }
                        }
                    });

        }else if(reference2 != null){
            Log.d("rrfffffff1111111","bbbbbbbbbbbbbbb");
            reference2.collection("message").get().
                    addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                List<DocumentSnapshot> myListOfDocuments = task.getResult().getDocuments();


                                for (DocumentSnapshot dss : myListOfDocuments) {

                                    Chat msg = dss.toObject(Chat.class);

                                    mOnGetMessagesListener.onGetMessagesSuccess(msg);
                                }


                            }
                        }
                    });
        }else{
             Log.e(TAG, "getMessageFromFirebaseUser: no such room available");
        }




    }
}
