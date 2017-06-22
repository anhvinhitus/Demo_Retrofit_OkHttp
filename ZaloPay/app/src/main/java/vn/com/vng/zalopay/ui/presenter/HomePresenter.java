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
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.app.ApplicationState;
import vn.com.vng.zalopay.data.appresources.AppResourceStore;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.event.AlertNotificationEvent;
import vn.com.vng.zalopay.event.LoadIconFontEvent;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.event.PaymentDataEvent;
import vn.com.vng.zalopay.event.PreferentialNotificationEvent;
import vn.com.vng.zalopay.event.RefreshPaymentSdkEvent;
import vn.com.vng.zalopay.event.RefreshPlatformInfoEvent;
import vn.com.vng.zalopay.exception.PaymentWrapperException;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.location.LocationProvider;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.promotion.PromotionHelper;
import vn.com.vng.zalopay.pw.AbsPWResponseListener;
import vn.com.vng.zalopay.pw.PaymentWrapper;
import vn.com.vng.zalopay.pw.PaymentWrapperBuilder;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.service.GlobalEventHandlingService;
import vn.com.vng.zalopay.service.UserSession;
import vn.com.vng.zalopay.ui.view.IHomeView;
import vn.com.vng.zalopay.ui.view.ILoadDataView;
import vn.com.vng.zalopay.utils.AppVersionUtils;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.vng.zalopay.utils.PermissionUtil;
import vn.com.vng.zalopay.utils.RootUtils;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.analytics.ZPPaymentSteps;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.interactor.PlatformInfoCallback;
import vn.com.zalopay.wallet.interactor.UpversionCallback;
import vn.zalopay.promotion.CashBackRender;
import vn.zalopay.promotion.IBuilder;
import vn.zalopay.promotion.IInteractPromotion;
import vn.zalopay.promotion.IResourceLoader;
import vn.zalopay.promotion.PromotionEvent;
import vn.zalopay.promotion.PromotionType;

/**
 * Created by longlv on 3/21/17.
 * Logic class of HomeActivity.
 */

public class HomePresenter extends AbstractPresenter<IHomeView> {

    private final EventBus mEventBus;
    private final AppResourceStore.Repository mAppResourceRepository;
    private final Context mApplicationContext;
    private final Navigator mNavigator;
    private final BalanceStore.Repository mBalanceRepository;
    private final ZaloPayRepository mZaloPayRepository;
    private final TransactionStore.Repository mTransactionRepository;
    private final User mUser;
    private final FriendStore.Repository mFriendRepository;

    private final NotificationStore.Repository mNotifyRepository;
    private final UserSession mUserSession;
    private final ApplicationState mApplicationState;
    private final GlobalEventHandlingService globalEventHandlingService;
    private boolean isInitTransaction;
    private Subscription mRefPlatformSubscription;
    private boolean isLoadedGateWayInfo;
    private DefaultSubscriber<PlatformInfoCallback> platformInfoSubscriber = new DefaultSubscriber<PlatformInfoCallback>() {
        @Override
        public void onError(Throwable e) {
            Timber.d("load platform info on error %s", e);
        }

        @Override
        public void onNext(PlatformInfoCallback platformInfoCallback) {
            Timber.d("load payment sdk finish");
            if (mView == null) {
                return;
            }
            if (platformInfoCallback instanceof UpversionCallback) {
                updateHomePage(((UpversionCallback) platformInfoCallback).forceupdate);
                AppVersionUtils.handleEventUpdateVersion(mView.getActivity(),
                        ((UpversionCallback) platformInfoCallback).forceupdate, ((UpversionCallback) platformInfoCallback).newestappversion,
                        ((UpversionCallback) platformInfoCallback).forceupdatemessage);
            } else {
                updateHomePage(false);
            }
        }
    };
    private PaymentWrapper paymentWrapper;
    private IBuilder mPromotionBuilder;
    private PromotionHelper mPromotionHelper;
    private IResourceLoader mPromotionResourceLoader;

    @Inject
    HomePresenter(User user, EventBus eventBus,
                  AppResourceStore.Repository appResourceRepository,
                  Context applicationContext,
                  Navigator navigator,
                  BalanceStore.Repository balanceRepository,
                  ZaloPayRepository zaloPayRepository,
                  TransactionStore.Repository transactionRepository,
                  FriendStore.Repository friendRepository,
                  NotificationStore.Repository notifyRepository,
                  UserSession userSession,
                  ApplicationState applicationState,
                  GlobalEventHandlingService globalEventHandlingService,
                  IResourceLoader resourceLoader,
                  PromotionHelper promotionHelper) {
        this.mEventBus = eventBus;
        this.mAppResourceRepository = appResourceRepository;
        this.mApplicationContext = applicationContext;
        this.mNavigator = navigator;
        this.mBalanceRepository = balanceRepository;
        this.mZaloPayRepository = zaloPayRepository;
        this.mTransactionRepository = transactionRepository;
        this.mFriendRepository = friendRepository;
        this.mUser = user;
        this.mNotifyRepository = notifyRepository;
        this.mUserSession = userSession;
        this.mApplicationState = applicationState;
        this.globalEventHandlingService = globalEventHandlingService;
        this.mPromotionResourceLoader = resourceLoader;
        this.mPromotionHelper = promotionHelper;
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
        mUserSession.beginSession();
        Timber.d("ApplicationState object [%s]", mApplicationState);
        mApplicationState.moveToState(ApplicationState.State.MAIN_SCREEN_CREATED);
    }

