package com.example.onyx.onyx;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;


public class contactSearch extends Activity{

    private EditText searchText;
    private Button searchButton;
    private FirebaseFirestore db;
    private CollectionReference users;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        searchText = findViewById(R.id.searchText);
        searchButton = findViewById(R.id.buttonSearch);
        db = FirebaseFirestore.getInstance();
        users = db.collection("Users");
    }

    public void search(String seachText){
        users.whereEqualTo("First name", seachText).orderBy("First name").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){

                }
                else{
                    //print error
                }
            }
        });
    }

}
