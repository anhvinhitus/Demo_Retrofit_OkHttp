package vn.com.vng.zalopay.ui.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.appresources.AppResourceStore;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.domain.executor.ThreadExecutor;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.PassportRepository;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.event.AlertNotificationEvent;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.event.PaymentDataEvent;
import vn.com.vng.zalopay.event.RefreshPaymentSdkEvent;
import vn.com.vng.zalopay.event.RefreshPlatformInfoEvent;
import vn.com.vng.zalopay.internal.di.components.ApplicationComponent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.ui.view.IHomeView;
import vn.com.vng.zalopay.utils.AppVersionUtils;
import vn.com.vng.zalopay.zpsdk.DefaultZPGatewayInfoCallBack;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.business.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.controller.WalletSDKApplication;
import vn.com.zalopay.wallet.merchant.CShareData;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * Created by AnhHieu on 5/24/16.
 * *
 */
public class MainPresenter extends BaseUserPresenter implements IPresenter<IHomeView> {

    private IHomeView homeView;

    private boolean isLoadedGateWayInfo;

    private PaymentWrapper paymentWrapper;

    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    private EventBus mEventBus;
    private AppResourceStore.Repository mAppResourceRepository;
    private UserConfig mUserConfig;
    private Context mApplicationContext;
    private Navigator mNavigator;
    private PassportRepository passportRepository;
    private BalanceStore.Repository mBalanceRepository;
    private ZaloPayRepository mZaloPayRepository;
    private TransactionStore.Repository mTransactionRepository;

    private User mUser;
    private ThreadExecutor mThreadExecutor;
    private FriendStore.Repository mFriendRepository;

    @Inject
    public MainPresenter(User user, EventBus eventBus,
                         AppResourceStore.Repository appResourceRepository,
                         UserConfig userConfig,
                         Context applicationContext,
                         Navigator navigator,
                         PassportRepository passportRepository,
                         BalanceStore.Repository balanceRepository,
                         ZaloPayRepository zaloPayRepository,
                         TransactionStore.Repository transactionRepository,
                         FriendStore.Repository friendRepository,
                         ThreadExecutor threadExecutor) {

        this.mEventBus = eventBus;
        this.mAppResourceRepository = appResourceRepository;
        this.mUserConfig = userConfig;
        this.mApplicationContext = applicationContext;
        this.mNavigator = navigator;
        this.passportRepository = passportRepository;
        this.mBalanceRepository = balanceRepository;
        this.mZaloPayRepository = zaloPayRepository;
        this.mTransactionRepository = transactionRepository;
        this.mFriendRepository = friendRepository;
        this.mThreadExecutor = threadExecutor;
        this.mUser = user;
    }

