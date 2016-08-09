package vn.com.vng.zalopay.mdl;

import com.facebook.react.bridge.NativeModuleCallExceptionHandler;

import java.lang.ref.WeakReference;

/**
 * Created by huuhoa on 7/15/16.
 * Handle NativeModuleCallExceptionHandler
 */
class HandleReactNativeException implements NativeModuleCallExceptionHandler {
    WeakReference<ReactNativeHostable> mInstanceReference;
    WeakReference<ReactBasedActivity> mActivityReference;

    public HandleReactNativeException(ReactNativeHostable instanceManager, ReactBasedActivity activity) {
        mInstanceReference = new WeakReference<>(instanceManager);
        mActivityReference = new WeakReference<>(activity);
    }

    @Override
    public void handleException(Exception e) {
        ReactBasedActivity basedActivity = mActivityReference.get();
        ReactNativeHostable manager = mInstanceReference.get();
        if (basedActivity != null && manager != null) {
           manager.handleJSException(basedActivity, e);
        }
    }
}
