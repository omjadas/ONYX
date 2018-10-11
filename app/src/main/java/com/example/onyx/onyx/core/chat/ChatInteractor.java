package com.example.onyx.onyx.core.chat;

import android.content.Context;

import com.example.onyx.onyx.IdGenerator;
import com.example.onyx.onyx.models.Chat;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;


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
        final String room_id = IdGenerator.getRoomId(chat.senderUid, chat.receiverUid);

        final String timestamp = Long.toString(chat.timestamp);
        final DocumentReference reference = FirebaseFirestore.getInstance().collection("chat_rooms").document(room_id);

        FirebaseFirestore.getInstance().collection("chat_rooms").document(room_id).get().
                addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        reference.collection("message").document(timestamp).set(chat);
                    }
                });
        mOnSendMessageListener.onSendMessageSuccess();
        //mOnGetMessagesListener.onGetMessagesSuccess(chat);
    }

    @Override
    public void getMessageFromFirebaseUser(String senderUid, String receiverUid) {


        String temp_room_id;
        int compare = senderUid.compareTo(receiverUid);
        if (compare < 0) {
            temp_room_id = senderUid + "_" + receiverUid;
        } else if (compare > 0) {
            temp_room_id = receiverUid + "_" + senderUid;
        } else {
            temp_room_id = senderUid + "_" + receiverUid;
        }
        final String room_id = temp_room_id;

        final DocumentReference reference = FirebaseFirestore.getInstance().collection("chat_rooms").document(room_id);

        reference.collection("message")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        System.err.println("Msg Listen failed:" + e);
                        return;
                    }

                    for (DocumentChange dc : Objects.requireNonNull(queryDocumentSnapshots).getDocumentChanges()) {
                        switch (dc.getType()) {
                            case ADDED:
                                dc.getDocument();

                                Chat chat = dc.getDocument().toObject(Chat.class);
                                mOnGetMessagesListener.onGetMessagesSuccess(chat);
                                break;
                            case MODIFIED:

                                break;
                            case REMOVED:

                                break;
                            default:
                                break;
                        }
                    }
                });
    }
}
