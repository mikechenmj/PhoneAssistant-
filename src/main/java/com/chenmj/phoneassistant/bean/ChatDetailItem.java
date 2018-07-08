package com.chenmj.phoneassistant.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by 健哥哥 on 2018/4/14.
 */

public class ChatDetailItem implements Parcelable{

    private String text;
    private String imageUrl;
    private String linkUrl;

    public ChatDetailItem(String text, String imageUrl, String linkUrl) {
        this.text = text;
        this.imageUrl = imageUrl;
        this.linkUrl = linkUrl;
    }
    protected ChatDetailItem(Parcel in) {
        text = in.readString();
        imageUrl = in.readString();
        linkUrl = in.readString();
    }

    public static final Creator<ChatDetailItem> CREATOR = new Creator<ChatDetailItem>() {
        @Override
        public ChatDetailItem createFromParcel(Parcel in) {
            return new ChatDetailItem(in);
        }

        @Override
        public ChatDetailItem[] newArray(int size) {
            return new ChatDetailItem[size];
        }
    };

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeString(imageUrl);
        dest.writeString(linkUrl);
    }

    @Override
    public String toString() {
        return "ChatDetailItem{" +
                "text='" + text + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", linkUrl='" + linkUrl + '\'' +
                '}';
    }
}
