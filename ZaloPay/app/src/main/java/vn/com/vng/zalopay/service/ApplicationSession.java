package vn.com.vng.zalopay.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;

import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.navigation.Navigator;

/**
 * Created by huuhoa on 6/14/16.
 * Manage application session
 */
public class ApplicationSession {
    Navigator navigator;
    Context applicationContext;
    private String mLoginMessage;

    public ApplicationSession(Context applicationContext, Navigator navigator) {
        this.applicationContext = applicationContext;
        this.navigator = navigator;
    }

    /**
     * Clear current user session and move to login state
     */
    public void clearUserSession() {
        // clear current user DB
        UserConfig userConfig = AndroidApplication.instance().getAppComponent().userConfig();
        userConfig.clearConfig();
        userConfig.setCurrentUser(null);

        // move to login
        ZaloSDK.Instance.unauthenticate();
        AndroidApplication.instance().releaseUserComponent();

        //cancel notification
        NotificationManagerCompat nm = NotificationManagerCompat.from(applicationContext);
        nm.cancelAll();

        applicationContext.stopService(new Intent(applicationContext, NotificationService.class));

        if (TextUtils.isEmpty(mLoginMessage)) {
            navigator.startLoginActivity(applicationContext, true);
        } else {
            navigator.startLoginActivity(applicationContext, mLoginMessage);
            mLoginMessage = null;
        }
    }

    public void setMessageAtLogin(String message) {
        mLoginMessage = message;
    }

    /**
     * New user session and move to main state
     */
    public void newUserSession() {

    }
}
