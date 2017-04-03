package vn.com.vng.zalopay.ui.presenter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.zalopay.ui.widget.dialog.SweetAlertDialog;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;
import com.zalopay.ui.widget.util.TimeUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.app.ApplicationState;
import vn.com.vng.zalopay.data.appresources.AppResourceStore;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.eventbus.ChangeBalanceEvent;
import vn.com.vng.zalopay.data.eventbus.DownloadZaloPayResourceEvent;
import vn.com.vng.zalopay.data.eventbus.NotificationChangeEvent;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.util.BusComponent;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.PassportRepository;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.event.AlertNotificationEvent;
import vn.com.vng.zalopay.event.LoadIconFontEvent;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.event.PaymentDataEvent;
import vn.com.vng.zalopay.event.RefreshPaymentSdkEvent;
import vn.com.vng.zalopay.event.RefreshPlatformInfoEvent;
import vn.com.vng.zalopay.exception.PaymentWrapperException;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.location.LocationProvider;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.service.AbsPWResponseListener;
import vn.com.vng.zalopay.service.GlobalEventHandlingService;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.service.PaymentWrapperBuilder;
import vn.com.vng.zalopay.service.UserSession;
import vn.com.vng.zalopay.ui.view.IHomeView;
import vn.com.vng.zalopay.ui.view.ILoadDataView;
import vn.com.vng.zalopay.utils.AppVersionUtils;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.vng.zalopay.utils.ConfigUtil;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.vng.zalopay.utils.PermissionUtil;
import vn.com.vng.zalopay.utils.RootUtils;
import vn.com.vng.zalopay.zpsdk.DefaultZPGatewayInfoCallBack;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.controller.SDKApplication;

import static vn.com.vng.zalopay.data.util.BusComponent.APP_SUBJECT;

/**
 * Created by longlv on 3/21/17.
 * Logic class of HomeActivity.
 */

public class HomePresenter extends AbstractPresenter<IHomeView> {
    private boolean isLoadedGateWayInfo;

    private PaymentWrapper paymentWrapper;

    private EventBus mEventBus;
    private AppResourceStore.Repository mAppResourceRepository;
    private Context mApplicationContext;
    private Navigator mNavigator;
    private PassportRepository passportRepository;
    private BalanceStore.Repository mBalanceRepository;
    private ZaloPayRepository mZaloPayRepository;
    private TransactionStore.Repository mTransactionRepository;
    private User mUser;
    private FriendStore.Repository mFriendRepository;
    private Subscription mRefPlatformSubscription;

    @Inject
    NotificationStore.Repository mNotifyRepository;

    @Inject
    UserSession mUserSession;

    @Inject
    ApplicationState mApplicationState;

    @Inject
    GlobalEventHandlingService globalEventHandlingService;

    private boolean isInitTransaction;

    @Inject
    HomePresenter(User user, EventBus eventBus,
                  AppResourceStore.Repository appResourceRepository,
                  Context applicationContext,
                  Navigator navigator,
                  PassportRepository passportRepository,
                  BalanceStore.Repository balanceRepository,
                  ZaloPayRepository zaloPayRepository,
                  TransactionStore.Repository transactionRepository,
                  FriendStore.Repository friendRepository,
                  NotificationStore.Repository notificationRepository) {
        this.mEventBus = eventBus;
        this.mAppResourceRepository = appResourceRepository;
        this.mApplicationContext = applicationContext;
        this.mNavigator = navigator;
        this.passportRepository = passportRepository;
        this.mBalanceRepository = balanceRepository;
        this.mZaloPayRepository = zaloPayRepository;
        this.mTransactionRepository = transactionRepository;
        this.mFriendRepository = friendRepository;
        this.mUser = user;
    }

    private void getZaloFriend() {
        Subscription subscription = retrieveZaloFriendsAsNeeded()
                .doOnTerminate(this::syncContact)
                .doOnError(Timber::d)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());

