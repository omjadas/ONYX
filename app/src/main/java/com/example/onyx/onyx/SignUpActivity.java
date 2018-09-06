package com.example.onyx.onyx;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends FragmentActivity {
    Button sendButton;
    EditText name;
    EditText password1;
    EditText password2;
    EditText email;
    EditText phoneNum;
    FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        System.out.println("test");
        mAuth = FirebaseAuth.getInstance();
        sendButton = findViewById(R.id.buttonSend);
        name = findViewById(R.id.nameText);
        password1 = findViewById(R.id.password);
        password2 = findViewById(R.id.passwordRepeat);
        email = findViewById(R.id.emailText);
        phoneNum = findViewById(R.id.phoneText);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkData(name.getText().toString(), password1.getText().toString(),
                        password2.getText().toString(), email.getText().toString(),
                        phoneNum.getText().toString())){
                    createAccount(email.getText().toString().trim(), password1.getText().toString().trim());
                }
            }
        });
    }

    private void createAccount(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            System.out.println("Signed in");
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

    public boolean checkData(String name, String password1, String password2, String email, String phoneNum){
        boolean valid = true;
        if(name.isEmpty()){

        }
        if(!validPassword(password1, password2)){
            valid = false;
        }
        if(email.isEmpty()){

        }
        if(validNumber(phoneNum)){

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
