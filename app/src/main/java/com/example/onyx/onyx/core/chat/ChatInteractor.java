package com.example.onyx.onyx.core.chat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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


        final String room_id;
        final String senderUid = chat.senderUid;
        final String receiverUid = chat.receiverUid;
        int compare = senderUid.compareTo(receiverUid);
        if (compare < 0){
            room_id = chat.senderUid + "_" + chat.receiverUid;
        }
        else if (compare > 0) {
            room_id = chat.receiverUid + "_" + chat.senderUid;
        }
        else {
            room_id = chat.senderUid + "_" + chat.receiverUid;
        }


        final String timestamp =Long.toString(chat.timestamp);
        final DocumentReference reference = FirebaseFirestore.getInstance().collection("chat_rooms").document(room_id);

        FirebaseFirestore.getInstance().collection("chat_rooms").document(room_id).get().
                addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if(!document.exists()){
                                reference.set(chat, SetOptions.merge());
                                reference.collection("message").document(timestamp).set(chat);
                            }else{
                                reference.collection("message").document(timestamp).set(chat);
                            }


                        }
                    }
                });

        sendPushNotificationToReceiver(chat.sender,
                chat.message,
                chat.senderUid,
                new SharedPrefUtil(context).getString(Constants.ARG_FIREBASE_TOKEN),
                receiverFirebaseToken);
        mOnSendMessageListener.onSendMessageSuccess();
        //mOnGetMessagesListener.onGetMessagesSuccess(chat);




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


        String temp_room_id;
        int compare = senderUid.compareTo(receiverUid);
        if (compare < 0){
            temp_room_id = senderUid + "_" + receiverUid;
        }
        else if (compare > 0) {
            temp_room_id = receiverUid + "_" + senderUid;
        }
        else {
            temp_room_id = senderUid + "_" + receiverUid;
        }
        final String room_id = temp_room_id;

        final DocumentReference reference = FirebaseFirestore.getInstance().collection("chat_rooms").document(room_id);

        reference.collection("message")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            System.err.println("Msg Listen failed:" + e);
                            return;
                        }


                        for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    if (dc.getDocument() != null) {

                                        Chat chat = dc.getDocument().toObject(Chat.class);
                                        mOnGetMessagesListener.onGetMessagesSuccess(chat);
                                    }
                                    break;
                                case MODIFIED:

                                    break;
                                case REMOVED:

                                    break;
                                default:
                                    break;
                            }

                        }
                    }


                });

    }
}