        mSubscription.add(subscription);
    }

    private Observable<Boolean> retrieveZaloFriendsAsNeeded() {
        return mFriendRepository.retrieveZaloFriendsAsNeeded()
                .doOnError(Timber::d)
                .delaySubscription(5, TimeUnit.SECONDS)
                .onErrorResumeNext(Observable.empty());
    }

    private void syncContact() {
        boolean granted = PermissionUtil.verifyPermission(mApplicationContext, new String[]{Manifest.permission.READ_CONTACTS});
        if (granted) {
            Subscription subscription = mFriendRepository.syncContact()
                    .doOnError(Timber::d)
                    .subscribeOn(Schedulers.io())
                    .subscribe(new DefaultSubscriber<>());
            mSubscription.add(subscription);
        }
    }

    @Override
    public void attachView(IHomeView iHomeView) {
        super.attachView(iHomeView);
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
        subscibeBusComponent();
        mUserSession.beginSession();
        Timber.d("ApplicationState object [%s]", mApplicationState);
        mApplicationState.moveToState(ApplicationState.State.MAIN_SCREEN_CREATED);
    }

    @Override
    public void detachView() {
        mEventBus.unregister(this);
        unsubscribeIfNotNull(mRefPlatformSubscription);
        CShareDataWrapper.dispose();
        mApplicationState.moveToState(ApplicationState.State.MAIN_SCREEN_DESTROYED);
        unregisterBusComponent();
        super.detachView();
    }

    @Override
    public void resume() {
        mUserSession.ensureNotifyConnect();
        GlobalEventHandlingService.Message message = globalEventHandlingService.popMessage();
        if (mView != null) {
            if (message != null) {
                SweetAlertDialog alertDialog = new SweetAlertDialog(mView.getContext(), message.messageType, R.style.alert_dialog);
                alertDialog.setConfirmText(message.title);
                alertDialog.setContentText(message.content);
                alertDialog.show();
            }
            mView.setBalance(mBalanceRepository.currentBalance());
            getBalance();
        }

    }

    @Override
    public void destroy() {
        super.destroy();
    }

    public void subscibeBusComponent() {
        BusComponent.subscribe(APP_SUBJECT, this, new HomePresenter.ComponentSubscriber(), AndroidSchedulers.mainThread());
    }

    public void unregisterBusComponent() {
        BusComponent.unregister(this);
    }

    public void initialize() {
        UserComponent userComponent = AndroidApplication.instance().getUserComponent();
        if (userComponent != null) {
            SDKApplication.getBuilder().setRetrofit(userComponent.retrofitConnector());
        }

        loadGatewayInfoPaymentSDK();
        ZPAnalytics.trackEvent(ZPEvents.APPLAUNCHHOME);
        LocationProvider.findLocation();
        ensureAppResourceAvailable();
        getZaloFriend();
        warningRoot();
        getTransaction(10);
    }

    private void warningRoot() {
        Subscription subscription = ObservableHelper.makeObservable(
                () -> !RootUtils.isDeviceRooted() || RootUtils.isHideWarningRooted())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {
                    if (!aBoolean && mView != null) {
                        mNavigator.startWarningRootedActivity(mView.getContext());
                    }
                });

        mSubscription.add(subscription);
    }

    private void ensureAppResourceAvailable() {
        Subscription subscription = mAppResourceRepository.ensureAppResourceAvailable()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        mSubscription.add(subscription);
    }

    private void refreshBanners() {
        isLoadedGateWayInfo = true;
        mEventBus.post(new RefreshPlatformInfoEvent());
    }

    private void loadGatewayInfoPaymentSDK() {
        final ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();
        UserInfo userInfo = new UserInfo();
        userInfo.zaloUserId = String.valueOf(mUser.zaloId);
        userInfo.zaloPayUserId = mUser.zaloPayId;
        userInfo.accessToken = mUser.accesstoken;
        paymentInfo.userInfo = userInfo;
        SDKApplication.loadGatewayInfo(paymentInfo, new DefaultZPGatewayInfoCallBack() {
            @Override
            public void onFinish() {
                Timber.d("load payment sdk finish");
                if (mView == null) {
                    return;
                }

                isLoadedGateWayInfo = true;

                refreshBanners();
                beginAutoRefreshPlatform();
            }

            @Override
            public void onUpVersion(boolean forceUpdate, String latestVersion, String msg) {
                Timber.d("onUpVersion latestVersion [%s] msg [%s]", latestVersion, msg);
                if (mView == null) {
                    return;
                }

                isLoadedGateWayInfo = true;

                if (!forceUpdate) {
                    beginAutoRefreshPlatform();
                }

                refreshBanners();
                AppVersionUtils.handleEventUpdateVersion(mView.getActivity(),
                        forceUpdate, latestVersion, msg);
            }
        });
    }

    private void unsubscribeIfNotNull(Subscription subscription) {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    private void beginAutoRefreshPlatform() {
        unsubscribeIfNotNull(mRefPlatformSubscription);

        mRefPlatformSubscription = Observable.just(CShareDataWrapper.getPlatformInfoExpiredTime())
                .flatMap(new Func1<Long, Observable<Long>>() {
                    @Override
                    public Observable<Long> call(Long var) {
                        long interval = Math.max(var, 300000); // 5 min
                        return Observable.interval(interval, TimeUnit.MILLISECONDS);
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe(aLong -> {
                    Timber.d("call refresh platform info");
                    loadGatewayInfoPaymentSDK();
                    mEventBus.post(new RefreshPlatformInfoEvent());
                });
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNetworkChange(NetworkChangeEvent event) {
        if (!event.isOnline) {
            return;
        }
        if (!isInitTransaction) {
            this.getTransaction(0);
        }

        if (!isLoadedGateWayInfo) {
            loadGatewayInfoPaymentSDK();
        }
        getBalance();
        ensureAppResourceAvailable();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onPayWithTransToken(final PaymentDataEvent event) {
        long currentTime = System.currentTimeMillis();

        if (currentTime - event.timeRequest > 4 * TimeUtils.MINUTE) {
            Timber.d("pay oder expired time");
            return;
        }

        if (event.isConfirm) {
            showPayDialogConfirm(event);
        } else {
            pay(event.appId, event.zptranstoken, event.isAppToApp);
        }

        mEventBus.removeStickyEvent(PaymentDataEvent.class);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onRefreshPaymentSdk(RefreshPaymentSdkEvent event) {
        if (mView == null) {
            return;
        }

        ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();
        paymentInfo.userInfo = new UserInfo();
        paymentInfo.userInfo.zaloPayUserId = mUser.zaloPayId;
        paymentInfo.userInfo.accessToken = mUser.accesstoken;
        SDKApplication.refreshGatewayInfo(paymentInfo, new DefaultZPGatewayInfoCallBack());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiverAlertNotification(AlertNotificationEvent event) {

        if (mView == null) {
            return;
        }

        final NotificationData notify = event.notify;
        if (notify.transid > 0) {
            SweetAlertDialog dialog = new SweetAlertDialog(mView.getContext(), SweetAlertDialog.NORMAL_TYPE, R.style.alert_dialog);

            dialog.setTitleText(TextUtils.isEmpty(event.mTitle) ? mApplicationContext.getString(R.string.notification) : event.mTitle);
            dialog.setCancelText(mApplicationContext.getString(R.string.txt_close));
            dialog.setContentText(notify.message);
            dialog.setConfirmText(mApplicationContext.getString(R.string.view_detail));
            dialog.setConfirmClickListener(dialog1 -> {
                if (mView != null) {
                    mNavigator.startTransactionDetail(mView.getContext(), String.valueOf(notify.transid));
                }
                dialog1.dismiss();
            });
            dialog.show();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onLoadIconFontSuccess(LoadIconFontEvent event) {
        mEventBus.removeStickyEvent(LoadIconFontEvent.class);
        if (mView != null) {
            mView.refreshIconFont();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.BACKGROUND)
    public void onDownloadResourceSuccessEvent(DownloadZaloPayResourceEvent event) {
        mEventBus.removeStickyEvent(DownloadZaloPayResourceEvent.class);
        if (event.isDownloadSuccess) {
            reloadConfig();
            reloadIconFont();
        }
    }

    private void reloadConfig() {
        boolean result = ConfigUtil.loadConfigFromResource();
        if (result) {
            Timber.d("Load config from resource app 1 successfully.");
        }
    }

    private void reloadIconFont() {
        AndroidApplication.instance().initIconFont();
    }

    public void pay(final long appId, String zptranstoken, final boolean isAppToApp) {
        showLoadingView();
        if (paymentWrapper == null) {
            paymentWrapper = getPaymentWrapper(appId, isAppToApp);
        }
        paymentWrapper.payWithToken(mView.getActivity(), appId, zptranstoken);
    }

    private void showLoadingView() {
        if (mView != null) {
            mView.showLoading();
        }
    }

    private void hideLoadingView() {
        if (mView != null) {
            mView.hideLoading();
        }
    }

    private void showErrorView(String mess) {
        if (mView != null) {
            mView.showError(mess);
        }
    }

    private void showNetworkError() {
        if (mView != null) {
            mView.showNetworkErrorDialog();
        }
    }

    private void responseToApp(Activity activity, long appId, int returnCode, String returnMessage) {
        Intent data = new Intent();
        data.putExtra("returncode", returnCode);
        data.putExtra("returnMessage", returnMessage);
        activity.setResult(Activity.RESULT_OK, data);
        activity.finish();
    }

    private PaymentWrapper getPaymentWrapper(final long appId, final boolean isAppToApp) {
        return new PaymentWrapperBuilder()
                .setBalanceRepository(mBalanceRepository)
                .setZaloPayRepository(mZaloPayRepository)
                .setTransactionRepository(mTransactionRepository)
                .setResponseListener(new AbsPWResponseListener(mView.getActivity()) {
                    @Override
                    protected ILoadDataView getView() {
                        return mView;
                    }

                    @Override
                    public void onError(PaymentWrapperException exception) {
                        if (mView == null) {
                            return;
                        }

                        hideLoadingView();
                        if (exception.getErrorCode() == PaymentError.ERR_CODE_INTERNET.value()) {
                            showNetworkError();
                        } else {
                            showErrorView(exception.getMessage());
                        }

                        if (isAppToApp) {
                            responseToApp(mView.getActivity(), appId, exception.getErrorCode(), exception.getMessage());
                        }
                    }

                    @Override
                    public void onCompleted() {
                        if (mView == null) {
                            return;
                        }

                        hideLoadingView();
                        if (isAppToApp) {
                            responseToApp(mView.getActivity(), appId, PaymentError.ERR_CODE_SUCCESS.value(), "");
                        }
                    }
                }).build();
    }

    private void showPayDialogConfirm(final PaymentDataEvent dataEvent) {
        if (mView == null || mView.getActivity() == null) {
            return;
        }

        DialogHelper.showConfirmDialog(mView.getActivity(),
                mView.getActivity().getString(R.string.lbl_confirm_pay_order),
                mView.getActivity().getString(R.string.accept),
                mView.getActivity().getString(R.string.cancel),
                new ZPWOnEventConfirmDialogListener() {
                    @Override
                    public void onCancelEvent() {
                    }

                    @Override
                    public void onOKevent() {
                        pay(dataEvent.appId, dataEvent.zptranstoken, dataEvent.isAppToApp);
                        if (dataEvent.notification != null) {
                            removeNotification(dataEvent.notification);
                        }
                    }
                });
    }

    private void removeNotification(NotificationData notify) {
        Subscription subscription = mNotifyRepository.removeNotifyByType(notify.notificationtype, notify.appid, notify.transid)
                .doOnError(Timber::d)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        mSubscription.add(subscription);
    }

    private void getTransaction(long delay) {
        Subscription subscriptionSuccess = mTransactionRepository.fetchTransactionHistoryLatest()
                .delaySubscription(delay, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                        isInitTransaction = true;
                    }
                });
        mSubscription.add(subscriptionSuccess);
    }

    public void getBalance() {
        Subscription subscription = mBalanceRepository.balance()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HomePresenter.BalanceSubscriber());

        mSubscription.add(subscription);
    }

    // Temporary class for getting balance value on collapse menu
    private class BalanceSubscriber extends DefaultSubscriber<Long> {
        @Override
        public void onNext(Long aLong) {
            HomePresenter.this.onGetBalanceSuccess(aLong);
        }
    }

    private void onGetBalanceSuccess(Long balance) {
        Timber.d("Get balance success : balance [%s]", balance);
        mView.setBalance(balance);
    }

    private class ComponentSubscriber extends DefaultSubscriber<Object> {
        @Override
        public void onNext(Object event) {
            if (event instanceof ChangeBalanceEvent) {
                if (mView != null) {
                    mView.setBalance(((ChangeBalanceEvent) event).balance);
                }
            } else if (event instanceof NotificationChangeEvent) {
                if (!((NotificationChangeEvent) event).isRead()) {
                    getTotalNotification(0);
                }
            }
        }
    }

    public void getTotalNotification(long delay) {
        Subscription subscription = mNotifyRepository.totalNotificationUnRead()
                .delaySubscription(delay, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HomePresenter.NotificationSubscriber());
        mSubscription.add(subscription);
    }

    private final class NotificationSubscriber extends DefaultSubscriber<Integer> {
        @Override
        public void onNext(Integer integer) {
            Timber.d("Got total %s unread notification messages", integer);
            if (mView != null) {
                mView.setTotalNotify(integer);
            }
        }
    }
}
