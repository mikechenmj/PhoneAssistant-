package com.chenmj.phoneassistant.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import com.chenmj.phoneassistant.R;
import com.iflytek.msc.MSC;

/**
 * Created by xy on 2018/4/29.
 */

public class HelpFragment extends PreferenceFragment {

    private MessageSender mSender;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.help_fragment);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) {
            view.setBackgroundColor(Color.WHITE);
        }
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        ListAdapter listAdapter = preferenceScreen.getRootAdapter();
        int count = listAdapter.getCount();
        for (int i = 0; i < count; i++) {
            Preference p = (Preference) listAdapter.getItem(i);
            p.setOnPreferenceClickListener(preferenceChangeListener);
        }
        return view;
    }

    private Preference.OnPreferenceClickListener preferenceChangeListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (mSender == null) {
                if (getActivity() instanceof MessageSender) {
                    mSender = (MessageSender) getActivity();
                }else {
                    return false;
                }
            }
            String message = preference.getSummary().toString().replace("\"", "");
            mSender.sendMessage(message);
            return false;
        }
    };

    public void setMessageSender(MessageSender messageSender) {
        mSender = messageSender;
    }

    public interface MessageSender {
        public void sendMessage(String s);
    }
}
