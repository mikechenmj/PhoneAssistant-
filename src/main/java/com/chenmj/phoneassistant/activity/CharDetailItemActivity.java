package com.chenmj.phoneassistant.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.chenmj.phoneassistant.R;
import com.chenmj.phoneassistant.bean.ChatDetailItem;
import com.chenmj.phoneassistant.adapter.ChatDetailItemAdapter;
import com.chenmj.phoneassistant.view.TransitionAnimationView;

import java.util.List;

/**
 * Created by 健哥哥 on 2018/4/14.
 */

public class CharDetailItemActivity extends AppCompatActivity {

    private boolean mEnterReturnTransitionAnimation;
    private TransitionAnimationView mEnterReturnTransitionAnimationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_detail_item_activity_layout);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            int flag = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            getWindow().getDecorView().setSystemUiVisibility(flag);
        }

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        List<ChatDetailItem> listItems = intent.getParcelableArrayListExtra("data");

        final ViewGroup rootContent = getWindow().getDecorView().findViewById(android.R.id.content);
        byte[] bitmapData = intent.getByteArrayExtra(TransitionAnimationView.EXTRA_TRANSITION_BITMAP);
        final Bitmap transitionForeground = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
        final Rect rect = intent.getSourceBounds();
        mEnterReturnTransitionAnimation = transitionForeground != null && !rect.isEmpty();
        if (mEnterReturnTransitionAnimation) {
            mEnterReturnTransitionAnimationView = new TransitionAnimationView(CharDetailItemActivity.this);
            rootContent.addView(mEnterReturnTransitionAnimationView);
            mEnterReturnTransitionAnimationView.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {

                        private boolean hasTransition;

                        @Override
                        public void onGlobalLayout() {
                            if (hasTransition) {
                                return;
                            } else {
                                hasTransition = true;
                            }

                            mEnterReturnTransitionAnimationView.startTransitionAnimation(null,
                                    TransitionAnimationView.DURATION, rect, false, transitionForeground);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                mEnterReturnTransitionAnimationView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            }
                        }
                    });
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(title);
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
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

    @Override
    public void finish() {
        if (mEnterReturnTransitionAnimation) {
            byte[] bitmapData = getIntent().getByteArrayExtra(TransitionAnimationView.EXTRA_TRANSITION_BITMAP);
            final Bitmap background = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
            AnimatorListenerAdapter animatorListenerAdapter = new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    CharDetailItemActivity.super.finish();
                    overridePendingTransition(0, 0);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    CharDetailItemActivity.super.finish();
                    overridePendingTransition(0, 0);
                }
            };
            View rootContent = findViewById(android.R.id.content);
            rootContent.setDrawingCacheEnabled(true);
            Bitmap foreground = rootContent.getDrawingCache();
            foreground = foreground.copy(Bitmap.Config.ARGB_8888, true);
            rootContent.setDrawingCacheEnabled(false);
            mEnterReturnTransitionAnimationView.setTransitionBackground(background);
            mEnterReturnTransitionAnimationView.startTransitionAnimation(animatorListenerAdapter,
                    TransitionAnimationView.DURATION, getIntent().getSourceBounds(),
                    true, foreground);
        } else {
            super.finish();
        }
    }
}
