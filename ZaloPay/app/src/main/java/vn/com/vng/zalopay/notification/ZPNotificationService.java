package vn.com.vng.zalopay.notification;

import android.content.Context;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.data.ServerErrorMessage;
import vn.com.vng.zalopay.data.eventbus.NotificationChangeEvent;
import vn.com.vng.zalopay.data.eventbus.ReadNotifyEvent;
import vn.com.vng.zalopay.data.eventbus.ThrowToLoginScreenEvent;
import vn.com.vng.zalopay.data.exception.AccountSuspendedException;
import vn.com.vng.zalopay.data.exception.ServerMaintainException;
import vn.com.vng.zalopay.data.exception.TokenException;
import vn.com.vng.zalopay.data.ws.connection.Connection;
import vn.com.vng.zalopay.data.ws.connection.NotificationApiHelper;
import vn.com.vng.zalopay.data.ws.connection.NotificationApiMessage;
import vn.com.vng.zalopay.data.ws.connection.NotificationService;
import vn.com.vng.zalopay.data.ws.model.AuthenticationData;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.data.ws.model.RecoveryPushMessage;
import vn.com.vng.zalopay.domain.executor.ThreadExecutor;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.event.TokenGCMRefreshEvent;
import vn.com.vng.zalopay.network.NetworkHelper;
import vn.com.vng.zalopay.network.OnReceivedPushMessageListener;
import vn.com.vng.zalopay.network.PushMessage;

public class ZPNotificationService implements OnReceivedPushMessageListener, NotificationService {

    /*Server API Key: AIzaSyCweupE81mBm3_m8VOoFTUbuhBF82r_GwI
    Sender ID: 386726389536*/

    private boolean mIsSubscribeGcm = false;

    private Connection mWsConnection;

    private Context mContext;

    private EventBus mEventBus;

    private NotificationHelper mNotificationHelper;

    private ThreadExecutor mExecutor;

    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    private static final int NUMBER_NOTIFICATION = 30;

    private long mLastTimeRecovery;

    public ZPNotificationService(Context context, User user, NotificationHelper notificationHelper,
                                 ThreadExecutor executor, Gson gson, EventBus eventbus,
                                 Connection wsConnection) {
        this.mContext = context;
        this.mNotificationHelper = notificationHelper;
        this.mExecutor = executor;
        this.mEventBus = eventbus;
        this.mWsConnection = wsConnection;
        Timber.d("ZPNotificationService: %s", this);
    }

    public void start() {
        Timber.d("Start notification service");
        mWsConnection.addReceiverListener(this);
        registerEvent();
        mExecutor.execute(this::connectToServer);
    }

    public void stop() {
        Timber.d("Destroy notification service");
        mIsSubscribeGcm = false;

        if (mCompositeSubscription != null) {
            mCompositeSubscription.clear();
        }

        unregisterEvent();

        mWsConnection.disconnect();
        mWsConnection.removeReceiverListener(this);
        mWsConnection.cleanup();
    }

    public void ensureConnected() {
        if (mExecutor != null) {
            mExecutor.execute(this::connectToServer);
        }
    }


