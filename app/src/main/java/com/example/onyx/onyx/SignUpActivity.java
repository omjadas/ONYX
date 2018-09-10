package com.example.onyx.onyx;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
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
    CheckBox checkCarer;
    FirebaseAuth mAuth;
    DatabaseReference database;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        System.out.println("test");

        mAuth = FirebaseAuth.getInstance();

        //Init UI functionality
        sendButton = findViewById(R.id.buttonSend);
        cancelButton = findViewById(R.id.buttonCancel);
        name = findViewById(R.id.nameText);
        lastName = findViewById(R.id.lastNameText);
        password1 = findViewById(R.id.password);
        password2 = findViewById(R.id.passwordRepeat);
        email = findViewById(R.id.emailText);
        checkCarer = findViewById(R.id.checkCarer);

        database = FirebaseDatabase.getInstance().getReference();

        //User sends a create account request
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Check if data is okay
                if(checkData(name.getText().toString(), lastName.getText().toString(), password1.getText().toString(),
                        password2.getText().toString(), email.getText().toString())){
                    //Make an account with user given data
                    createAccount(name.getText().toString().trim(),
                            lastName.getText().toString().trim(),
                            password1.getText().toString().trim(),
                            email.getText().toString().trim(),checkCarer.isChecked());
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

    private void createAccount(final String name, final String lastName, String password, final String email, final boolean isCarer){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //If user is created, set user data in database under made user ID
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            database.child("Users").child(user.getUid()).child("First name").setValue(name);
                            database.child("Users").child(user.getUid()).child("Last name").setValue(lastName);
                            database.child("Users").child(user.getUid()).child("Email").setValue(email);
                            database.child("Users").child(user.getUid()).child("isCarer").setValue(isCarer);
                            database.child("Users").child(user.getUid()).child("Contacts").setValue(null);
                            database.child("Users").child(user.getUid()).child("Favourite places").setValue(null);
                            //Return to sign in activity
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            System.out.println("nope");
                            FirebaseAuthException e = (FirebaseAuthException )task.getException();
                            System.out.println("Failed Registration: "+e.getMessage());
                            return;
                        }
                    }
                });
    }

    public boolean checkData(String name, String lastName, String password1, String password2, String email){
        boolean valid = true;
        if(name.isEmpty()){
            this.name.setHint(R.string.invalidName);
            valid = false;
        }
        if(!validPassword(password1, password2)){
            this.password1.setHint(R.string.invalidPassword);
            this.password2.setHint(R.string.invalidPassword);
            valid = false;
        }
        if(email.isEmpty()){
            this.email.setHint(R.string.invalidEmail);
            valid = false;
        }
        if(lastName.isEmpty()){
            this.lastName.setHint(R.string.invalidName);
            valid = false;
        }
        return valid;
    }

    public boolean validPassword(String password1, String password2){
        return !password1.isEmpty() && !password2.isEmpty() && password1.equals(password2);
    }
}
