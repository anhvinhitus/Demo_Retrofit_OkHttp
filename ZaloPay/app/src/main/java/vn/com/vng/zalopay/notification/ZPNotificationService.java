package vn.com.vng.zalopay.notification;

import android.content.Context;

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
import vn.com.vng.zalopay.data.NetworkError;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.eventbus.NotificationChangeEvent;
import vn.com.vng.zalopay.data.eventbus.ReadNotifyEvent;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.data.ws.callback.OnReceiverMessageListener;
import vn.com.vng.zalopay.data.ws.connection.WsConnection;
import vn.com.vng.zalopay.data.ws.model.AuthenticationData;
import vn.com.vng.zalopay.data.ws.model.Event;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.data.ws.parser.MessageParser;
import vn.com.vng.zalopay.domain.executor.ThreadExecutor;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.event.TokenGCMRefreshEvent;
import vn.com.vng.zalopay.internal.di.components.ApplicationComponent;
import vn.com.vng.zalopay.internal.di.components.UserComponent;

public class ZPNotificationService implements OnReceiverMessageListener {

    private static final String[] TOPICS = {"global"};

    /*Server API Key: AIzaSyCweupE81mBm3_m8VOoFTUbuhBF82r_GwI
    Sender ID: 386726389536*/

    private boolean mIsSubscribeGcm = false;

    @Inject
    Context mContext;

    @Inject
    EventBus mEventBus;

    @Inject
    UserConfig mUserConfig;

    @Inject
    Gson mGson;

    WsConnection mWsConnection;

    @Inject
    NotificationHelper notificationHelper;

    @Inject
    ThreadExecutor mExecutor;

    @Inject
    public ZPNotificationService() {

    }

    public void startNotificationService() {
        Timber.d("startNotificationService");
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }

        ensureInitializeNetworkConnection();

        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                connectToServer();
            }
        });
    }

    public void destroy() {
        Timber.d("destroy");
        mIsSubscribeGcm = false;
        if (mEventBus.isRegistered(this)) {
            mEventBus.unregister(this);
        }
        if (mWsConnection != null) {
            mWsConnection.disconnect();
            mWsConnection.clearReceiverListener();
            mWsConnection.cleanup();
            mWsConnection = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        notificationHelper = null;
        Timber.d("Finalize ZPNotificationService");
    }

    private void connectToServer() {
        String token = null;

        try {
            InstanceID instanceID = InstanceID.getInstance(mContext);
//            Timber.d("onHandleIntent: senderId %s", getString(R.string.gcm_defaultSenderId));
            token = instanceID.getToken(mContext.getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            subscribeTopics(token);
            // sharedPreferences.edit().putBoolean(Constants.SENT_TOKEN_TO_SERVER, true).apply();
        } catch (Exception ex) {
            Timber.d(ex, "exception in working with GCM");
            //  sharedPreferences.edit().putBoolean(Constants.SENT_TOKEN_TO_SERVER, false).apply();
        }

        this.connect(token);
    }

    private void connect(String token) {
        Timber.d("connect with token %s", token);
        if (!NetworkHelper.isNetworkAvailable(mContext)) {
            Timber.d("Skip create connection, since OS reports no network connection");
            return;
        }

        ensureInitializeNetworkConnection();

        if (!mWsConnection.isConnected()) {
            Timber.d("Socket is not connected. About to create connection.");
            mWsConnection.setGCMToken(token);
            mWsConnection.connect();
        } else {
            Timber.d("Socket is already connected. Do nothing.");
        }
    }

    private void disconnectServer() {
        Timber.d("Request to disconnect connection with notification server");
        if (mWsConnection == null) {
            return;
        }

        mWsConnection.disconnect();
    }

    @Override
    public void onReceiverEvent(Event event) {
        if (event instanceof AuthenticationData) {
            AuthenticationData authenticationData = (AuthenticationData) event;
            if (authenticationData.code != NetworkError.SUCCESSFUL) {
                if (authenticationData.code == NetworkError.UM_TOKEN_NOT_FOUND ||
                        authenticationData.code == NetworkError.UM_TOKEN_EXPIRE ||
                        authenticationData.code == NetworkError.TOKEN_INVALID) {
                    // session expired
                    Timber.d("Session is expired");
                    // clear user session and logout

                    getAppComponent().applicationSession().setMessageAtLogin(R.string.exception_token_expired_message);
                    getAppComponent().applicationSession().clearUserSession();
                }
            } else {
                Timber.d("Socket authentication succeeded");
            }
        } else if (event instanceof NotificationData) {
            if (notificationHelper == null) {
                return;
            }

            notificationHelper.processNotification((NotificationData) event);
        }
    }

    private void subscribeTopics(String token) throws IOException {
        Timber.d("subscribeTopics mIsSubscribeGcm [%s] token [%s]", mIsSubscribeGcm, token);
        if (mIsSubscribeGcm) {
            return;
        }
        GcmPubSub pubSub = GcmPubSub.getInstance(mContext);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
        mIsSubscribeGcm = true;
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNetworkChange(NetworkChangeEvent event) {
        Timber.d("onNetworkChange %s", event.isOnline);
        if (event.isOnline) {
            this.connectToServer();
        } else {
            this.disconnectServer();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReadNotify(ReadNotifyEvent event) {
        notificationHelper.closeNotificationSystem();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNotificationUpdated(NotificationChangeEvent event) {
        Timber.d("on Notification updated %s", event.isRead());
        if (!event.isRead()) {
            notificationHelper.showNotificationSystem();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.BACKGROUND)
    public void onTokenGcmRefresh(TokenGCMRefreshEvent event) {
        Timber.d("on Token GCM Refresh event %s", event);
        TokenGCMRefreshEvent stickyEvent = mEventBus.getStickyEvent(TokenGCMRefreshEvent.class);
        // Better check that an event was actually posted before
        if(stickyEvent != null) {
            // "Consume" the sticky event
            mEventBus.removeStickyEvent(stickyEvent);
            mIsSubscribeGcm = false;
            startNotificationService();
        }
    }

    private void ensureInitializeNetworkConnection() {
        if (mWsConnection == null) {
            mWsConnection = new WsConnection(BuildConfig.WS_HOST, BuildConfig.WS_PORT, mContext,
                    new MessageParser(mGson), mUserConfig);
            mWsConnection.addReceiverListener(this);
        }
    }

    public ApplicationComponent getAppComponent() {
        return AndroidApplication.instance().getAppComponent();
    }

    protected UserComponent getUserComponent() {
        return AndroidApplication.instance().getUserComponent();
    }

}
