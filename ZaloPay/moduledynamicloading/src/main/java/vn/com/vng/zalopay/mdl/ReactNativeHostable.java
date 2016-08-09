package vn.com.vng.zalopay.mdl;

import android.content.Context;

import com.facebook.react.ReactInstanceManager;

/**
 * Created by huuhoa on 7/15/16.
 * Manage instance of ReactInstanceManager
 */
public interface ReactNativeHostable {
    /**
     * Acquire a new instance of ReactInstanceManager
     * @return new instance of ReactInstanceManager
     */
    ReactInstanceManager acquireReactInstanceManager(ReactBasedActivity activity);
    void releaseReactInstanceManager(ReactBasedActivity activity, ReactInstanceManager instance, boolean forceRemove);

    void cleanup();

    /**
     * Handle exception caused on react native execution
     * @param activity based activity that hosts the react native module
     * @param e exception
     */
    void handleJSException(ReactBasedActivity activity, Exception e);

    Context getActivityContext();
    void setActivityContext(ReactBasedActivity activity);
}
