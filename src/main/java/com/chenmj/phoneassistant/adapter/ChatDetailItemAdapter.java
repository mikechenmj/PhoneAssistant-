package com.chenmj.phoneassistant.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chenmj.phoneassistant.R;
import com.chenmj.phoneassistant.bean.ChatDetailItem;

import java.util.List;

public class ChatDetailItemAdapter extends RecyclerView.Adapter<ChatDetailItemAdapter.ViewHolder> {

    private Context mContext;
    private LayoutInflater mInflater;
    private final List<ChatDetailItem> mChatList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View root;

        public ViewHolder(View view) {
            super(view);
            root = view;
        }
    }

    public ChatDetailItemAdapter(List<ChatDetailItem> chatList) {
        mChatList = chatList;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
            mInflater = LayoutInflater.from(mContext);
        }
        View view = mInflater.inflate(R.layout.chat_detail_card_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                ChatDetailItem intentChatInfo = mChatList.get(position);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse(intentChatInfo.getLinkUrl()));
                mContext.startActivity(intent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatDetailItem chatDetailItem = (ChatDetailItem) mChatList.get(position);
        ImageView imageView = holder.root.findViewById(R.id.item_image);
        imageView.setImageBitmap(null);
        Glide.with(mContext).load(chatDetailItem.getImageUrl()).into(imageView);
        TextView textView = holder.root.findViewById(R.id.item_name);
        textView.setText(chatDetailItem.getText());
    }

    @Override
    public int getItemCount() {
        return mChatList.size();
    }

}