    private void getZaloFriend() {
        Subscription subscription = mFriendRepository.retrieveZaloFriendsAsNeeded()
                .delaySubscription(5, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        mCompositeSubscription.add(subscription);
    }

    @Override
    public void setView(IHomeView iHomeView) {
        this.homeView = iHomeView;
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }


    private void sendCrashUserInformation(User user) {
        if (user == null) {
            return;
        }

        // TODO: Use the current user's information
        // You can call any combination of these three methods
        Crashlytics.setUserIdentifier(user.zaloPayId);
        if (!TextUtils.isEmpty(user.email)) {
            Crashlytics.setUserEmail(user.email);
        }
        if (!TextUtils.isEmpty(user.zalopayname)) {
            Crashlytics.setUserName(user.zalopayname);
        }

    }


    @Override
    public void destroyView() {
        mEventBus.unregister(this);
        unsubscribeIfNotNull(mRefPlatformSubscription);
        unsubscribeIfNotNull(mCompositeSubscription);
        GlobalData.initApplication(null);
        this.homeView = null;
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void destroy() {
        CShareData.dispose();
    }

    public void initialize() {
        this.loadGatewayInfoPaymentSDK();
        ZPAnalytics.trackEvent(ZPEvents.APPLAUNCHHOME);
        mThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                sendCrashUserInformation(mUser);
                initializeAppConfig();
                getZaloFriend();
                warningRoot();
            }
        });
    }

    private void warningRoot() {
       /* Subscription subscription = ObservableHelper.makeObservable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return !RootUtils.isDeviceRooted() || RootUtils.isHideWarningRooted();
            }
        }).subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                if (!aBoolean && homeView != null) {
                    mNavigator.startWarningRootedActivity(homeView.getContext());
                }
            }
        });

        mCompositeSubscription.add(subscription);*/
    }

    private void initializeAppConfig() {
        Subscription subscription = mAppResourceRepository.initialize()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        mCompositeSubscription.add(subscription);
    }

    private void refreshBanners() {
        mEventBus.post(new RefreshPlatformInfoEvent());
    }

    private void loadGatewayInfoPaymentSDK() {
        User user = mUserConfig.getCurrentUser();
        final ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();
        UserInfo userInfo = new UserInfo();
        userInfo.zaloUserId = String.valueOf(user.zaloId);
        userInfo.zaloPayUserId = user.zaloPayId;
        userInfo.accessToken = user.accesstoken;
        paymentInfo.userInfo = userInfo;
        WalletSDKApplication.loadGatewayInfo(paymentInfo, new DefaultZPGatewayInfoCallBack() {
            @Override
            public void onFinish() {
                Timber.d("load payment sdk finish");
                isLoadedGateWayInfo = true;

                refreshBanners();
                beginAutoRefreshPlatform();
            }

            @Override
            public void onUpVersion(boolean forceUpdate, String latestVersion, String msg) {
                Timber.d("onUpVersion latestVersion [%s] msg [%s]", latestVersion, msg);
                isLoadedGateWayInfo = true;

                if (!forceUpdate) {
                    beginAutoRefreshPlatform();
                }

                refreshBanners();
                AppVersionUtils.setVersionInfoInServer(forceUpdate, latestVersion, msg);
                AppVersionUtils.showDialogUpgradeAppIfNeed(homeView.getActivity());
            }
        });
    }


    private Subscription mRefPlatformSubscription;

    private void beginAutoRefreshPlatform() {
        unsubscribeIfNotNull(mRefPlatformSubscription);

        mRefPlatformSubscription = Observable.just(CShareData.getInstance().getPlatformInfoExpiredTime())
                .flatMap(new Func1<Long, Observable<Long>>() {
                    @Override
                    public Observable<Long> call(Long var) {
                        long interval = Math.max(var, 300000); // 5 min
                        return Observable.interval(interval, TimeUnit.MILLISECONDS);
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        Timber.d("call refresh platform info");
                        mEventBus.post(new RefreshPlatformInfoEvent());
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNetworkChange(NetworkChangeEvent event) {
        if (event.isOnline && !isLoadedGateWayInfo) {
            loadGatewayInfoPaymentSDK();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onPayWithTransToken(final PaymentDataEvent event) {
        pay(event.appId, event.zptranstoken, event.isAppToApp);
        mEventBus.removeStickyEvent(PaymentDataEvent.class);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onRefreshPaymentSdk(RefreshPaymentSdkEvent event) {
        if (homeView == null) {
            return;
        }

        if (mUserConfig.hasCurrentUser()) {
            ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();
            paymentInfo.userInfo.zaloPayUserId = mUserConfig.getCurrentUser().zaloPayId;
            paymentInfo.userInfo.accessToken = mUserConfig.getCurrentUser().accesstoken;

            WalletSDKApplication.refreshGatewayInfo(paymentInfo, new DefaultZPGatewayInfoCallBack());
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiverAlertNotification(AlertNotificationEvent event) {

        if (homeView == null) {
            return;
        }

        final NotificationData notify = event.notify;
        if (notify.transid > 0) {
            SweetAlertDialog dialog = new SweetAlertDialog(homeView.getContext(), SweetAlertDialog.NORMAL_TYPE, R.style.alert_dialog);

            dialog.setTitleText(TextUtils.isEmpty(event.mTitle) ? mApplicationContext.getString(R.string.notification) : event.mTitle);
            dialog.setCancelText(mApplicationContext.getString(R.string.txt_close));
            dialog.setContentText(notify.message);
            dialog.setConfirmText(mApplicationContext.getString(R.string.view_detail));
            dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog dialog) {
                    if (homeView != null) {
                        mNavigator.startTransactionDetail(homeView.getContext(), String.valueOf(notify.transid));
                    }
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    }

    public void logout() {
        Subscription subscription = passportRepository.logout()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>());
        mCompositeSubscription.add(subscription);

        ApplicationComponent applicationComponent = AndroidApplication.instance().getAppComponent();
        applicationComponent.applicationSession().clearUserSession();
    }


    public void pay(final long appId, String zptranstoken, final boolean isAppToApp) {
        showLoadingView();
        if (paymentWrapper == null) {
            paymentWrapper = new PaymentWrapper(mBalanceRepository, mZaloPayRepository, mTransactionRepository, new PaymentWrapper.IViewListener() {
                @Override
                public Activity getActivity() {
                    if (homeView != null) {
                        return homeView.getActivity();
                    }
                    return null;
                }
            }, new PaymentWrapper.IResponseListener() {
                @Override
                public void onParameterError(String param) {

                    Timber.d("onParameterError");

                    if (homeView == null) {
                        return;
                    }

                    if ("order".equalsIgnoreCase(param)) {
                        homeView.showError(mApplicationContext.getString(R.string.order_invalid));
                    } else if ("uid".equalsIgnoreCase(param)) {
                        homeView.showError(mApplicationContext.getString(R.string.user_invalid));
                    } else if ("token".equalsIgnoreCase(param)) {
                        homeView.showError(mApplicationContext.getString(R.string.order_invalid));
                    }

                    hideLoadingView();

                    if (isAppToApp && homeView != null) {
                        responseToApp(homeView.getActivity(), appId, -1, param);
                    }
                }

                @Override
                public void onPreComplete(boolean isSuccessful, String transId, String pAppTransId) {

                }

                @Override
                public void onResponseError(PaymentError paymentError) {
                    Timber.d("onResponseError");
                    if (homeView == null) {
                        return;
                    }

                    if (paymentError == PaymentError.ERR_CODE_INTERNET) {
                        homeView.showError(mApplicationContext.getString(R.string.exception_no_connection_try_again));
                    }

                    if (isAppToApp && homeView != null) {
                        responseToApp(homeView.getActivity(), appId, paymentError.value(), PaymentError.getErrorMessage(paymentError));
                    }

                    hideLoadingView();
                }

                @Override
                public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
                    Timber.d("onResponseSuccess");
                    hideLoadingView();

                    if (isAppToApp && homeView != null) {
                        responseToApp(homeView.getActivity(), appId, PaymentError.ERR_CODE_SUCCESS.value(),
                                PaymentError.getErrorMessage(PaymentError.ERR_CODE_SUCCESS));
                    }
                }

                @Override
                public void onResponseTokenInvalid() {
                    Timber.d("onResponseTokenInvalid");
                    if (homeView == null) {
                        return;
                    }

                    hideLoadingView();

                  /*  homeView.onTokenInvalid();
                    clearAndLogout();*/
                }

                @Override
                public void onAppError(String msg) {
                    Timber.d("onAppError msg [%s]", msg);
                    if (homeView == null) {
                        return;
                    }
                    homeView.showError(mApplicationContext.getString(R.string.exception_generic));
                    hideLoadingView();
                }

                @Override
                public void onNotEnoughMoney() {

                    Timber.d("onNotEnoughMoney");

                    if (homeView == null) {
                        return;
                    }
                    hideLoadingView();
                    mNavigator.startDepositActivity(mApplicationContext);

                }
            });
        }

        paymentWrapper.payWithToken(appId, zptranstoken);
    }

    private void showLoadingView() {
        if (homeView != null) {
            homeView.showLoading();
        }
    }

    private void hideLoadingView() {
        if (homeView != null) {
            homeView.hideLoading();
        }
    }

    private void responseToApp(Activity activity, long appId, int returnCode, String returnMessage) {
        String responseFormat = "zp-redirect-%s://result?returncode=%s&returnmessage=%s";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse(String.format(Locale.getDefault(), responseFormat, appId, returnCode, returnMessage)));
        activity.startActivity(intent);
    }

}
