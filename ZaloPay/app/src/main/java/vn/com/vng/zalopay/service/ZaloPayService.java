package vn.com.vng.zalopay.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.data.ws.connection.WsConnection;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.utils.NotificationHelper;

/**
 * Created by AnhHieu on 6/14/16.
 */
public class ZaloPayService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Inject
    WsConnection mWsConnection;

    @Inject
    EventBus eventBus;

    @Inject
    NotificationHelper notificationHelper;

    public ZaloPayService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.d("onCreate thread %s", Thread.currentThread().getName());
        AndroidApplication.instance().getAppComponent().inject(this);
        eventBus.register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Timber.d("onStartCommand %s startId %s thread %s", intent, startId, Thread.currentThread().getName());
        this.connectAndSendAuthentication();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        eventBus.unregister(this);
        super.onDestroy();
    }


    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNetworkChange(NetworkChangeEvent event) {
        Timber.d("onNetworkChange %s", event.isOnline);

        if (event.isOnline) {
            this.connectAndSendAuthentication();
        }
    }


    private void connectAndSendAuthentication() {
        if (mWsConnection.isConnected()) {
            mWsConnection.sendAuthentication();
        } else {
            mWsConnection.connect();
        }
    }
}
