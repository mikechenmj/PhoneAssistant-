package com.chenmj.phoneassistant.listener;

import android.content.Context;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Toast;

import com.chenmj.phoneassistant.R;

import java.util.regex.Pattern;

/**
 * 输入框输入范围控制
 */
public class SettingTextWatcher implements TextWatcher {
    private int editStart;
    private int editCount;
    private final EditTextPreference mEditTextPreference;
    private final int minValue;
    private final int maxValue;
    private final Context mContext;

    public SettingTextWatcher(Context context, EditTextPreference e, int min, int max) {
        mContext = context;
        mEditTextPreference = e;
        minValue = min;
        maxValue = max;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        editStart = start;
        editCount = count;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (TextUtils.isEmpty(s)) {
            return;
        }
        String content = s.toString();
        if (isNumeric(content)) {
            int num = Integer.parseInt(content);
            if (num > maxValue || num < minValue) {
                s.delete(editStart, editStart + editCount);
                mEditTextPreference.getEditText().setText(s);
                Toast.makeText(mContext, R.string.settings_value_out_of_limit_tip, Toast.LENGTH_SHORT).show();
            }
        } else {
            s.delete(editStart, editStart + editCount);
            mEditTextPreference.getEditText().setText(s);
            Toast.makeText(mContext, R.string.settings_value_format_wrong_tip, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 正则表达式-判断是否为数字
     */
    private static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }
}
