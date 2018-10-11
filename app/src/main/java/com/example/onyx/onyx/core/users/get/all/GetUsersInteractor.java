package com.example.onyx.onyx.core.users.get.all;

import com.example.onyx.onyx.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GetUsersInteractor implements GetUsersInterface.Interactor {
    private static final String TAG = "GetUsersInteractor";

    private GetUsersInterface.OnGetAllUsersListener mOnGetAllUsersListener;
    private FirebaseFirestore db;
    private CollectionReference users;

    public GetUsersInteractor(GetUsersInterface.OnGetAllUsersListener onGetAllUsersListener) {
        this.mOnGetAllUsersListener = onGetAllUsersListener;
    }


    @Override
    public void getAllUsersFromFirebase() {

        db = FirebaseFirestore.getInstance();
        //get contact for current user
        db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("contacts")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> myListOfDocuments = task.getResult().getDocuments();
                        final List<User> users = new ArrayList<>();
                        final List<String> uids = new ArrayList<>();
                        for (DocumentSnapshot dss : myListOfDocuments) {

                            String uid = dss.get("userRef").toString();
                            uids.add(uid);
                        }
                        if (uids.size() < 1) {

                            mOnGetAllUsersListener.onGetAllUsersSuccess(users);
                            return;
                        }
                        //for each contact id found
                        for (String uid : uids) {
                            FirebaseFirestore.getInstance().collection("users").document(uid).get().addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    DocumentSnapshot doc = task1.getResult();

                                    User user = doc.toObject(User.class);
                                    user.uid = doc.getId();
                                    if (!user.email.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                                        users.add(user);
                                        //display users
                                        //if(users.size()==uids.size())
                                        Collections.sort(users);
                                        mOnGetAllUsersListener.onGetAllUsersSuccess(users);
                                    }
                                }
                            });
                        }
                    }
                });
    }

    @Override
    public void getChatUsersFromFirebase() {
    }
}
