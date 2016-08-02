package vn.com.vng.zalopay.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.event.NetworkChangeEvent;

/**
 * Created by AnhHieu on 6/14/16.
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
    }
}
