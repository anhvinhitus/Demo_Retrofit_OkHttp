package vn.com.vng.zalopay.service;

import android.content.Context;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.NoSubscriberEvent;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.data.apptransidlog.ApptransidLogStore;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.eventbus.NewSessionEvent;
import vn.com.vng.zalopay.data.filelog.FileLogStore;
import vn.com.vng.zalopay.data.ws.connection.NotificationService;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.event.AppStateChangeEvent;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.event.UploadFileLogEvent;
import vn.com.vng.zalopay.network.NetworkHelper;
import vn.com.vng.zalopay.network.RetryFileLogUpload;
import vn.com.vng.zalopay.tracker.FileLogHelper;

/**
 * Created by hieuvm on 11/23/16.
 */

public class UserSession {

    private final Context mContext;
    private final User mUser;
    private final EventBus mEventBus;
    private final UserConfig mUserConfig;
    private final NotificationService mNotifyService;
    private final BalanceStore.Repository mBalanceRepository;
    private final CompositeSubscription mCompositeSubscription = new CompositeSubscription();
    private final FileLogStore.Repository mFileLogRepository;
    private final ApptransidLogStore.Repository mApptransidLogRepository;

    public static long mLastTimeCheckPassword = 0;
    public static String mHashPassword;
    private static Boolean userInitialized = false;

    public UserSession(Context context, User user,
                       UserConfig mUserConfig,
                       EventBus eventBus,
                       NotificationService notifyService,
                       BalanceStore.Repository balanceRepository,
                       FileLogStore.Repository fileLogRepository,
                       ApptransidLogStore.Repository apptransidLogRepository

    ) {

        this.mContext = context;
        this.mUser = user;
        this.mEventBus = eventBus;
        this.mUserConfig = mUserConfig;
        this.mNotifyService = notifyService;
        this.mBalanceRepository = balanceRepository;
        this.mFileLogRepository = fileLogRepository;
        this.mApptransidLogRepository = apptransidLogRepository;
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
                .subscribe(new DefaultSubscriber<>());
        mCompositeSubscription.add(subscription);

        uploadFileLogs();
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
        Timber.d("Ensure user initialized");
        beginSession();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNetworkChanged(NetworkChangeEvent event) {
        if (!event.isOnline) {
            return;
        }

        mNotifyService.start();

        Subscription subscription = mBalanceRepository.updateBalance()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        mCompositeSubscription.add(subscription);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onAppStateChanged(AppStateChangeEvent event) {
        Timber.d("onAppStateChanged: [isForeground: %s]", event.isForeground);

        if (!event.isForeground) {
            return;
        }

        mNotifyService.ensureConnected();
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

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onUploadFileLogEvent(UploadFileLogEvent event) {
        Timber.d("onUploadFileLogEvent : filePath [%s]", event.filePath);
        uploadFileLog(event.filePath);
        uploadApptransidFileLog();
    }

    private void uploadApptransidFileLog() {
        Subscription subscription = Observable.just(NetworkHelper.isNetworkAvailable(mContext))
                .filter(Boolean::booleanValue)
                .flatMap(aBoolean -> FileLogHelper.uploadApptransidFileLog(mFileLogRepository, mApptransidLogRepository))
                .retryWhen(new RetryFileLogUpload())
                .doOnError(Timber::w)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        mCompositeSubscription.add(subscription);
    }

    private void uploadFileLog(String filePath) {
        Subscription subscription = Observable.just(NetworkHelper.isNetworkAvailable(mContext))
                .filter(Boolean::booleanValue)
                .flatMap(aBoolean -> FileLogHelper.uploadFileLog(filePath, mFileLogRepository))
                .retryWhen(new RetryFileLogUpload())
                .doOnError(Timber::w)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        mCompositeSubscription.add(subscription);
    }


    private int numFileUpload;

    private void uploadFileLogs() {
        Subscription subscription = Observable.just(NetworkHelper.isNetworkAvailable(mContext))
                .filter(Boolean::booleanValue)
                .flatMap(aBoolean -> FileLogHelper.uploadFileLogs(mFileLogRepository))
                .delaySubscription(30, TimeUnit.SECONDS)
                .doOnError(Timber::w)
                .doOnTerminate(() -> Timber.d("Number file upload success [%s]", numFileUpload))
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        numFileUpload++;
                    }
                });
        mCompositeSubscription.add(subscription);
    }

}
