package com.chenmj.phoneassistant.bean;

/**
 * Created by 健哥哥 on 2018/4/12.
 */

public class UrlChatTextInfo extends SynthesizerStateTextInfo {

    private String url;

    public UrlChatTextInfo(String text, String url) {
        super(TypeChatInfo.CHAT_URL_TYPE_RECEIVE, text);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "UrlChatTextInfo{" +
                "text='" + text + '\'' +
                ", url='" + url + '\'' +
                ", type=" + type +
                '}';
    }
}
