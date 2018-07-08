package com.chenmj.phoneassistant.bean;

/**
 * Created by 健哥哥 on 2018/4/12.
 */

public abstract class TypeChatInfo {

    public static final int CHAT_TYPE_SEND = 0;

    public static final int CHAT_TYPE_RECEIVE = 1;

    public static final int CHAT_URL_TYPE_RECEIVE = 2;

    public static final int CHAT_INTENT_TYPE_RECEIVE = 3;

    protected int type;

    public TypeChatInfo(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "TypeChatInfo{" +
                "type=" + type +
                '}';
    }
}
