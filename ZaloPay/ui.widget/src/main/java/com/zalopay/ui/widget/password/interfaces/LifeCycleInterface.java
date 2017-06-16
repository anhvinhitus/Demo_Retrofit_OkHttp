package com.zalopay.ui.widget.password.interfaces;

import android.app.Activity;

public interface LifeCycleInterface {

    /**
     * Called in {@link Activity#onResume()}
     */
    public void onActivityResumed(Activity activity);

    /**
     * Called in {@link Activity#onPause()}
     */
    public void onActivityPaused(Activity activity);
}
