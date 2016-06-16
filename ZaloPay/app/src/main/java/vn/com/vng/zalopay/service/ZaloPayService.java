package vn.com.vng.zalopay.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.ws.callback.OnReceiverMessageListener;
import vn.com.vng.zalopay.data.ws.connection.WsConnection;
import vn.com.vng.zalopay.data.ws.model.AuthenticationData;
import vn.com.vng.zalopay.data.ws.model.Event;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.utils.NotificationHelper;

/**
 * Created by AnhHieu on 6/14/16.
 */
public class ZaloPayService extends Service implements OnReceiverMessageListener {

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

    @Inject
    Navigator navigator;

    public ZaloPayService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.d("onCreate thread %s", Thread.currentThread().getName());
        AndroidApplication.instance().getAppComponent().inject(this);
        eventBus.register(this);
        mWsConnection.addReceiverListener(this);
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
        mWsConnection.removeReceiverListener(this);
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

    @Override
    public void onReceiverEvent(Event event) {
        Timber.d("onReceiverEvent %s", event.msgType);
        if (event instanceof NotificationData) {
            notificationHelper.create(getApplicationContext(), 1, navigator.getIntentMiniAppActivity(getApplicationContext(), "Notifications"), R.mipmap.ic_launcher, "Zalo Pay", ((NotificationData) event).message);
        } else if (event instanceof AuthenticationData) {
            Toast.makeText(ZaloPayService.this, "Authentication success", Toast.LENGTH_SHORT).show();
        }
    }
}
