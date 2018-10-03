package com.example.onyx.onyx.videochat.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.MenuItem;

import com.twilio.video.AudioCodec;
import com.twilio.video.G722Codec;
import com.twilio.video.H264Codec;
import com.twilio.video.IsacCodec;
import com.twilio.video.OpusCodec;
import com.twilio.video.PcmaCodec;
import com.twilio.video.PcmuCodec;
import com.twilio.video.VideoCodec;
import com.twilio.video.Vp8Codec;
import com.twilio.video.Vp9Codec;
import com.example.onyx.onyx.R;

import org.webrtc.MediaCodecVideoDecoder;
import org.webrtc.MediaCodecVideoEncoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CallPreferences {

    public static final String PREF_AUDIO_CODEC_DEFAULT = OpusCodec.NAME;
    public static final String PREF_VIDEO_CODEC_DEFAULT = Vp8Codec.NAME;
    public static final String PREF_SENDER_MAX_AUDIO_BITRATE_DEFAULT = "0";
    public static final String PREF_SENDER_MAX_VIDEO_BITRATE_DEFAULT = "0";
    public static final boolean PREF_VP8_SIMULCAST_DEFAULT = false;

}
