package com.chenmj.phoneassistant.bean;

import android.content.Intent;

/**
 * Created by 健哥哥 on 2018/4/14.
 */

public class IntentChatTextInfo extends SynthesizerStateTextInfo {

    private Intent intent;

    private boolean shouldLauncherImmediate;

    public IntentChatTextInfo(String text, Intent intent) {
        super(TypeChatInfo.CHAT_INTENT_TYPE_RECEIVE, text);
        this.intent = intent;
    }

    public IntentChatTextInfo(String text, Intent intent,boolean shouldLauncherImmediate) {
        super(TypeChatInfo.CHAT_INTENT_TYPE_RECEIVE, text);
        this.intent = intent;
        this.shouldLauncherImmediate = shouldLauncherImmediate;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public boolean isShouldLauncherImmediate() {
        return shouldLauncherImmediate;
    }

    public void setShouldLauncherImmediate(boolean shouldLauncherImmediate) {
        this.shouldLauncherImmediate = shouldLauncherImmediate;
    }

    @Override
    public String toString() {
        return "IntentChatTextInfo{" +
                "text='" + text + '\'' +
                ", intent=" + intent +
                ", type=" + type +
                ", shouldLauncherImmediate=" + shouldLauncherImmediate +
                '}';
    }
}
