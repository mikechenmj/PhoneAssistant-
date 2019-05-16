package com.chenmj.phoneassistant.xunfei;

import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.LexiconListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.util.ContactManager;
import com.iflytek.sunflower.FlowerCollector;
import com.chenmj.phoneassistant.application.SpeechApp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by 健哥哥 on 2018/4/9.
 */

public class SpeechRecognizerController {

    private final static String TAG = "MCJ";

    private final SpeechRecognizer mIat;

    private final HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();

    private OnRecognizerListener mOnRecognizerListener;

    private SpeechRecognizerController() {
        mIat = SpeechRecognizer.createRecognizer(SpeechApp.getContext(), mInitListener);
        if (mIat != null) {
            ContactManager mgr = ContactManager.createManager(SpeechApp.getContext(),
                    mContactListener);
            mgr.asyncQueryAllContactsName();
            setParam();
        }
    }

    public static SpeechRecognizerController getInstance() {
        return SpeechRecognizerControllerHolder.INSTANCE;
    }

    private static class SpeechRecognizerControllerHolder {
        private static final SpeechRecognizerController INSTANCE = new SpeechRecognizerController();  //创建实例的地方
    }

    private final ContactManager.ContactListener mContactListener = new ContactManager.ContactListener() {

        @Override
        public void onContactQueryFinish(final String contactInfos, boolean changeFlag) {
            if (changeFlag) {
                mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
                mIat.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
                int ret = mIat.updateLexicon("contact", contactInfos, mLexiconListener);
                if (ret != ErrorCode.SUCCESS) {
                    Log.d("MCJ", "上传联系人失败：" + ret);
                } else {
                    Log.d("MCJ", "contactInfos：" + contactInfos);
                }
            }
        }
    };

    private final InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Log.d(TAG, "init fail");
            }
        }
    };

    private final RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            Log.d(TAG, "onBeginOfSpeech ");
        }

        @Override
        public void onError(SpeechError error) {
            Log.d(TAG, "onError: " + error.getPlainDescription(true));
            if (mOnRecognizerListener != null) {
                mOnRecognizerListener.onEnd();
            }
        }

        @Override
        public void onEndOfSpeech() {
            Log.d(TAG, "onEndOfSpeech");
            if (mOnRecognizerListener != null) {
                mOnRecognizerListener.onEnd();
            }
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            String result = getResult(results);
            boolean isEmpty = TextUtils.isEmpty(result);
            if (!isEmpty && mOnRecognizerListener != null) {
                mOnRecognizerListener.onResult(result);
                mOnRecognizerListener.onEnd();
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            if (mOnRecognizerListener != null) {
                mOnRecognizerListener.onVolumeChanged(volume, data);
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }
    };

    private final LexiconListener mLexiconListener = new LexiconListener() {

        @Override
        public void onLexiconUpdated(String lexiconId, SpeechError error) {
            if (error != null) {
                Log.e("MCJ", "onLexiconUpdated: " + error.toString());
            } else {
                Log.i("MCJ", "onLexiconUpdated success");
            }
        }
    };

    public void recognize(OnRecognizerListener listener) {
        FlowerCollector.onEvent(SpeechApp.getContext(), "iat_recognize");
        setParam();
        mOnRecognizerListener = listener;
        mIatResults.clear();
        int ret = mIat.startListening(mRecognizerListener);
        Log.i("MCJ", "ret: " + ret);
    }
    public void stopRecognize() {
        mIat.stopListening();
    }

    public void cancelRecognize() {
        if (mIat.isListening()) {
            mIat.cancel();
        }
    }

    public boolean isRecognizeing() {
        return mIat.isListening();
    }

    private String getResult(RecognizerResult results) {
        String text = XunfeiResultJsonParser.parseIatResult(results.getResultString());

        String sn = null;
        boolean ls = false;
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
            ls = Boolean.parseBoolean(resultJson.optString("ls"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);
        if (!ls) {
            return null;
        }
        StringBuilder resultBuffer = new StringBuilder();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        return resultBuffer.toString();
    }

    private void setParam() {
        mIat.setParameter(SpeechConstant.PARAMS, null);

        mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin");
        mIat.setParameter(SpeechConstant.VAD_BOS, "3000");
        mIat.setParameter(SpeechConstant.VAD_EOS, "1500");
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");
    }

    public interface OnRecognizerListener {
        void onResult(String result);

        void onVolumeChanged(int volume, byte[] data);

        void onEnd();
    }
}
