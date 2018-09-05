package com.example.onyx.onyx;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
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

                }
            }
        });
    }

    public boolean checkData(String name, String password1, String password2, String email, String phoneNum){
        boolean valid = true;
        if(name.isEmpty()){

        }
        if(validPassword(password1, password2)){

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
