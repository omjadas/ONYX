package com.example.onyx.onyx.call;

import com.twilio.video.OpusCodec;
import com.twilio.video.Vp8Codec;

public class CallPreferences {

    //Settings for calls
    public static final String PREF_AUDIO_CODEC = OpusCodec.NAME;
    public static final String PREF_VIDEO_CODEC = Vp8Codec.NAME;
    public static final String PREF_SENDER_MAX_AUDIO_BITRATE = "0";
    public static final String PREF_SENDER_MAX_VIDEO_BITRATE = "0";
    public static final boolean PREF_VP8_SIMULCAST = false;

    //Variables for UI
    public static boolean voiceEnabled = true;
    public static boolean videoEnabled = false;

}