    private void registerEvent() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
        //    BusComponent.subscribe(APP_SUBJECT, this, new ComponentSubscriber(), AndroidSchedulers.mainThread());
    }

    private void unregisterEvent() {
        mEventBus.unregister(this);
        //  BusComponent.unregister(this);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mNotificationHelper = null;
        Timber.d("Finalize ZPNotificationService");
    }

    private void connectToServer() {
        String token = null;
        try {
            token = GcmHelper.getTokenGcm(mContext);
            subscribeTopics(token);
        } catch (Exception ex) {
            Timber.d(ex, "exception in working with GCM");
        }

        this.connect(token);
    }

    private void connect(String token) {
        Timber.d("connect with token %s", token);
        if (!NetworkHelper.isNetworkAvailable(mContext)) {
            Timber.d("Skip create connection, since OS reports no network connection");
            return;
        }

        if (!mWsConnection.isConnected()) {
            Timber.d("Socket is not connected. About to create connection.");
            mWsConnection.setGCMToken(token);
            mWsConnection.connect();
        } else {
            Timber.d("Socket is already connected. Do nothing.");
        }
    }

    @Override
    public void onReceivedPushMessage(PushMessage pushMessage) {
        Timber.d("onReceived PushMessage : [mtuid: %s] [msgType: %s]", pushMessage.mtuid, pushMessage.msgType);
        if (pushMessage instanceof AuthenticationData) {
            AuthenticationData authenticationData = (AuthenticationData) pushMessage;
            if (authenticationData.result != ServerErrorMessage.SUCCESSFUL) {
                handlerAuthenticationError(authenticationData);
            } else {
                Timber.d("Socket authentication succeeded");
                recoveryAfterAuthenticate();
            }
        } else if (pushMessage instanceof NotificationData) {
            if (mNotificationHelper == null) {
                return;
            }

            mNotificationHelper.processImmediateNotification((NotificationData) pushMessage);
        } else if (pushMessage instanceof RecoveryPushMessage) {
            final List<NotificationData> listMessage = ((RecoveryPushMessage) pushMessage).listNotify;

            Timber.d("Receive notification : listMessage size %s", listMessage.size());
            if (mNotificationHelper == null) {
                return;
            }

            if (mTimeoutRecoverySubscription != null) {
                mTimeoutRecoverySubscription.unsubscribe();
            }

            //Cần recoveryNotification xong để set lasttime recovery xong,
            // mới tiếp tục sendmessage recovery.
            Subscription sub = mNotificationHelper.recoveryNotification(listMessage)
                    .filter(aVoid -> listMessage.size() >= NUMBER_NOTIFICATION)
                    .flatMap(aVoid -> mNotificationHelper.getOldestTimeRecoveryNotification(false))
                    .doOnError(Timber::d)
                    .subscribeOn(Schedulers.io())
                    .subscribe(new RecoverySubscriber(listMessage.size() < NUMBER_NOTIFICATION));
            mCompositeSubscription.add(sub);
        }
    }

    private class RecoverySubscriber extends DefaultSubscriber<Long> {

        boolean recoveryData;
        boolean isSendMessRecovery;

        RecoverySubscriber(boolean recoveryData) {
            this.recoveryData = recoveryData;
        }


        @Override
        public void onNext(Long time) {
            isSendMessRecovery = sendMessageRecovery(time);
        }


        @Override
        public void onCompleted() {
            if (recoveryData || !isSendMessRecovery) {
                recoveryData();
            }
        }
    }


    @Override
    public void onError(Throwable t) {
        //empty
    }

    private Subscription mTimeoutRecoverySubscription;

    private void recoveryAfterAuthenticate() {
        if (mNotificationHelper == null) {
            return;
        }

        Subscription subscription = mNotificationHelper.getOldestTimeRecoveryNotification(true)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Long>() {
                    @Override
                    public void onNext(Long time) {
                        sendMessageRecovery(time);
                    }
                });

        mCompositeSubscription.add(subscription);
    }

    private Subscription startTimeoutRecoveryNotification() {
        return Observable.timer(5, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Long>() {
                    @Override
                    public void onCompleted() {
                        recoveryData();
                    }
                });
    }

    private void recoveryData() {
        Timber.d("Begin recovery data");

        if (mNotificationHelper == null) {
            return;
        }

        mNotificationHelper.recoveryTransaction();
        mNotificationHelper.recoveryRedPacketStatus();
    }

    private static final long TIMESTAMP_DEFAULT_RECOVERY = 1;

    private boolean sendMessageRecovery(Long time) {
        long timeStamp;
        if (time == null || time == 0) {
            timeStamp = TIMESTAMP_DEFAULT_RECOVERY;
        } else {
            timeStamp = time;
        }

        Timber.d("Send message recovery timeStamp [%s]", timeStamp);
        if (mLastTimeRecovery > TIMESTAMP_DEFAULT_RECOVERY && mLastTimeRecovery <= timeStamp) {
            Timber.d("ignore recovery with mLastTimeRecovery [%s]", mLastTimeRecovery);
            return false;
        }

        mLastTimeRecovery = timeStamp;

        if (!mWsConnection.isConnected()) {
            Timber.d("ignore recovery because socket not connected");
            return false;
        }

        mTimeoutRecoverySubscription = startTimeoutRecoveryNotification();

        NotificationApiMessage message = NotificationApiHelper.createMessageRecovery(NUMBER_NOTIFICATION, timeStamp);
        mWsConnection.send(message.messageCode, message.messageContent);

        return true;
    }

    private void subscribeTopics(String token) throws IOException {
        if (mIsSubscribeGcm) {
            return;
        }
        GcmHelper.subscribeTopics(mContext, token);
        mIsSubscribeGcm = true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReadNotify(ReadNotifyEvent event) {
        if (mNotificationHelper != null) {
            mNotificationHelper.closeNotificationSystem();
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNotificationUpdated(NotificationChangeEvent event) {
        Timber.d("notification updated : isRead %s", event.isRead());

        if (event.isRead()) {
            return;
        }

        if (mNotificationHelper != null) {
            mNotificationHelper.showNotificationSystem();
        }
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.BACKGROUND)
    public void onTokenGcmRefresh(TokenGCMRefreshEvent event) {
        TokenGCMRefreshEvent stickyEvent = mEventBus.getStickyEvent(TokenGCMRefreshEvent.class);
        // Better check that an event was actually posted before
        if (stickyEvent != null) {
            // "Consume" the sticky event
            mEventBus.removeStickyEvent(stickyEvent);
            mIsSubscribeGcm = false;

            if (mWsConnection != null) {
                mWsConnection.disconnect();
            }
            start();
        }
    }

    private void handlerAuthenticationError(AuthenticationData authentication) {
        Timber.d("Handler authentication error: authentication code [%s]", authentication.code);
        if (authentication.code == ServerErrorMessage.UM_TOKEN_NOT_FOUND ||
                authentication.code == ServerErrorMessage.UM_TOKEN_EXPIRE ||
                authentication.code == ServerErrorMessage.TOKEN_INVALID) {
            // session expired
            Timber.d("Session is expired");
            TokenException exception = new TokenException(authentication.code);
            mEventBus.post(new ThrowToLoginScreenEvent(exception));
        } else if (authentication.code == ServerErrorMessage.SERVER_MAINTAIN) {
            Timber.d("Server maintain");
            ServerMaintainException exception = new ServerMaintainException(authentication.code, "");
            mEventBus.post(new ThrowToLoginScreenEvent(exception));
        } else if (authentication.code == ServerErrorMessage.ZPW_ACCOUNT_SUSPENDED
                || authentication.code == ServerErrorMessage.USER_IS_LOCKED) {
            Timber.d("Account is locked");
            AccountSuspendedException exception = new AccountSuspendedException(authentication.code, "");
            mEventBus.post(new ThrowToLoginScreenEvent(exception));
        }
    }

    @Override
    public void addReceiverListener(OnReceivedPushMessageListener listener) {
        mWsConnection.addReceiverListener(listener);
    }

    @Override
    public void send(NotificationApiMessage message) {
        mWsConnection.send(message);
    }
}
