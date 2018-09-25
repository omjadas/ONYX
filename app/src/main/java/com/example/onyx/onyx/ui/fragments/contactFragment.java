package com.example.onyx.onyx.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.onyx.onyx.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class contactFragment extends Fragment {

    private EditText searchText;
    private Button sendButton;
    private Button cancelButton;

    public static contactFragment newInstance() {
        return new contactFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_new_contact, container, false);
        return fragmentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        System.out.println("CONTACTS FRAGMENT: CREATED");
        searchText = (EditText)getView().findViewById(R.id.emailSearch);
        sendButton = (Button)getView().findViewById(R.id.addContactButton);
        cancelButton = (Button)getView().findViewById(R.id.cancelButton);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addContact(searchText.getText().toString());
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getParentFragment().getChildFragmentManager().beginTransaction().
                        remove(contactFragment.this).commit();
            }
        });
    }

    private void addContact(String contactEmail) {
        System.out.println("Adding user with email: " + contactEmail);
        CollectionReference users = FirebaseFirestore.getInstance().
                collection("users");
        final CollectionReference contacts = users.document(FirebaseAuth.getInstance().getUid()).collection("contacts");
        users.whereEqualTo("email",contactEmail).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    System.out.println("Got results: " + task.getResult().getDocuments().size());
                    DocumentSnapshot snap = task.getResult().getDocuments().get(0);
                    String contactRef = snap.getId();
                    Map<String,Object> newContact = new HashMap<>();
                    newContact.put("userRef",contactRef);
                    contacts.document().set(newContact);
                }
            }
        });
    }

}
