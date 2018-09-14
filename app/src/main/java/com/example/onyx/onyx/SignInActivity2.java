package com.example.onyx.onyx;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignInActivity2 extends Activity{

    static final String SIGN_IN_TAG = "SIGN IN: ";
    static final String GOOGLE_AUTH_TAG = "GOOGLE AUTHENTICATION: ";
    static final String FIRESTORE_WRITE_TAG = "ADDITION TO DATABASE: ";
    static final String SUCCESS = "SUCCESS";
    static final String FAILURE = "FAILURE";

    GoogleSignInClient mGoogleSignInClient;
    static final int RC_SIGN_IN = 1;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private GoogleSignInAccount acct;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin2);

        db = FirebaseFirestore.getInstance();

        Button buttonSend = findViewById(R.id.buttonSignIn2);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignIn();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart(){
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        updateUI();
    }

    private void SignIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);

        if(requestCode == RC_SIGN_IN){
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                acct = task.getResult(ApiException.class);
                firebaseAuthWithGoogle();
            }
            catch(ApiException e){
                Log.w(GOOGLE_AUTH_TAG, FAILURE, e);
            }
        }
    }

    private void updateUI(){
        if(currentUser != null){
            Intent startApp = new Intent(this,MainActivity.class);
            startActivity(startApp);
            Log.d(SIGN_IN_TAG,SUCCESS);
            finish();
        }
        else
            Log.d(SIGN_IN_TAG,FAILURE);
    }

    private void firebaseAuthWithGoogle(){
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(),null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    currentUser = mAuth.getCurrentUser();
                    db.collection("users").document(currentUser.getUid()).get().
                            addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            Log.d(FIRESTORE_WRITE_TAG,"IS READY");
                            if(task.isSuccessful()){
                                Log.d("TASK: ",SUCCESS);
                                DocumentSnapshot document = task.getResult();
                                if(!document.exists()){
                                    Log.d("DOCUMENT: ","!EXISTS");
                                    setData();
                                }
                            }
                        }
                    });
                    updateUI();
                }
                else{
                    Log.d(GOOGLE_AUTH_TAG,FAILURE);
                    updateUI();
                }
            }
        });
    }

    private void setData(){
        Map<String, Object> user = new HashMap<>();
        user.put("first name",acct.getGivenName());
        user.put("last name",acct.getFamilyName());
        user.put("email",acct.getEmail());
       /* user.put("contacts", db.collection("users").document(currentUser.getUid()).
                collection("contacts"));*/
        /*user.put("favourite locations", db.collection("users").
                document(currentUser.getUid()).collection("locations"));*/
        user.put("current location", null);
        user.put("is online", true);
        //Add is carer
        db.collection("users").document(currentUser.getUid()).set(user).
                addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(FIRESTORE_WRITE_TAG,SUCCESS);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(FIRESTORE_WRITE_TAG,FAILURE);
            }
        });
    }
}
