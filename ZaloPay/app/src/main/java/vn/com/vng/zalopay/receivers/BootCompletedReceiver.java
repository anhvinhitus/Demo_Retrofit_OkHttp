package vn.com.vng.zalopay.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import java.io.IOException;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.notification.GcmHelper;

/**
 * Created by longlv on 11/25/16.
 * Handle event when device reboot completed.
 */

public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        UserConfig userConfig = AndroidApplication.instance().getAppComponent().userConfig();
        Timber.d(" mUserConfig %s", userConfig.isSignIn());
        if (!userConfig.isSignIn()) {
            return;
        }
        String token = GcmHelper.getTokenGcm(context);
        if (TextUtils.isEmpty(token)) {
            return;
        }
        try {
            GcmHelper.subscribeTopics(context, token);
            Timber.w("Subscribe topics successful, token[%s]", token);
        } catch (IOException e) {
            Timber.w(e, "Subscribe topics exception");
        }
    }
}
