package vn.com.vng.zalopay.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.JsonObject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.data.ws.callback.OnReceiverMessageListener;
import vn.com.vng.zalopay.data.ws.connection.WsConnection;
import vn.com.vng.zalopay.data.ws.model.Event;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.internal.di.components.ApplicationComponent;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.utils.NotificationHelper;

/**
 * Created by AnhHieu on 6/14/16.
 */
public class NotificationService extends Service implements OnReceiverMessageListener {

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

    @Inject
    NotificationStore.LocalStorage localStorage;

    public NotificationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        boolean isInject = doInject();

        if (isInject) {
            eventBus.register(this);
            mWsConnection.addReceiverListener(this);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (NetworkHelper.isNetworkAvailable(getApplicationContext())) {
            this.connectAndSendAuthentication();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Timber.d("onDestroy");

        try {
            eventBus.unregister(this);
            mWsConnection.clearOnScrollListeners();
            mWsConnection.disconnect();

        } catch (Exception ex) {
            Timber.w(ex, " onDestroy");
        }

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
        try {
            if (mWsConnection.isConnected()) {
                mWsConnection.sendAuthentication();
            } else {
                mWsConnection.connect();
            }
        } catch (Exception ex) {
            Timber.w(ex, " onStartCommand");
        }
    }

    @Override
    public void onReceiverEvent(Event event) {
        Timber.d("onReceiverEvent %s", event.msgType);
        if (event instanceof NotificationData) {
            onReceiverNotification((NotificationData) event);
        }
    }


    private void onReceiverNotification(NotificationData event) {
        this.showNotification(event);
        this.updateTransaction();
        this.updateBalance();
        localStorage.put(event);
    }

    private void showNotification(NotificationData event) {
        if (!event.read) {
            String message = TextUtils.isEmpty(event.message) ? getString(R.string.notify_from_zalopay) : event.message;
            String title = getString(R.string.app_name);

            int notificationId = 1;
            int notificationType = event.getNotificationType();
            int transType = event.getTransType();

            Intent intent = null;

            if (transType > 0) {
                intent = navigator.getIntentMiniAppActivity(getApplicationContext(), Constants.ModuleName.NOTIFICATIONS);
            } else if (notificationType == 2) {
                intent = navigator.intentProfile(getApplicationContext());
            }

            notificationHelper.create(getApplicationContext(), notificationId,
                    intent,
                    R.mipmap.ic_launcher,
                    title, message);
        }
    }

    protected void updateTransaction() {
        UserComponent userComponent = getUserComponent();
        if (userComponent != null) {
            userComponent.transactionRepository().updateTransaction()
                    .subscribeOn(Schedulers.io())
                    .subscribe(new DefaultSubscriber<Boolean>());
        }
    }

    protected void updateBalance() {
        UserComponent userComponent = getUserComponent();
        if (userComponent != null) {
            userComponent.balanceRepository().updateBalance()
                    .subscribeOn(Schedulers.io())
                    .subscribe(new DefaultSubscriber<>());
        }
    }

    protected UserComponent getUserComponent() {
        return AndroidApplication.instance().getUserComponent();
    }

    private boolean doInject() {
        createUserComponent();

        if (getUserComponent() != null) {
            getUserComponent().inject(this);
        } else {
            stopSelf();
            return false;
        }

        return true;

    }

    private void createUserComponent() {
        Timber.d(" user component %s", getUserComponent());
        if (getUserComponent() != null)
            return;

        UserConfig userConfig = getAppComponent().userConfig();
        Timber.d(" userConfig %s", userConfig.isSignIn());
        if (userConfig.isSignIn()) {
            userConfig.loadConfig();
            AndroidApplication.instance().createUserComponent(userConfig.getCurrentUser());
        }
    }


    public ApplicationComponent getAppComponent() {
        return AndroidApplication.instance().getAppComponent();
    }
}
