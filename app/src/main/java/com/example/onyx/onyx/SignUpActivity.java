package com.example.onyx.onyx;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    static final String FIRESTORE_WRITE_TAG = "ADDITION TO DATABASE: ";
    static final String SUCCESS = "SUCCESS";
    static final String FAILURE = "FAILURE";

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private GoogleSignInAccount account;
    private Button yesButton;
    private Button noButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        account = GoogleSignIn.getLastSignedInAccount(this);
        db = FirebaseFirestore.getInstance();
        yesButton = findViewById(R.id.yesButton);
        noButton = findViewById(R.id.noButton);

        yesButton.setOnClickListener(view -> setData(true));

        noButton.setOnClickListener(view -> setData(false));
    }


    private void setData(boolean isCarer) {
        String givenName = account.getGivenName() == null ? " " : account.getGivenName();
        String lastName = account.getFamilyName() == null ? " " : account.getFamilyName();
        String email = account.getEmail() == null ? " " : account.getEmail();
        String QR = generateQR(email);
        DocumentReference newUser = db.collection("users").document(currentUser.getUid());

        Log.d("register user", "setdata called");
        Map<String, Object> user = new HashMap<>();
        user.put("firstName", givenName);
        user.put("lastName", lastName);
        user.put("email", email);
        user.put("isOnline", true);
        user.put("isCarer", isCarer);
        user.put("QR",QR);
        newUser.set(user).
                addOnSuccessListener(aVoid -> Log.d(FIRESTORE_WRITE_TAG, SUCCESS)).addOnFailureListener(e -> Log.d(FIRESTORE_WRITE_TAG, FAILURE));
        startActivity(new Intent(SignUpActivity.this, MainActivity.class));
        finish();
    }

    public String generateQR(String email){
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try{
            BitMatrix bitMatrix = multiFormatWriter.encode(email, BarcodeFormat.QR_CODE,800,800);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            return BitmapToString(bitmap);
        }
        catch (WriterException e){
            e.printStackTrace();
        }
        return "";
    }

    public String BitmapToString(Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT);
    }
}
