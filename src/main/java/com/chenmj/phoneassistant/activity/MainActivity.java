package com.chenmj.phoneassistant.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.os.Build;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.chenmj.phoneassistant.R;
import com.chenmj.phoneassistant.bean.ChatDetailItem;
import com.chenmj.phoneassistant.bean.IntentChatTextInfo;
import com.chenmj.phoneassistant.bean.SynthesizerStateTextInfo;
import com.chenmj.phoneassistant.bean.TextChatInfo;
import com.chenmj.phoneassistant.bean.TypeChatInfo;
import com.chenmj.phoneassistant.bean.UrlChatTextInfo;
import com.chenmj.phoneassistant.fragment.BaseNavFragment;
import com.chenmj.phoneassistant.fragment.HelpFragment;
import com.chenmj.phoneassistant.network.ChatInfoAsyncTask;
import com.chenmj.phoneassistant.network.CheckNetwork;
import com.chenmj.phoneassistant.listener.AppBarStateChangeListener;
import com.chenmj.phoneassistant.adapter.ChatAdapter;
import com.chenmj.phoneassistant.util.AssistantFunctionController;
import com.chenmj.phoneassistant.util.Constant;
import com.chenmj.phoneassistant.util.PermissionHelper;
import com.chenmj.phoneassistant.xunfei.SpeechRecognizerController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, HelpFragment.MessageSender {

    private SpeechRecognizerController mSpeechRecognizerController;
    private AssistantFunctionController mAssistantFunctionController;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private EditText mInputEditText;
    private FloatingActionButton mSendButton;
    private List<TypeChatInfo> mLists;
    private ChatAdapter mChatAdapter;
    private FloatingActionButton mInputSwitch;
    private LinearLayout mInputContainer;
    private CardView mInputCardView;
    private DrawerLayout mDrawerLayout;
    private boolean mIsEditTextInputMode;
    private FloatingActionButton mVoiceRecognize;
    private AppBarLayout mAppBarLayout;
    private float mInputContainerTranslationX = -1;
    private ArrayList<ChatInfoAsyncTask> mChatInfoAsyncTasks;

    private static final int REQUEST_CODE = 1000;
    private static final String[] PERMISSION_NEEDED = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private final ChatInfoAsyncTask.ChatHttpDataListener mHatHttpDataListener = new ChatInfoAsyncTask.ChatHttpDataListener() {
        @Override
        public void getData(String data) {
            praseTulingResultJson(data);
        }
    };

    private final SpeechRecognizerController.OnRecognizerListener mOnRecognizerListener =
            new SpeechRecognizerController.OnRecognizerListener() {

                private static final float ALPHA = 0.70f;

                private final Paint mPaint = new Paint();
                private float mRatio;

                @Override
                public void onResult(String result) {
                    pullMessage(result);
                }

                @Override
                public void onVolumeChanged(int volume, byte[] data) {
                    float ratio = volume / 30f;
                    if (ratio < 0.6f) {
                        ratio = (float) (0.5f * Math.random()) + 0.1f;
                    }
                    mRatio = ALPHA * mRatio + (1 - ALPHA) * ratio;
                    mVoiceRecognize.setImageBitmap(getVolumeChangedBitmap(mRatio));
                    Log.i("MCJ", "onVolumeChanged");
                }

                @Override
                public void onEnd() {
                    Log.i("MCJ", "onEnd");
                    mVoiceRecognize.setImageResource(R.drawable.ic_perm_group_microphone);
                }

                private Bitmap getVolumeChangedBitmap(float ratio) {
                    Bitmap micBitmap = BitmapFactory.decodeResource(getResources(),
                            R.drawable.ic_perm_group_microphone);
                    Bitmap bitmap = Bitmap.createBitmap(micBitmap.getWidth(),
                            micBitmap.getHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    mPaint.setAntiAlias(true);
                    mPaint.setShader(new LinearGradient(bitmap.getWidth() / 2, bitmap.getHeight(),
                            bitmap.getWidth() / 2, 0, getResources().getColor(R.color.colorPrimary),
                            getResources().getColor(R.color.colorPrimaryDark), Shader.TileMode.CLAMP));
                    canvas.drawRect(0, bitmap.getHeight() * (1 - ratio), bitmap.getWidth(), bitmap.getHeight(), mPaint);
                    mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
                    canvas.drawBitmap(micBitmap, 0, 0, mPaint);
                    mPaint.setXfermode(null);
                    return bitmap;
                }
            };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        CheckNetwork.checkNetwork(this);

        /**
         * use android:windowFullscreen instead
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
         int flag = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
         getWindow().getDecorView().setSystemUiVisibility(flag);
         }
         */

        /**
         * use android:statusBarColor instead
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
         getWindow().setStatusBarColor(Color.TRANSPARENT);
         }
         */
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVoiceRecognize.setImageResource(R.drawable.ic_perm_group_microphone);
    }

    private void init() {
        mLists = new ArrayList<>();
        mChatInfoAsyncTasks = new ArrayList<ChatInfoAsyncTask>();
        initView();
        initNavFragment();
        if (!PermissionHelper.checkPermission(this, PERMISSION_NEEDED)) {
            PermissionHelper.requestPermissions(this, PERMISSION_NEEDED, REQUEST_CODE, null);
        } else {
            initXunfei();
            initAssistantFunction();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE:
                PermissionHelper.onRequestPermissionsResult(this, permissions, grantResults, new PermissionHelper.PermissionCallback() {
                    @Override
                    public boolean onPermissionGrantedStates(String permission, boolean isGranted, boolean shouldShowRationale) {
                        return false;
                    }

                    @Override
                    public void onAllGranted(boolean isAllGranted) {
                        if (!isAllGranted) {
                            Toast.makeText(MainActivity.this, getString(R.string.without_permission_tip), Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            initXunfei();
                            initAssistantFunction();
                        }
                    }
                });
                break;
        }
    }

    private void initXunfei() {
        mSpeechRecognizerController = SpeechRecognizerController.getInstance();
    }

    private void initAssistantFunction() {
        mAssistantFunctionController = AssistantFunctionController.getInstance();
        mAssistantFunctionController.init(this);
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                FragmentManager fragmentManager = getFragmentManager();
                Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_content);
                boolean isBaseNavFragment = fragment instanceof BaseNavFragment;
                if (!isBaseNavFragment) {
                    MainActivity.super.onBackPressed();
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        mChatAdapter = new ChatAdapter(mLists);
        mRecyclerView = (RecyclerView) findViewById(R.id.main_recycler);
        mRecyclerView.setAdapter(mChatAdapter);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mInputContainer = (LinearLayout) findViewById(R.id.input_edit_container);
        mInputEditText = (EditText) findViewById(R.id.input_edit);
        mSendButton = (FloatingActionButton) findViewById(R.id.send_button);
        mSendButton.setOnClickListener(this);
        mVoiceRecognize = (FloatingActionButton) findViewById(R.id.voice_recognize);
        mVoiceRecognize.setOnClickListener(this);
        mInputSwitch = (FloatingActionButton) findViewById(R.id.input_switch);
        mInputSwitch.setOnClickListener(this);
        mInputCardView = (CardView) findViewById(R.id.input_card);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.appBar);
        if (mIsEditTextInputMode) {
            mInputSwitch.setImageResource(R.drawable.ic_perm_group_microphone);
            mVoiceRecognize.setVisibility(View.INVISIBLE);
        } else {
            mInputSwitch.setImageResource(R.drawable.ic_send);
            mInputContainer.setVisibility(View.INVISIBLE);
        }
    }

    private void initNavFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_content, new BaseNavFragment()).commit();
        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.fragment_content, new HelpFragment()).commit();
        mDrawerLayout.openDrawer(Gravity.START);
    }

    private void initCollapsing() {
        final CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        mAppBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, int state) {
                switch (state) {
                    case AppBarStateChangeListener.EXPANDED:
                        collapsingToolbar.setTitle("");
                        break;
                    case AppBarStateChangeListener.IDLE:
                        collapsingToolbar.setTitle("");
                        break;
                    case AppBarStateChangeListener.COLLAPSED:
                        collapsingToolbar.setTitle(getResources().getString(R.string.app_name));
                        break;
                    default:
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSpeechRecognizerController != null) {
            mSpeechRecognizerController.cancelRecognize();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mChatInfoAsyncTasks != null) {
            for (ChatInfoAsyncTask chatInfoAsyncTask : mChatInfoAsyncTasks) {
                chatInfoAsyncTask.cancel(true);
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_content);
        if (mDrawerLayout.isDrawerOpen(Gravity.START) && fragment instanceof BaseNavFragment) {
            mDrawerLayout.closeDrawer(Gravity.START);
            return;
        }
        super.onBackPressed();
    }

    private void praseTulingResultJson(String result) {
        try {
            SynthesizerStateTextInfo chatInfo;
            JSONArray jsonArray;
            JSONObject arrayJSONObj;
            JSONObject jsonObject = new JSONObject(result);
            switch (Integer.parseInt(jsonObject.getString("code"))) {
                case Constant.TYPE_TEXT_CODE:
                    chatInfo = new SynthesizerStateTextInfo(TypeChatInfo.CHAT_TYPE_RECEIVE, jsonObject.getString("text"));
                    break;

                case Constant.TYPE_LINK_CODE:
                    chatInfo = new UrlChatTextInfo(jsonObject.getString("text"), jsonObject.getString("url"));
                    break;

                case Constant.TYPE_NEWS_CODE:
                    jsonArray = jsonObject.getJSONArray("list");
                    ArrayList<ChatDetailItem> newDetailItems = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        arrayJSONObj = jsonArray.getJSONObject(i);
                        String article = arrayJSONObj.getString("article");
                        String icon = arrayJSONObj.getString("icon");
                        String detailUrl = arrayJSONObj.getString("detailurl");
                        ChatDetailItem chatDetailItem = new ChatDetailItem(article, icon, detailUrl);
                        newDetailItems.add(chatDetailItem);
                    }
                    Intent newsIntent = new Intent(this, CharDetailItemActivity.class);
                    newsIntent.putExtra("title", getString(R.string.news_title));
                    newsIntent.putParcelableArrayListExtra("data", newDetailItems);
                    chatInfo = new IntentChatTextInfo(getString(R.string.open_news_tip), newsIntent);
                    break;

                case Constant.TYPE_COOK_CODE:
                    jsonArray = jsonObject.getJSONArray("list");
                    ArrayList<ChatDetailItem> cookDetailItems = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        arrayJSONObj = jsonArray.getJSONObject(i);
                        String name = arrayJSONObj.getString("name");
                        String icon = arrayJSONObj.getString("icon");
                        String detailUrl = arrayJSONObj.getString("detailurl");
                        ChatDetailItem chatDetailItem = new ChatDetailItem(name, icon, detailUrl);
                        cookDetailItems.add(chatDetailItem);
                    }
                    Intent cookIntent = new Intent(this, CharDetailItemActivity.class);
                    cookIntent.putExtra("title", getString(R.string.cook_title));
                    cookIntent.putParcelableArrayListExtra("data", cookDetailItems);
                    chatInfo = new IntentChatTextInfo(getString(R.string.open_cook_tip), cookIntent);
                    break;

                default:
                    chatInfo = new SynthesizerStateTextInfo(TypeChatInfo.CHAT_TYPE_RECEIVE, jsonObject.getString("text"));
                    break;
            }
            mLists.add(chatInfo);
            mChatAdapter.notifyDataSetChanged();
            scrollToEnd();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean smartLauncherDialer(String content) {
        IntentChatTextInfo[] intentChatTextInfos = mAssistantFunctionController.smartLauncherDialer(content, this);
        Collections.addAll(mLists, intentChatTextInfos);
        return intentChatTextInfos.length > 0;
    }

    private boolean smartLaunchApplication(String content) {
        return mAssistantFunctionController.smartLaunchApplication(content, this);
    }


    private void requestData(String testStr) {
        String contentStr = testStr.replace(" ", ",");
        String postUrl = new StringBuilder(Constant.API_URL)
                .append(URLEncoder.encode(contentStr))
                .append(Constant.USER_ID)
                .toString();
        Log.i("MCJ", "postUrl: " + postUrl);
        ChatInfoAsyncTask ChatInfoAsyncTask = (ChatInfoAsyncTask) new ChatInfoAsyncTask(postUrl, mHatHttpDataListener, mChatInfoAsyncTasks).execute();
        mChatInfoAsyncTasks.add(ChatInfoAsyncTask);
    }

    private void scrollToEnd() {
        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mLayoutManager.scrollToPosition(mLists.size() - 1);
            }
        }, 100);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_button:
                if (!CheckNetwork.checkNetwork(this)) {
                    return;
                }
                CheckNetwork.checkNetwork(this);
                pullMessage(mInputEditText.getText().toString());
                break;
            case R.id.input_switch:
                switchInputMode();
                break;
            case R.id.voice_recognize:
                if (!CheckNetwork.checkNetwork(this)) {
                    return;
                }
                if (mSpeechRecognizerController.isRecognizeing()) {
                    mSpeechRecognizerController.stopRecognize();
                } else {
                    mSpeechRecognizerController.recognize(mOnRecognizerListener);
                }
                break;
        }
    }

    private void pullMessage(final String inputText) {
        if (TextUtils.isEmpty(inputText)) {
            return;
        }

        mLists.add(new TextChatInfo(TypeChatInfo.CHAT_TYPE_SEND, inputText));
        mChatAdapter.notifyDataSetChanged();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!smartLauncherDialer(inputText) && !smartLaunchApplication(inputText)) {
                    requestData(inputText);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mChatAdapter.notifyDataSetChanged();
                        scrollToEnd();
                    }
                });
            }
        }).start();

        mInputEditText.setText("");
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
        mAppBarLayout.setExpanded(false, true);
    }

    private void switchInputMode() {
        mInputSwitch.setClickable(false);
        if (mInputContainerTranslationX == -1) {
            mInputContainerTranslationX = mSendButton.getWidth() / 2
                    + getResources().getDimension(R.dimen.floating_action_button_margin)
                    - mInputContainer.getWidth() / 2;
        }
        if (mIsEditTextInputMode) {
            mInputSwitch.setImageResource(R.drawable.ic_send);
            ObjectAnimator inputContainerAnimator = ObjectAnimator.ofFloat(mInputContainer,
                    "translationX", mInputContainerTranslationX);

            ObjectAnimator inputCardAnimator = ObjectAnimator.ofFloat(mInputCardView,
                    "scaleX", 0f);

            ObjectAnimator sendButtonAnimator = ObjectAnimator.ofFloat(mSendButton,
                    "alpha", 1f, 0f);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(inputCardAnimator).after(inputContainerAnimator).before(sendButtonAnimator);
            animatorSet.setDuration(200);
            animatorSet.start();
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mVoiceRecognize.setVisibility(View.VISIBLE);
                    mInputContainer.setVisibility(View.INVISIBLE);
                    ObjectAnimator voiceRecognizeAnimator = ObjectAnimator.ofFloat(mVoiceRecognize,
                            "alpha", 0f, 1f);
                    voiceRecognizeAnimator.setDuration(200);
                    voiceRecognizeAnimator.start();
                    voiceRecognizeAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mInputSwitch.setClickable(true);
                        }
                    });
                }
            });
        } else {
            mInputSwitch.setImageResource(R.drawable.ic_perm_group_microphone);
            ObjectAnimator voiceRecognizeAnimator = ObjectAnimator.ofFloat(mVoiceRecognize,
                    "alpha", 1f, 0f);
            voiceRecognizeAnimator.setDuration(200);
            voiceRecognizeAnimator.start();
            voiceRecognizeAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mInputContainer.setTranslationX(mInputContainerTranslationX);
                    mInputCardView.setScaleX(0);
                    mInputContainer.setVisibility(View.VISIBLE);
                    ObjectAnimator inputContainerAnimator = ObjectAnimator.ofFloat(mInputContainer,
                            "translationX", mInputContainerTranslationX, 0f);
                    ObjectAnimator inputCardAnimator = ObjectAnimator.ofFloat(mInputCardView,
                            "scaleX", 0f, 1f);
                    ObjectAnimator sendButtonAnimator = ObjectAnimator.ofFloat(mSendButton,
                            "alpha", 0f, 1f);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.play(inputCardAnimator).before(inputContainerAnimator).after(sendButtonAnimator);
                    animatorSet.setDuration(200);
                    animatorSet.start();
                    animatorSet.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mVoiceRecognize.setVisibility(View.INVISIBLE);
                            mInputSwitch.setClickable(true);
                        }
                    });
                }
            });
        }
        mIsEditTextInputMode = !mIsEditTextInputMode;
    }

    @Override
    public void sendMessage(String s) {
        pullMessage(s);
    }
}



