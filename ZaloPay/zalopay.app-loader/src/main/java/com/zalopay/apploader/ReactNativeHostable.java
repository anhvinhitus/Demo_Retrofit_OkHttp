package com.zalopay.apploader;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;

import com.facebook.react.ReactInstanceManager;

/**
 * Created by huuhoa on 7/15/16.
 * Manage instance of ReactInstanceManager
 */
public interface ReactNativeHostable {
    /**
     * Acquire a new instance of ReactInstanceManager
     *
     * @return new instance of ReactInstanceManager
     */
    ReactInstanceManager acquireReactInstanceManager(ReactInstanceDelegate delegate);

    void releaseReactInstanceManager(ReactInstanceDelegate activity, ReactInstanceManager instance, boolean forceRemove);

    void cleanup();

    /**
     * Handle exception caused on react native execution
     *
     * @param activity based activity that hosts the react native module
     * @param e        exception
     */
    void handleJSException(ReactInstanceDelegate activity, Exception e);

    @Nullable
    Context getActivityContext();

    void setActivityContext(Activity activity);
}
