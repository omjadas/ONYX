package com.example.onyx.onyx;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends Activity {
    private FirebaseAuth mAuth;

    private EditText emailText, password;

    private Button buttonSignIn, buttonCreate, buttonDelete, buttonVerify;

    Intent intent;

    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        //Set view and widgets
        setContentView(R.layout.activity_signin);

        mAuth = FirebaseAuth.getInstance();

        emailText = findViewById(R.id.emailText);
        password = findViewById(R.id.password);
        buttonSignIn = findViewById(R.id.buttonSignIn);
        buttonCreate = findViewById(R.id.buttonCreate);

        buttonSignIn.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SignIn(emailText.getText().toString().trim(), password.getText().toString().trim());
            }
        });

        buttonCreate.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SignUp();
            }
        });
    }

    @Override
    public void onStart(){
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void SignIn(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    updateUI(currentUser);
                }
                else{
                    updateUI(null);
                    FirebaseAuthException e = (FirebaseAuthException )task.getException();
                    System.out.println("Failed Registration: "+e.getMessage());
                }
            }
        });
    }


    private void SignUp(){
        //Start Sign up activity which should change the current user
        intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
        updateUI(mAuth.getCurrentUser());
    }

    private void updateUI(FirebaseUser currentUser){
        //If null then awaiting sign in
        if(currentUser == null){
            findViewById(R.id.credentials_invalid).setVisibility(View.VISIBLE);
        }
        else{
            //Awaiting email verification
            if(!currentUser.isEmailVerified()){
                verifyUserEmail(currentUser);
            }
            //Verified users signed in, access main activity
            else {
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
        }
    }

    private void verifyUserEmail(final FirebaseUser currentUser){

        //Set view for user email verification

        setContentView(R.layout.verify_email);
        buttonDelete = findViewById(R.id.buttonDelete);
        buttonVerify = findViewById(R.id.buttonVerify);
        Button buttonSignIn2 = findViewById(R.id.buttonSignIn2);
        final EditText emailText2 = findViewById(R.id.emailText2);
        final EditText password2 = findViewById(R.id.password2);

        //Allow for first sign in
        buttonSignIn2.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignIn(emailText2.getText().toString().trim(), password2.getText().toString().trim());
            }
        });

        //Allow for verification re-send
        buttonVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentUser.sendEmailVerification();
            }
        });

        //TODO set user deletion from this stage
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        currentUser.sendEmailVerification().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    System.out.println("Email sent");
                }
            }
        });
    }
}
