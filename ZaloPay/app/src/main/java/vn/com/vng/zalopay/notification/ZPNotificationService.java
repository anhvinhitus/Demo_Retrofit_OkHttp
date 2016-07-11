package vn.com.vng.zalopay.notification;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.data.ws.callback.OnReceiverMessageListener;
import vn.com.vng.zalopay.data.ws.connection.WsConnection;
import vn.com.vng.zalopay.data.ws.model.Event;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.internal.di.components.ApplicationComponent;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.navigation.Navigator;

public class ZPNotificationService extends Service implements OnReceiverMessageListener {

    private static final String TAG = "ZPNotificationService";
    private static final String[] TOPICS = {"global"};

    /*Server API Key: AIzaSyCweupE81mBm3_m8VOoFTUbuhBF82r_GwI
    Sender ID: 386726389536*/

    public ZPNotificationService() {
    }

    final EventBus eventBus = AndroidApplication.instance().getAppComponent().eventBus();
    final SharedPreferences sharedPreferences = AndroidApplication.instance().getAppComponent().sharedPreferences();

    @Inject
    WsConnection mWsConnection;

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
        boolean isInject = doInject();
        if (isInject) {
            mWsConnection.addReceiverListener(this);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d("onStartCommand: flags %s startId %s", flags, startId);
        if (NetworkHelper.isNetworkAvailable(getApplicationContext())) {
            connectToServer();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Timber.d("onDestroy");
        eventBus.unregister(this);
        super.onDestroy();
    }


    private void connectToServer() {
        try {
            InstanceID instanceID = InstanceID.getInstance(this);

            Timber.d("onHandleIntent: senderId %s", getString(R.string.gcm_defaultSenderId));

            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            connect(token);

            subscribeTopics(token);
            sharedPreferences.edit().putBoolean(Constants.SENT_TOKEN_TO_SERVER, true).apply();
        } catch (Exception ex) {
            sharedPreferences.edit().putBoolean(Constants.SENT_TOKEN_TO_SERVER, false).apply();
        }
    }

    private void connect(String token) {
        Timber.d("connect with token %s", token);
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

    protected UserComponent getUserComponent() {
        return AndroidApplication.instance().getUserComponent();
    }

}
