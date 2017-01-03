package vn.com.vng.zalopay.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import timber.log.Timber;
import vn.com.vng.zalopay.utils.AppVersionUtils;

/**
 * Created by longlv on 1/3/17.
 * *
 */

public class UpgradedAppReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("on upgraded app receiver, data[%s]", intent);
        Uri packageName = intent.getData();
        if (packageName.toString().equals("package:" + context.getPackageName())) {
            //Zalo Pay was upgraded.
            Timber.d("on upgraded Zalo Pay.");
            AppVersionUtils.clearData();
        }
    }
}
