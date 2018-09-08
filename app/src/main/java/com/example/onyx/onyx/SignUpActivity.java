package com.example.onyx.onyx;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SignUpActivity extends FragmentActivity {
    Button sendButton;
    Button cancelButton;
    EditText name;
    EditText lastName;
    EditText password1;
    EditText password2;
    EditText email;
    FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        System.out.println("test");
        mAuth = FirebaseAuth.getInstance();
        sendButton = findViewById(R.id.buttonSend);
        cancelButton = findViewById(R.id.buttonCancel);
        name = findViewById(R.id.nameText);
        lastName = findViewById(R.id.lastNameText);
        password1 = findViewById(R.id.password);
        password2 = findViewById(R.id.passwordRepeat);
        email = findViewById(R.id.emailText);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkData(name.getText().toString(), lastName.getText().toString(), password1.getText().toString(),
                        password2.getText().toString(), email.getText().toString())){
                    createAccount(name.getText().toString().trim(),
                            lastName.getText().toString().trim(),
                            password1.getText().toString().trim(),
                            email.getText().toString().trim());
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void createAccount(final String name, final String lastName, String password, final String email){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            //create user
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference mDatabase = database.getReference();
                            List<String> testcontacts = new ArrayList<String>();
                            testcontacts.add("Bob");
                            testcontacts.add("Jill");
                            testcontacts.add("Frank");
                            User newUser = new User(email, name, lastName,
                                    testcontacts,
                                    Collections.<Location>emptyList(), Boolean.FALSE);
                            mDatabase.child("users").child(mAuth.getUid()).setValue(newUser);

                            finish();

                            //get user data
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            System.out.println("nope");
                            FirebaseAuthException e = (FirebaseAuthException )task.getException();
                            System.out.println("Failed Registration: "+e.getMessage());
                            return;
                        }

                        // ...
                    }
                });
    }

    public boolean checkData(String name, String lastName, String password1, String password2, String email){
        boolean valid = true;
        if(name.isEmpty()){

        }
        if(!validPassword(password1, password2)){
            valid = false;
        }
        if(email.isEmpty()){

        }
        if(lastName.isEmpty()){

        }
        return valid;
    }

    public boolean validPassword(String password1, String password2){
        return !password1.isEmpty() && !password2.isEmpty() && password1.equals(password2);
    }

    public boolean validNumber(String phoneNum){
        if(phoneNum.isEmpty() || phoneNum.length() > 10)
            return false;
        for(char c : phoneNum.toCharArray()){
            if(!Character.isDigit(c))
                return false;
        }
        return true;
    }


}
