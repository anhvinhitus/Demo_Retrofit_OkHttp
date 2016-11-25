package vn.com.vng.zalopay.notification;

import android.content.Context;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.NetworkError;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.eventbus.NotificationChangeEvent;
import vn.com.vng.zalopay.data.eventbus.ReadNotifyEvent;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.data.ws.callback.OnReceiverMessageListener;
import vn.com.vng.zalopay.data.ws.connection.NotificationApiHelper;
import vn.com.vng.zalopay.data.ws.connection.NotificationApiMessage;
import vn.com.vng.zalopay.data.ws.connection.WsConnection;
import vn.com.vng.zalopay.data.ws.model.AuthenticationData;
import vn.com.vng.zalopay.data.ws.model.Event;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.data.ws.model.RecoveryMessageEvent;
import vn.com.vng.zalopay.data.ws.parser.MessageParser;
import vn.com.vng.zalopay.domain.executor.ThreadExecutor;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.event.TokenGCMRefreshEvent;
import vn.com.vng.zalopay.internal.di.components.ApplicationComponent;
import vn.com.vng.zalopay.internal.di.components.UserComponent;

public class ZPNotificationService implements OnReceiverMessageListener {

    /*Server API Key: AIzaSyCweupE81mBm3_m8VOoFTUbuhBF82r_GwI
    Sender ID: 386726389536*/

    private boolean mIsSubscribeGcm = false;
    private WsConnection mWsConnection;

    @Inject
    Context mContext;

    @Inject
    EventBus mEventBus;

    @Inject
    UserConfig mUserConfig;

    @Inject
    Gson mGson;

    @Inject
    NotificationHelper notificationHelper;

    @Inject
    ThreadExecutor mExecutor;

    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    private final int NUMBER_NOTIFICATION = 30;

    @Inject
    ZPNotificationService() {

    }

    public void startNotificationService() {
        Timber.d("startNotificationService");
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }

        ensureInitializeNetworkConnection();

        if (mExecutor != null) {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    connectToServer();
                }
            });
        }
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
            token = GcmHelper.getTokenGcm(mContext);
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
            if (authenticationData.result != NetworkError.SUCCESSFUL) {
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
                this.recoveryNotification(true);

            }
        } else if (event instanceof NotificationData) {
            if (notificationHelper == null) {
                return;
            }

            notificationHelper.processNotification((NotificationData) event);
        } else if (event instanceof RecoveryMessageEvent) {
            List<NotificationData> listMessage = ((RecoveryMessageEvent) event).listNotify;
            Timber.d("RecoveryMessageEvent %s", listMessage);

            if (Lists.isEmptyOrNull(listMessage)) {
                return;
            }

            if (notificationHelper != null) {
                notificationHelper.recoveryNotification(listMessage);
            }
            if (listMessage.size() >= NUMBER_NOTIFICATION) {
                this.recoveryNotification(false);
            }
        }
    }


    private void recoveryNotification(final boolean isFirst) {
        if (notificationHelper == null) {
            return;
        }

        Subscription subscription = notificationHelper.getOldestTimeNotification()
                .observeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Long>() {
                    @Override
                    public void onNext(Long time) {
                        if (isFirst) {
                            if (time == 0) {
                                sendMessageRecovery(0);
                            }
                        } else {
                            sendMessageRecovery(time);
                        }
                    }
                });

        mCompositeSubscription.add(subscription);
    }


    private void sendMessageRecovery(long timeStamp) {
        if (mWsConnection != null) {
            NotificationApiMessage message = NotificationApiHelper.createMessageRecovery(NUMBER_NOTIFICATION, timeStamp);
            mWsConnection.send(message.messageCode, message.messageContent);
        }
    }

    private void subscribeTopics(String token) throws IOException {
        Timber.d("subscribeTopics mIsSubscribeGcm [%s] token [%s]", mIsSubscribeGcm, token);
        if (mIsSubscribeGcm) {
            return;
        }
        GcmHelper.subscribeTopics(mContext, token);
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
        if (notificationHelper == null) {
            return;
        }

        notificationHelper.closeNotificationSystem();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNotificationUpdated(NotificationChangeEvent event) {
        Timber.d("on Notification updated %s", event.isRead());
        if (notificationHelper == null) {
            return;
        }

        if (!event.isRead()) {
            notificationHelper.showNotificationSystem();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.BACKGROUND)
    public void onTokenGcmRefresh(TokenGCMRefreshEvent event) {
        Timber.d("on Token GCM Refresh event %s", event);
        TokenGCMRefreshEvent stickyEvent = mEventBus.getStickyEvent(TokenGCMRefreshEvent.class);
        // Better check that an event was actually posted before
        if (stickyEvent != null) {
            // "Consume" the sticky event
            mEventBus.removeStickyEvent(stickyEvent);
            mIsSubscribeGcm = false;
            mWsConnection.disconnect();
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