    @Override
    public void detachView() {
        if (mEventBus.isRegistered(this)) {
            mEventBus.unregister(this);
        }
        unsubscribeIfNotNull(mRefPlatformSubscription);
        CShareDataWrapper.dispose();
        mApplicationState.moveToState(ApplicationState.State.MAIN_SCREEN_DESTROYED);
        super.detachView();
    }

    @Override
    public void resume() {
        mUserSession.ensureNotifyConnect();
        GlobalEventHandlingService.Message message = globalEventHandlingService.popMessage();
        if (message != null && mView != null) {
            SweetAlertDialog alertDialog = new SweetAlertDialog(mView.getContext(), message.messageType, R.style.alert_dialog);
            alertDialog.setConfirmText(message.title);
            alertDialog.setContentText(message.content);
            alertDialog.show();
        }

    }

    @Override
    public void destroy() {
        super.destroy();
    }

    public void initialize() {
        UserComponent userComponent = AndroidApplication.instance().getUserComponent();
        if (userComponent != null) {
            SDKApplication.getBuilder().setRetrofit(userComponent.retrofitConnector());
        }

        loadGatewayInfoPaymentSDK();
        ZPAnalytics.trackEvent(ZPEvents.APPLAUNCHHOME);
        LocationProvider.findLocation();
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

    private void refreshBanners() {
        isLoadedGateWayInfo = true;
        mEventBus.post(new RefreshPlatformInfoEvent());
    }

    private void loadGatewayInfoPaymentSDK() {
        UserInfo userInfo = new UserInfo();
        userInfo.zalo_userid = String.valueOf(mUser.zaloId);
        userInfo.zalopay_userid = mUser.zaloPayId;
        userInfo.accesstoken = mUser.accesstoken;
        String appVersion = BuildConfig.VERSION_NAME;
        Subscription[] subscriptions = SDKApplication.loadSDKData(userInfo, appVersion, platformInfoSubscriber);
        if (subscriptions != null && subscriptions.length > 0) {
            for (int i = 0; i < subscriptions.length; i++) {
                mSubscription.add(subscriptions[i]);
            }
        }
    }

    private void updateHomePage(boolean forceUpdate) {
        isLoadedGateWayInfo = true;
        refreshBanners();
        if (!forceUpdate) {
            beginAutoRefreshPlatform();
        }
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

        UserInfo userInfo = new UserInfo();
        userInfo.zalopay_userid = mUser.zaloPayId;
        userInfo.accesstoken = mUser.accesstoken;
        String appVersion = BuildConfig.VERSION_NAME;
        Subscription subscription = SDKApplication.refreshSDKData(userInfo, appVersion, new DefaultSubscriber());
        mSubscription.add(subscription);
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
                    mNavigator.startTransactionDetail(mView.getContext(), String.valueOf(notify.transid), String.valueOf(notify.notificationId));
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

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onCashBackEvent(PromotionEvent event) {
        mEventBus.removeStickyEvent(PromotionEvent.class);
        if (mPromotionBuilder != null) {
            mPromotionBuilder.setPromotion(event);
            return;
        }
        if (mView != null && event != null) {
            switch (event.type) {
                case PromotionType.CASHBACK:
                    mPromotionBuilder = CashBackRender.getBuilder()
                            .setPromotion(event)
                            .setResourceProvider(mPromotionResourceLoader)
                            .setInteractPromotion(new IInteractPromotion() {

                                @Override
                                public void onUserInteract(PromotionEvent pPromotionEvent) {
                                    mPromotionHelper.navigate(mView.getActivity(), pPromotionEvent);
                                }

                                @Override
                                public void onClose() {
                                    mPromotionBuilder.release();
                                    mPromotionBuilder = null;
                                }
                            });
                    mView.showCashBackView(mPromotionBuilder, event);
                    break;
                default:
                    Timber.d("undefine promotion type");
            }
        }
    }

    public void pay(final long appId, String zptranstoken, final boolean isAppToApp) {
        showLoadingView();
        if (paymentWrapper == null) {
            paymentWrapper = getPaymentWrapper(appId, isAppToApp);
        }
        paymentWrapper.payWithToken(mView.getActivity(), appId, zptranstoken, isAppToApp ? ZPPaymentSteps.OrderSource_AppToApp : ZPPaymentSteps.OrderSource_NotifyInApp);
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
        PaymentWrapper wrapper = new PaymentWrapperBuilder()
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
        wrapper.initializeComponents();
        return wrapper;
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceivePreferentialNotification(PreferentialNotificationEvent event) {
        Timber.d("onReceivePreferentialNotification: ");
        if (mView != null) {
            mView.showBadgePreferential();
        }
    }
}
