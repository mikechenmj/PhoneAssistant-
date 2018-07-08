package com.chenmj.phoneassistant.network;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * chenmj 20160226 add for chat
 */

public class ChatInfoAsyncTask extends AsyncTask<String, Void, String> {

    private URL mUrl;
    private final ChatHttpDataListener mHttpDataListener;
    private final ArrayList<ChatInfoAsyncTask> mChatInfoAsyncTasks;

    public ChatInfoAsyncTask(String url, ChatHttpDataListener httpDataListener,ArrayList<ChatInfoAsyncTask> chatInfoAsyncTasks) {
        mHttpDataListener = httpDataListener;
        mChatInfoAsyncTasks = chatInfoAsyncTasks;
        try {
            mUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String doInBackground(String... params) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            connection = (HttpURLConnection) mUrl.openConnection();
            connection.setRequestMethod("GET");
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            mHttpDataListener.getData(result);
        }
        mChatInfoAsyncTasks.remove(this);
        super.onPostExecute(result);
    }


    public interface ChatHttpDataListener {

        void getData(String data);


    }



}
