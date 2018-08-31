package com.example.onyx.onyx;

import android.app.Service;
import android.content.Intent;
import android.net.sip.SipManager;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.lang.Thread;

public class VoiceService extends Service {
    private SipManager mSipManager = null;
    public

    VoiceService() {
    }

    public void onStartCommand() {
        if (mSipManager == null) {
            mSipManager = SipManager.newInstance(this);
        }
    }

    public void shutdown() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
