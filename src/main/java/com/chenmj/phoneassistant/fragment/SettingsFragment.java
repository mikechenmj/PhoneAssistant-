package com.chenmj.phoneassistant.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chenmj.phoneassistant.R;
import com.chenmj.phoneassistant.listener.SettingTextWatcher;

/**
 * Created by mikechenmj on 18-4-26.
 */

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener{

    private ListPreference mTalkerListPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_fragment);

        mTalkerListPreference = (ListPreference) findPreference("voice_talker");
        mTalkerListPreference.setSummary(mTalkerListPreference.getEntry());

        EditTextPreference speedPreference = (EditTextPreference)findPreference("speed_preference");
        speedPreference.getEditText().addTextChangedListener(new SettingTextWatcher(getActivity(),speedPreference,0,100));
        speedPreference.setSummary(speedPreference.getText());

        EditTextPreference pitchPreference = (EditTextPreference)findPreference("pitch_preference");
        pitchPreference.getEditText().addTextChangedListener(new SettingTextWatcher(getActivity(),pitchPreference,0,100));
        pitchPreference.setSummary(pitchPreference.getText());

        EditTextPreference volumePreference = (EditTextPreference)findPreference("volume_preference");
        volumePreference.getEditText().addTextChangedListener(new SettingTextWatcher(getActivity(),volumePreference,0,100));
        volumePreference.setSummary(volumePreference.getText());

        speedPreference.setOnPreferenceChangeListener(this);
        pitchPreference.setOnPreferenceChangeListener(this);
        volumePreference.setOnPreferenceChangeListener(this);
        mTalkerListPreference.setOnPreferenceChangeListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) {
            view.setBackgroundColor(Color.WHITE);
        }
        return view;
    }

    @Override
    public boolean onPreferenceChange(final Preference preference, Object newValue) {
        if (preference == mTalkerListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            final String entry = (String) listPreference.getEntry();
            final String value = listPreference.getValue();
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    preference.setSummary(((ListPreference) preference).getEntry());
                    Log.e("MCJ", "entry: " + entry);
                    Log.e("MCJ", "value: " + value);
                }
            });
        }else {
            preference.setSummary((String) newValue);
        }
        return true;
    }
}
