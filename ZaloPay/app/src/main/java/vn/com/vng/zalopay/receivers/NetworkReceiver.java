package vn.com.vng.zalopay.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.greenrobot.eventbus.EventBus;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.network.BaseNetworkInterceptor;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.utils.AndroidUtils;

/**
 * Created by AnhHieu on 6/14/16.
 * *
 */
public class NetworkReceiver extends BroadcastReceiver {

    EventBus eventBus = AndroidApplication.instance().getAppComponent().eventBus();

    public NetworkReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isOnline = NetworkHelper.isNetworkAvailable(context);
        Timber.d("Network State Change %s", isOnline);
        eventBus.post(new NetworkChangeEvent(isOnline));
        if (isOnline) {
            BaseNetworkInterceptor.CONNECTION_TYPE = AndroidUtils.getNetworkClass();
        }
    }
}
