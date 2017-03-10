package vn.com.vng.zalopay.service;

import android.content.Context;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.NoSubscriberEvent;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.eventbus.NewSessionEvent;
import vn.com.vng.zalopay.data.ws.connection.NotificationService;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.event.NetworkChangeEvent;

/**
 * Created by hieuvm on 11/23/16.
 */

public class UserSession {

    public static long mLastTimeCheckPassword = 0;
    public static String mHashPassword;

    private Context mContext;

    private User mUser;
    private EventBus mEventBus;
    private UserConfig mUserConfig;

    private static Boolean userInitialized = false;
    private NotificationService mNotifyService;
    private BalanceStore.Repository mBalanceRepository;

    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    public UserSession(Context context, User user,
                       UserConfig mUserConfig,
                       EventBus eventBus,
                       NotificationService notifyService,
                       BalanceStore.Repository balanceRepository

    ) {

        this.mContext = context;
        this.mUser = user;
        this.mEventBus = eventBus;
        this.mUserConfig = mUserConfig;
        this.mNotifyService = notifyService;
        this.mBalanceRepository = balanceRepository;
    }

    public void beginSession() {
        Timber.d("beginSession: userInitialized [%s] ", userInitialized);
        if (userInitialized) {
            return;
        }

        userInitialized = true;

        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
        sendCrashUserInformation(mUser);
        mNotifyService.start();

        Subscription subscription = mBalanceRepository.balanceLocal()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Long>());
        mCompositeSubscription.add(subscription);
    }

    public void endSession() {
        Timber.d("endSession");
        mEventBus.unregister(this);
        mLastTimeCheckPassword = 0;
        mHashPassword = null;
        mNotifyService.stop();
        mCompositeSubscription.clear();
        userInitialized = false;
    }

    private void sendCrashUserInformation(User user) {
        if (user == null) {
            return;
        }
        Crashlytics.setUserIdentifier(user.zaloPayId);
        if (!TextUtils.isEmpty(user.email)) {
            Crashlytics.setUserEmail(user.email);
        }
        if (!TextUtils.isEmpty(user.zalopayname)) {
            Crashlytics.setUserName(user.zalopayname);
        }
    }

    public void ensureNotifyConnect() {
        mNotifyService.start();
    }

    public void ensureUserInitialized() {
        Timber.d("ensureUserInitialized");
        beginSession();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNetworkChanged(NetworkChangeEvent event) {
        if (!event.isOnline) {
            return;
        }

        mNotifyService.start();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onSessionChanged(NewSessionEvent event) {
        Timber.d("onSessionChange from server [%s]", event.newSession);
        if (TextUtils.isEmpty(event.newSession)) {
            return;
        }

        if (!mUser.accesstoken.equals(event.newSession)) {
            Timber.d("Update accesstoken old: [%s]", mUser.accesstoken);
            mUser.accesstoken = event.newSession;
            mUserConfig.setAccessToken(event.newSession);
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNoSubscriber(NoSubscriberEvent event) {
        Timber.d("onNoSubscriber: %s", event.originalEvent);
    }
}
