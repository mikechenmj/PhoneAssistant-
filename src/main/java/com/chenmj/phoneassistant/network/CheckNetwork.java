package com.chenmj.phoneassistant.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.chenmj.phoneassistant.R;

/**
 * Created by Administrator on 16-3-5.
 */
public class CheckNetwork {

    public static boolean checkNetwork(Context context) {
        boolean isAvailable = false;
        ConnectivityManager manager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            Toast.makeText(context, R.string.internet_not_connected, Toast.LENGTH_LONG).show();
            return false;
        }
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null) {
            if (!networkInfo.isAvailable()) {
                Toast.makeText(context, R.string.internet_not_connected, Toast.LENGTH_LONG).show();
                isAvailable = false;
            } else {
                isAvailable = true;
            }
        }
        return isAvailable;
    }
}
