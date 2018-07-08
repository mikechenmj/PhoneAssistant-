package com.chenmj.phoneassistant.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.chenmj.phoneassistant.R;
import com.chenmj.phoneassistant.bean.ChatDetailItem;
import com.chenmj.phoneassistant.adapter.ChatDetailItemAdapter;

import java.util.List;

/**
 * Created by 健哥哥 on 2018/4/14.
 */

public class CharDetailItemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_detail_item_activity_layout);
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        List<ChatDetailItem> listItems = intent.getParcelableArrayListExtra("data");
        for (ChatDetailItem chatDetailItem : listItems) {
            Log.d("MCJ", "chatDetailItem: " + chatDetailItem);
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(title);
        }
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(this,2);
        recyclerView.setLayoutManager(layoutManager);
        ChatDetailItemAdapter chatDetailItemAdapter = new ChatDetailItemAdapter(listItems);
        recyclerView.setAdapter(chatDetailItemAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
        }
        return true;
    }
}
