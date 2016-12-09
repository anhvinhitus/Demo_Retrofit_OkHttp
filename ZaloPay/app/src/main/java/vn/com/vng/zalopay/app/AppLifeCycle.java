package vn.com.vng.zalopay.app;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

/**
 * Created by AnhHieu on 3/23/16.
 */
public class AppLifeCycle implements Application.ActivityLifecycleCallbacks {

    private static HashMap<String, Integer> activities;
    private static String mLastActivity;

    public AppLifeCycle() {
        activities = new HashMap<>();
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        String className = activity.getLocalClassName();
        activities.put(className, 1);
        mLastActivity = className;
        applicationStatus();
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
        activities.put(activity.getLocalClassName(), 0);
        applicationStatus();
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    public static boolean isBackGround() {
        for (String s : activities.keySet()) {
            if (activities.get(s) == 1) {
                return false;
            }
        }
        return true;
    }


    private short mLastState;

    private void applicationStatus() {
        Timber.i("Is application background " + isBackGround());
        if (isBackGround()) {
            //Do something if the application is in background
            if (mLastState == 0) {
                return;
            }
            mLastState = 0;

        } else {
            if (mLastState == 1) {
                return;
            }
            mLastState = 1;
        }
    }

    public static boolean activityExistInStack(String simpleName) {
        return false;
    }

    public static boolean isLastActivity(@NonNull String simpleName) {
        return simpleName.equalsIgnoreCase(mLastActivity);
    }
}