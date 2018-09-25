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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class contactFragment extends Fragment {

    private EditText searchText;
    private Button sendButton;
    private Button cancelButton;

    public contactFragment() {

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
        searchText = getView().findViewById(R.id.emailSearch);
        sendButton = getView().findViewById(R.id.addContactsButton);
        cancelButton = getView().findViewById(R.id.cancelButton);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addContact(searchText.getText().toString());
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void addContact(String contactRef) {
        CollectionReference contacts = FirebaseFirestore.getInstance().
                collection("users").document(FirebaseAuth.getInstance().getUid()).
                collection("contacts");
        Map<String,Object> newContact = new HashMap<>();
        newContact.put("userRef",contactRef);
        contacts.document().set(newContact);
    }

}
