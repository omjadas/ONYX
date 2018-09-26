package com.example.onyx.onyx.core.users.get.all;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.onyx.onyx.models.User;
import com.example.onyx.onyx.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

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
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> myListOfDocuments = task.getResult().getDocuments();
                            final List<User> users = new ArrayList<>();
                            final List<String> uids = new ArrayList<>();
                            for (DocumentSnapshot dss : myListOfDocuments) {

                                String uid = dss.get("userRef").toString();
                                uids.add(uid);
                            }
                            if(uids.size()<1){
                                mOnGetAllUsersListener.onGetAllUsersSuccess(users);
                                return;
                            }
                            //for each contact id found
                            for(String uid:uids){
                                FirebaseFirestore.getInstance().collection("users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()){
                                            DocumentSnapshot doc = task.getResult();

                                            User user = doc.toObject(User.class);
                                            user.uid = doc.getId();
                                            if (!user.email.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                                                users.add(user);
                                                //display users
                                                //if(users.size()==uids.size())
                                                mOnGetAllUsersListener.onGetAllUsersSuccess(users);

                                            }

                                        }
                                    }
                                });
                            }

                        }
                    }
                });

    }

    @Override
    public void getChatUsersFromFirebase() {
    }
}
