package com.chenmj.phoneassistant.listener;

import android.support.design.widget.AppBarLayout;

/**
 * Created by mikechenmj on 18-4-3.
 */

public abstract class AppBarStateChangeListener implements AppBarLayout.OnOffsetChangedListener {

    public static final int EXPANDED = 0;
    public static final int COLLAPSED = 1;
    public static final int IDLE = 2;

    @Override
    public final void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        int state;
        if (i == 0) {
            state = EXPANDED;
        } else if (Math.abs(i) >= appBarLayout.getTotalScrollRange()) {
            state = COLLAPSED;
        } else {
            state = IDLE;
        }
        onStateChanged(appBarLayout, state);
    }

    public abstract void onStateChanged(AppBarLayout appBarLayout, int state);
}
