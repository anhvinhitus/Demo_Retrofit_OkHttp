package vn.com.vng.zalopay.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.notification.ZPNotificationService;

/**
 * Created by AnhHieu on 8/2/16.
 */
public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("receive boot complete");
        UserComponent userComponent = AndroidApplication.instance().getUserComponent();
        if (userComponent != null) {
            Timber.d("Start notification service");
            context.startService(new Intent(context, ZPNotificationService.class));
        }
    }
}
