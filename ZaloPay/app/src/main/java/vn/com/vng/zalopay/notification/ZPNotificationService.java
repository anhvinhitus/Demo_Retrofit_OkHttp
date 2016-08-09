package vn.com.vng.zalopay.notification;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.eventbus.NotificationChangeEvent;
import vn.com.vng.zalopay.data.eventbus.ReadNotifyEvent;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.data.ws.SocketConnection;
import vn.com.vng.zalopay.data.ws.callback.OnReceiverMessageListener;
import vn.com.vng.zalopay.data.ws.connection.WsConnection;
import vn.com.vng.zalopay.data.ws.model.Event;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.data.ws.parser.MessageParser;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.internal.di.components.ApplicationComponent;
import vn.com.vng.zalopay.internal.di.components.UserComponent;

public class ZPNotificationService extends Service implements OnReceiverMessageListener {

    private static final String[] TOPICS = {"global"};

    /*Server API Key: AIzaSyCweupE81mBm3_m8VOoFTUbuhBF82r_GwI
    Sender ID: 386726389536*/

    public ZPNotificationService() {
    }

    final EventBus eventBus = AndroidApplication.instance().getAppComponent().eventBus();
    final UserConfig userConfig = AndroidApplication.instance().getAppComponent().userConfig();
    final Gson mGson = AndroidApplication.instance().getAppComponent().gson();

    SocketConnection mWsConnection;

    @Inject
    NotificationHelper notificationHelper;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.d("onCreate");
        eventBus.register(this);
        doInject();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d("onStartCommand: flags %s startId %s", flags, startId);
        if (NetworkHelper.isNetworkAvailable(this)) {

            if (mWsConnection == null) {
                mWsConnection = new SocketConnection(BuildConfig.WS_HOST, BuildConfig.WS_PORT, this,
                        new MessageParser(userConfig, mGson), userConfig);
                mWsConnection.addReceiverListener(this);
            }

            getAppComponent().threadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    connectToServer();
                }
            });
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Timber.d("onDestroy");
        eventBus.unregister(this);
        if (mWsConnection != null) {
            mWsConnection.disconnect();
            mWsConnection.clearReceiverListener();
            mWsConnection = null;
        }
        super.onDestroy();
    }


    private void connectToServer() {
        String token = null;

        try {
            InstanceID instanceID = InstanceID.getInstance(this);

            Timber.d("onHandleIntent: senderId %s", getString(R.string.gcm_defaultSenderId));

            token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            subscribeTopics(token);
            // sharedPreferences.edit().putBoolean(Constants.SENT_TOKEN_TO_SERVER, true).apply();
        } catch (Exception ex) {
            Timber.e(ex, "exception");
            //  sharedPreferences.edit().putBoolean(Constants.SENT_TOKEN_TO_SERVER, false).apply();
        }

        this.connect(token);
    }

    private void connect(String token) {
        Timber.d("connect with token %s", token);
        if (mWsConnection == null) {
            return;
        }

        if (!mWsConnection.isConnected()) {
            mWsConnection.setGCMToken(token);
            mWsConnection.connect();
        }
    }

    @Override
    public void onReceiverEvent(Event event) {
        if (event instanceof NotificationData) {
            notificationHelper.processNotification((NotificationData) event);
        }
    }

    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNetworkChange(NetworkChangeEvent event) {
        Timber.d("onNetworkChange %s", event.isOnline);
        if (event.isOnline) {
            this.connectToServer();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReadNotify(ReadNotifyEvent event) {
        notificationHelper.closeNotificationSystem();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNotificationUpdated(NotificationChangeEvent event) {
        Timber.d("on Notification updated %s", event.read);
        if (!event.read) {
            notificationHelper.showNotificationSystem();
        }
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
        if (getUserComponent() != null) {
            return;
        }

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

    protected UserComponent getUserComponent() {
        return AndroidApplication.instance().getUserComponent();
    }

}
