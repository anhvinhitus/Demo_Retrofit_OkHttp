package vn.com.vng.zalopay.app;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.HashMap;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.internal.di.components.ApplicationComponent;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.ui.activity.MainActivity;

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
        checkCreatedIfRootActivity(activity, savedInstanceState);
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
        //   Timber.i("Is application background " + isBackGround());
        if (isBackGround()) {
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

    public static boolean isLastActivity(@NonNull String simpleName) {
        return simpleName.equalsIgnoreCase(mLastActivity);
    }

    private void checkCreatedIfRootActivity(Activity activity, Bundle savedInstanceState) {
       // Timber.d("Created savedInstanceState %s activities %s getUserComponent() %s", savedInstanceState, activities, getUserComponent());

        if (savedInstanceState == null || !activities.isEmpty()) {
            return;
        }

        if (activity.getClass().getSimpleName().equals(MainActivity.TAG)) {
            return;
        }

        createUserComponent();

        if (getUserComponent() == null) {
            return;
        }

        getUserComponent().userSession().ensureUserInitialized();
    }

    private void createUserComponent() {

        Timber.d(" user component %s", getUserComponent());

        if (getUserComponent() != null) {
            return;
        }

        UserConfig userConfig = getAppComponent().userConfig();
        Timber.d(" isSignIn %s", userConfig.isSignIn());
        if (userConfig.isSignIn()) {
            userConfig.loadConfig();
            AndroidApplication.instance().createUserComponent(userConfig.getCurrentUser());
        }
    }

    public ApplicationComponent getAppComponent() {
        return AndroidApplication.instance().getAppComponent();
    }

    private UserComponent getUserComponent() {
        return AndroidApplication.instance().getUserComponent();
    }

}