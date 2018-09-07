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

    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText emailText, password;

    private Button buttonSignIn;

    private Button buttonCreate;
    Intent intent;

    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signin);

        mAuth = FirebaseAuth.getInstance();

        emailText = (EditText) findViewById(R.id.emailText);
        password = (EditText) findViewById(R.id.password);
        buttonSignIn = (Button) findViewById(R.id.buttonSignIn);
        buttonCreate = (Button) findViewById(R.id.buttonCreate);

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
                    return;
                }
            }
        });
    }


    private void SignUp(){
        intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    private void updateUI(FirebaseUser currentUser){
        if(currentUser == null){
            System.out.println("nope");
            //need login screen
        }
        else{
            intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
        }
    }
}
