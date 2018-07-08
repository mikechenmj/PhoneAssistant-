package com.chenmj.phoneassistant.xunfei;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.sunflower.FlowerCollector;
import com.chenmj.phoneassistant.R;
import com.chenmj.phoneassistant.application.SpeechApp;

/**
 * Created by 健哥哥 on 2018/4/22.
 */

public class SpeechSynthesizerController {

    private final static String TAG = "MCJ";

    private final com.iflytek.cloud.SpeechSynthesizer mTts;

    private final static String ENGINE_TYPE = SpeechConstant.TYPE_CLOUD;

    private boolean mIsSpeakPaused;

    private SpeechSynthesizerController() {
        mTts = SpeechSynthesizer.createSynthesizer(SpeechApp.getContext(), mTtsInitListener);
        if (mTts != null) {
            setParam();
        }
    }

    public static SpeechSynthesizerController getInstance() {
        return SpeechSynthesizerController.SpeechSynthesizerHolder.INSTANCE;
    }

    private static class SpeechSynthesizerHolder {
        private static final SpeechSynthesizerController INSTANCE = new SpeechSynthesizerController();  //创建实例的地方
    }

    private final InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Log.d(TAG, "初始化失败,错误码：" + code);
            }
        }
    };

    private final SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            Log.e("MCJ", "onSpeakBegin");
            mIsSpeakPaused = false;
        }

        @Override
        public void onSpeakPaused() {
            Log.e("MCJ", "onSpeakPaused");
            mIsSpeakPaused = true;
        }

        @Override
        public void onSpeakResumed() {
            Log.e("MCJ", "onSpeakResumed");
            mIsSpeakPaused = false;
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            mIsSpeakPaused = false;
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                Log.e("MCJ", "onCompleted");
            } else {
                Log.e("MCJ", "onCompleted:" + error.getPlainDescription(true));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }
    };

    private void setParam() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SpeechApp.getContext());
        mTts.setParameter(SpeechConstant.PARAMS, null);
        if (ENGINE_TYPE.equals(SpeechConstant.TYPE_CLOUD)) {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            mTts.setParameter(SpeechConstant.VOICE_NAME, sharedPreferences.getString("voice_talker",
                    SpeechApp.getContext().getString(R.string.def_voice_talker_value)));
            mTts.setParameter(SpeechConstant.SPEED, sharedPreferences.getString("speed_preference",
                    SpeechApp.getContext().getString(R.string.voice_speed_def_value)));
            mTts.setParameter(SpeechConstant.PITCH, sharedPreferences.getString("pitch_preference",
                    SpeechApp.getContext().getString(R.string.voice_pitch_def_value)));
            mTts.setParameter(SpeechConstant.VOLUME, sharedPreferences.getString("volume_preference",
                    SpeechApp.getContext().getString(R.string.voice_volume_def_value)));
        } else {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
            mTts.setParameter(SpeechConstant.VOICE_NAME, "");
        }
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/tts.wav");
    }

    public boolean isSpeeching() {
        return mTts.isSpeaking();
    }

    public boolean isSpeakPaused() {
        return mIsSpeakPaused;
    }

    public void startSpeech(String text) {
        FlowerCollector.onEvent(SpeechApp.getContext(), "tts_start");
        setParam();
        mTts.stopSpeaking();
        mTts.startSpeaking(text, mTtsListener);
    }

    public void stopSpeech() {
        mTts.stopSpeaking();
    }

    public void pauseSpeech() {
        mTts.pauseSpeaking();
    }

    public void resumeSpeech() {
        mTts.resumeSpeaking();
    }
}
