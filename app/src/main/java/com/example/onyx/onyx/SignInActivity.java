package com.example.onyx.onyx;

import android.app.Activity;
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

    private Button buttonSend;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        mAuth = FirebaseAuth.getInstance();

        emailText = (EditText) findViewById(R.id.emailText);
        password = (EditText) findViewById(R.id.password);
        buttonSend = (Button) findViewById(R.id.buttonSend);

        buttonSend.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SignIn(emailText.getText().toString().trim(), password.getText().toString().trim());
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
    private void updateUI(FirebaseUser currentUser){
        if(currentUser == null){
            System.out.println("nope");
            //need login screen
        }
        else{
            System.out.println("logged in");
            //signed in
        }
    }
}
