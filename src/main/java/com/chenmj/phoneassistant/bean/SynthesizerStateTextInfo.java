package com.chenmj.phoneassistant.bean;

/**
 * Created by 健哥哥 on 2018/4/25.
 */

public class SynthesizerStateTextInfo extends TextChatInfo {

    protected boolean hasSynthesized;

    public SynthesizerStateTextInfo(int type, String test) {
        super(type, test);
    }

    public boolean isHasSynthesized() {
        return hasSynthesized;
    }

    public void setHasSynthesized(boolean hasSynthesized) {
        this.hasSynthesized = hasSynthesized;
    }
}
