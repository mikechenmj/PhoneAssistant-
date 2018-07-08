package com.chenmj.phoneassistant.util;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;

import com.chenmj.phoneassistant.R;
import com.chenmj.phoneassistant.bean.IntentChatTextInfo;
import com.chenmj.phoneassistant.bean.MusicInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by 健哥哥 on 2018/4/26.
 */

public class AssistantFunctionController {

    private final String[] LAUNCHER_APPLICATION_WORDS = new String[]{
            "启动", "打开",
    };

    private final String[] LAUNCHER_CALL_WORDS = new String[]{
            "打电话", "拨打", "拨号", "呼叫", "打给"
    };

    private HashMap<String, Intent> mApplicationInfos;
    private HashMap<String, String> mContactInfos;

    private AssistantFunctionController() {
    }

    public void init(final Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addDataScheme("package");
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, Intent intent) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        readLauncherAppInfo(context);
                    }
                }).start();
            }
        };
        context.registerReceiver(broadcastReceiver, intentFilter);

        context.getContentResolver().registerContentObserver(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                true, new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange) {
                        readContacts(context);
                    }
                });

        context.getContentResolver().registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                true, new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange) {
                        readMusicList(context);
                    }
                });

        new Thread(new Runnable() {
            @Override
            public void run() {
                readLauncherAppInfo(context);
                readContacts(context);
                readMusicList(context);
            }
        }).start();
    }

    private void readMusicList(Context context) {
        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA
        };
        List<MusicInfo> musicList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection, null, null,
                    new StringBuilder(MediaStore.Audio.Media.TITLE).append(" ASC").toString());

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    MusicInfo music = new MusicInfo(id, name, artist, data);
                    musicList.add(music);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public IntentChatTextInfo[] smartLauncherDialer(String content, Context context) {
        if (mContactInfos == null) {
            readContacts(context);
        }
        String[] numbers = null;
        String name = "";
        IntentChatTextInfo[] intentChatTextInfos = new IntentChatTextInfo[]{};
        if (mContactInfos.containsKey(content)) {
            numbers = mContactInfos.get(content).split("/");
            name = content;
        }
        Iterator iterator = mContactInfos.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String keyName = (String) entry.getKey();
            if (content.contains(keyName)) {
                if (content.length() < keyName.length() + 2) {
                    name = content;
                    numbers = ((String) entry.getValue()).split("/");
                }
                for (String callWord : LAUNCHER_CALL_WORDS) {
                    if (content.contains(callWord)) {
                        if ((content.length() - keyName.length()) < 6) {
                            name = content;
                            numbers = ((String) entry.getValue()).split("/");
                        }
                    }
                }
            }
        }
        if (numbers != null) {
            int length = numbers.length;
            if (length < 1) {
                return intentChatTextInfos;
            }
            boolean launcherImmediate = length == 1;
            intentChatTextInfos = new IntentChatTextInfo[length];
            for (int i = 0; i < length; i++) {
                String number = numbers[i];
                Log.e("MCJ", "number: " + number);
                if (name.lastIndexOf("。") == name.length() - 1) {
                    name = name.substring(0, name.length() - 1);
                }
                String dialerTip = context.getString(R.string.dialer_tip, name, number);
                Log.e("MCJ", "dialerTip: " + dialerTip);
                intentChatTextInfos[i] = new IntentChatTextInfo(dialerTip, getDialerIntent(number), launcherImmediate);
                intentChatTextInfos[i].setHasSynthesized(true);
            }
        }
        return intentChatTextInfos;
    }

    private Intent getDialerIntent(String number) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + number));
        return intent;
    }

    public boolean smartLaunchApplication(String content, Context context) {
        if (mApplicationInfos == null) {
            readLauncherAppInfo(context);
        }
        if (mApplicationInfos.containsKey(content)) {
            smartLaunchApplication(mApplicationInfos.get(content), context);
            return true;
        }
        Iterator iterator = mApplicationInfos.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String applicationName = (String) entry.getKey();
            if (content.contains(applicationName) && content.length() < applicationName.length() + 2) {
                smartLaunchApplication((Intent) entry.getValue(), context);
                return true;
            } else {
                for (String launcherWord : LAUNCHER_APPLICATION_WORDS) {
                    if (content.contains(launcherWord)) {
                        if (content.contains(applicationName)) {
                            if ((content.length() - applicationName.length()) < 6) {
                                smartLaunchApplication((Intent) entry.getValue(), context);
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private void smartLaunchApplication(Intent intent, Context context) {
        context.startActivity(intent);
    }

    private void readLauncherAppInfo(Context context) {
        Intent launcherIntent = new Intent("android.intent.action.MAIN");
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(launcherIntent, 0);
        Log.d("MCJ", "list.size: " + list.size());
        mApplicationInfos = new HashMap<>();
        for (ResolveInfo resolveInfo : list) {
            String applicationLabel = (String) resolveInfo.activityInfo.loadLabel(context.getPackageManager());
            String pkg = resolveInfo.activityInfo.packageName;
            String cls = resolveInfo.activityInfo.name;
            ComponentName componentName = new ComponentName(pkg, cls);
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setComponent(componentName);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            mApplicationInfos.put(applicationLabel, intent);
        }
    }

    private void readContacts(Context context) {
        mContactInfos = new HashMap<>();
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String displayName = cursor.getString(cursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                    ));
                    String number = cursor.getString(cursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NUMBER
                    ));
                    StringBuilder valueStringBuilder = new StringBuilder();
                    if (mContactInfos.get(displayName) != null) {
                        valueStringBuilder.append(mContactInfos.get(displayName)).append("/");
                    }
                    valueStringBuilder.append(number);
                    mContactInfos.put(displayName, valueStringBuilder.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("MCJ", "e: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static AssistantFunctionController getInstance() {

        return AssistantFunctionController.AssistantFunctionControllerHolder.INSTANCE;
    }

    private static class AssistantFunctionControllerHolder {
        private static final AssistantFunctionController INSTANCE = new AssistantFunctionController();
    }
}
