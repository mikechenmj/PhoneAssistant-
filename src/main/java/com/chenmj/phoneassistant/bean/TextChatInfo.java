package com.chenmj.phoneassistant.bean;

/**
 * Created by 健哥哥 on 2018/4/12.
 */

public class TextChatInfo extends TypeChatInfo {

    protected String text;

    public TextChatInfo(int type,String text) {
        super(type);
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void  setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "TextChatInfo{" +
                "text='" + text + '\'' +
                ", type=" + type +
                '}';
    }
}
