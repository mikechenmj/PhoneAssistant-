package com.chenmj.phoneassistant.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chenmj.phoneassistant.R;
import com.chenmj.phoneassistant.bean.IntentChatTextInfo;
import com.chenmj.phoneassistant.bean.SynthesizerStateTextInfo;
import com.chenmj.phoneassistant.bean.TextChatInfo;
import com.chenmj.phoneassistant.bean.TypeChatInfo;
import com.chenmj.phoneassistant.bean.UrlChatTextInfo;
import com.chenmj.phoneassistant.xunfei.SpeechSynthesizerController;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private Context mContext;
    private LayoutInflater mInflater;
    private final List<TypeChatInfo> mChatList;
    private int mPressPosition = -1;

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View root;

        public ViewHolder(View view) {
            super(view);
            root = view;
        }
    }

    public ChatAdapter(List<TypeChatInfo> chatList) {
        mChatList = chatList;
    }

    @Override
    public int getItemViewType(int position) {
        return mChatList.get(position).getType();
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
            mInflater = LayoutInflater.from(mContext);
        }
        View view;
        switch (viewType) {
            case TypeChatInfo.CHAT_TYPE_SEND:
                view = mInflater.inflate(R.layout.chat_send_item, parent, false);
                break;
            case TypeChatInfo.CHAT_TYPE_RECEIVE:
            case TypeChatInfo.CHAT_URL_TYPE_RECEIVE:
            case TypeChatInfo.CHAT_INTENT_TYPE_RECEIVE:
                view = mInflater.inflate(R.layout.chat_simgle_text_receive_item, parent, false);
                break;
            default:
                throw new RuntimeException("error type of TypeChatInfo: " + viewType);
        }
        final ViewHolder holder = new ViewHolder(view);
        if (viewType != TypeChatInfo.CHAT_TYPE_SEND) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getAdapterPosition();
                    boolean isSameItem = position == mPressPosition;
                    mPressPosition = position;
                    String text = ((TextView)v.findViewById(R.id.chat_text)).getText().toString();
                    if (!TextUtils.isEmpty(text)) {
                        SpeechSynthesizerController speechSynthesizerController = SpeechSynthesizerController.getInstance();
                        if (!isSameItem) {
                            speechSynthesizerController.startSpeech(text);
                            return;
                        }
                        if (!speechSynthesizerController.isSpeeching()) {
                                speechSynthesizerController.startSpeech(text);
                        }else {
                            if (speechSynthesizerController.isSpeakPaused()) {
                                speechSynthesizerController.resumeSpeech();
                            }else {
                                speechSynthesizerController.pauseSpeech();
                            }
                        }
                    }
                }
            });
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mPressPosition = holder.getAdapterPosition();
                    String text = ((TextView)v.findViewById(R.id.chat_text)).getText().toString();
                    if (!TextUtils.isEmpty(text)) {
                        SpeechSynthesizerController speechSynthesizerController = SpeechSynthesizerController.getInstance();
                        speechSynthesizerController.startSpeech(text);
                    }
                    return true;
                }
            });
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int type = getItemViewType(position);
        TextChatInfo textChatInfo = (TextChatInfo) mChatList.get(position);
        TextView textView = holder.root.findViewById(R.id.chat_text);
        CharSequence content = textChatInfo.getText();
        if (type == TypeChatInfo.CHAT_TYPE_SEND || type == TypeChatInfo.CHAT_TYPE_RECEIVE) {
            content = textChatInfo.getText();
        } else if (type == TextChatInfo.CHAT_URL_TYPE_RECEIVE) {
            content = mContext.getString(R.string.open_url_tip);
            UrlChatTextInfo urlChatInfo = (UrlChatTextInfo) textChatInfo;
            SpannableString spannableString = new SpannableString(content);
            spannableString.setSpan(new URLSpan(urlChatInfo.getUrl()),
                    0, content.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(textView.getCurrentTextColor()),
                    0, content.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            content = spannableString;
        }else if (type == TypeChatInfo.CHAT_INTENT_TYPE_RECEIVE) {
            final IntentChatTextInfo intentChatInfo = (IntentChatTextInfo) textChatInfo;
            content = intentChatInfo.getText();
            SpannableString spannableString = new SpannableString(content);
            spannableString.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    mContext.startActivity(intentChatInfo.getIntent());
                }
            },0, content.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(textView.getCurrentTextColor()),
                    0, content.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            if (intentChatInfo.isShouldLauncherImmediate()) {
                mContext.startActivity(intentChatInfo.getIntent());
                intentChatInfo.setShouldLauncherImmediate(false);
            }
            content = spannableString;
        }

        textView.setText(content);
        if (type != TypeChatInfo.CHAT_TYPE_SEND) {
            SynthesizerStateTextInfo synthesizerStateTextInfo = (SynthesizerStateTextInfo) textChatInfo;
            if (!synthesizerStateTextInfo.isHasSynthesized()) {
                holder.root.performClick();
                synthesizerStateTextInfo.setHasSynthesized(true);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mChatList.size();
    }

}
