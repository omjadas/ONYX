package com.example.onyx.onyx;

import android.app.Service;
import android.content.Intent;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.Thread;

public class VoiceService extends Service {
    private SipManager mSipManager = null;
    public SipProfile mSipProfile = null;

    public void onCreate() {
        if (mSipManager == null) {
            mSipManager = SipManager.newInstance(this);
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void onDestroy() {
        if (mSipManager == null) {
            return;
        }
        try {
            if (mSipProfile != null) {
                mSipManager.close(mSipProfile.getUriString());
            }
        } catch (Exception ee) {
            Log.d("onDestroy", "Failed to close local profile.", ee);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
