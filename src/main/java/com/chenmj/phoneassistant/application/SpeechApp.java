package com.chenmj.phoneassistant.application;

import android.app.Application;
import android.content.Context;

import com.iflytek.cloud.SpeechUtility;

import java.lang.ref.WeakReference;

public class SpeechApp extends Application {

	private static WeakReference<Context> mContext;

	@Override
	public void onCreate() {
		SpeechUtility.createUtility(SpeechApp.this, "appid=" + "5aaaa215");
		super.onCreate();
		mContext = new WeakReference<Context>(getApplicationContext());
	}

	public static Context getContext() {
		return mContext.get();
	}
	
}
