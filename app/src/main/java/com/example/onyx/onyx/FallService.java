package com.example.onyx.onyx;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Objects;

public class FallService extends Service implements SensorEventListener {

    private static final String TAG = "FallService";
    private static int sensorValuesSize = 70;
    int index = 0;
    boolean fallDetected = false;
    private LocalBroadcastManager broadcaster = null;
    // Fall Detection
    private SensorManager accelManage = null;
    private Sensor senseAccel = null;
    private float accelValuesX[] = new float[sensorValuesSize];
    private float accelValuesY[] = new float[sensorValuesSize];
    private float accelValuesZ[] = new float[sensorValuesSize];

    public void onCreate() {
        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        startFallDetection();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onDestroy() {
        stopFallDetection();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Log.d("onSensorChanged", "sensor changed");
        Sensor mySensor = sensorEvent.sensor;
        //   Log.i(TAG,"Sensor Running ");

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            index++;
            accelValuesX[index] = sensorEvent.values[0];
            accelValuesY[index] = sensorEvent.values[1];
            accelValuesZ[index] = sensorEvent.values[2];

            if (index >= sensorValuesSize - 1) {
                index = 0;
                accelManage.unregisterListener(this);
//                Log.i(TAG, "Calling for fall detection");
                callForRecognition();
                accelManage.registerListener(this, senseAccel, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    public void startFallDetection() {
        Log.d(TAG, "Starting fall detection");
        accelManage = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senseAccel = Objects.requireNonNull(accelManage).getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accelManage.registerListener(this, senseAccel, SensorManager.SENSOR_DELAY_NORMAL);

    }

    public void callForRecognition() {
        float currx = 0;
        float curry = 0;
        float currz = 0;
        double rootSquare = 0.0; //root square val of all sensors

        for (int i = 5; i < sensorValuesSize; i++) {
            currx = accelValuesX[i];
            curry = accelValuesY[i];
            currz = accelValuesZ[i];

            rootSquare = Math.sqrt(Math.pow(currx, 2) + Math.pow(curry, 2) + Math.pow(currz, 2));
            if (rootSquare < 2.0) {
                Toast.makeText(this, "Fall detected", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Fall Detected");
                fallDetected = true;
                registerFall();
                return;
            }
        }
        fallDetected = false;
    }

    public void registerFall() {
        Intent intent = new Intent("fall");
        broadcaster.sendBroadcast(intent);
    }

    public void stopFallDetection() {
        if (accelManage != null) {
            accelManage.unregisterListener(this);
        }
        Log.d(TAG, "Fall detection stopped");
        fallDetected = false;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
