package com.example.onyx.onyx;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import static com.example.onyx.onyx.MapsFragment.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;

/**
 * Created by xin on 14/08/2018.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}