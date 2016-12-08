package vn.com.vng.zalopay.ui.presenter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.app.ApplicationState;
import vn.com.vng.zalopay.data.api.entity.UserExistEntity;
import vn.com.vng.zalopay.data.appresources.AppResourceStore;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.PassportRepository;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.event.AlertNotificationEvent;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.event.PaymentDataEvent;
import vn.com.vng.zalopay.event.RefreshPaymentSdkEvent;
import vn.com.vng.zalopay.event.RefreshPlatformInfoEvent;
import vn.com.vng.zalopay.exception.PaymentWrapperException;
import vn.com.vng.zalopay.internal.di.components.ApplicationComponent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.notification.ZPNotificationService;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.service.AbsPWResponseListener;
import vn.com.vng.zalopay.service.GlobalEventHandlingService;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.service.PaymentWrapperBuilder;
import vn.com.vng.zalopay.service.UserSession;
import vn.com.vng.zalopay.ui.view.IHomeView;
import vn.com.vng.zalopay.utils.AppVersionUtils;
import vn.com.vng.zalopay.utils.PermissionUtil;
import vn.com.vng.zalopay.utils.RootUtils;
import vn.com.vng.zalopay.zpsdk.DefaultZPGatewayInfoCallBack;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.controller.WalletSDKApplication;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.merchant.CShareData;
import vn.com.zalopay.wallet.view.dialog.DialogManager;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * Created by AnhHieu on 5/24/16.
 * *
 */
public class MainPresenter extends BaseUserPresenter implements IPresenter<IHomeView> {

    private IHomeView mHomeView;

    private boolean isLoadedGateWayInfo;

    private PaymentWrapper paymentWrapper;

    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

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

    @Inject
    NotificationStore.Repository mNotifyRepository;

    @Inject
    ZPNotificationService notificationService;

    @Inject
    UserSession mUserSession;

    @Inject
    ApplicationState mApplicationState;

    @Inject
    GlobalEventHandlingService globalEventHandlingService;

    @Inject
    MainPresenter(User user, EventBus eventBus,
                  AppResourceStore.Repository appResourceRepository,
                  Context applicationContext,
                  Navigator navigator,
                  PassportRepository passportRepository,
                  BalanceStore.Repository balanceRepository,
                  ZaloPayRepository zaloPayRepository,
                  TransactionStore.Repository transactionRepository,
                  FriendStore.Repository friendRepository) {
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
        Observable<Boolean> observableZFriendList = retrieveZaloFriendsAsNeeded();
        Observable<Boolean> observableMergeWithZp = checkListZaloIdForClient();

        Subscription subscription = observableZFriendList.concatWith(observableMergeWithZp)
                .doOnTerminate(new Action0() {
                    @Override
                    public void call() {
                        syncContact();
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>() {
                    @Override
                    public void onError(Throwable e) {
                        Timber.d(e, "Get zalo friend error");
                    }
                });

        mCompositeSubscription.add(subscription);
    }

    private Observable<Boolean> retrieveZaloFriendsAsNeeded() {
        return mFriendRepository.retrieveZaloFriendsAsNeeded()
                .delaySubscription(5, TimeUnit.SECONDS)
                .onErrorResumeNext(new Func1<Throwable, Observable<? extends Boolean>>() {
                    @Override
                    public Observable<? extends Boolean> call(Throwable throwable) {
                        Timber.d(throwable, "retrieve zalo friends exception");
                        return Observable.empty();
                    }
                });
    }

    private Observable<Boolean> checkListZaloIdForClient() {
        return mFriendRepository.checkListZaloIdForClient()
                .map(new Func1<List<UserExistEntity>, Boolean>() {
                    @Override
                    public Boolean call(List<UserExistEntity> entities) {
                        return Boolean.TRUE;
                    }
                })
                ;
    }

    private void syncContact() {
        boolean granted = PermissionUtil.verifyPermission(mApplicationContext, new String[]{Manifest.permission.READ_CONTACTS});
        if (granted) {
            Subscription subscription = mFriendRepository.syncContact()
                    .subscribeOn(Schedulers.io())
                    .subscribe(new DefaultSubscriber<Boolean>() {
                        @Override
                        public void onError(Throwable e) {
                            Timber.d(e, "Sync contact exception");
                        }
                    });
            mCompositeSubscription.add(subscription);
        }
    }

    @Override
    public void setView(IHomeView iHomeView) {
        this.mHomeView = iHomeView;
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
        mUserSession.beginSession();

        Timber.d("ApplicationState object [%s]", mApplicationState);
        mApplicationState.moveToState(ApplicationState.State.MAIN_SCREEN_CREATED);
    }

    @Override
    public void destroyView() {
        mEventBus.unregister(this);
        unsubscribeIfNotNull(mRefPlatformSubscription);
        unsubscribeIfNotNull(mCompositeSubscription);
        GlobalData.initApplication(null);
        notificationService.destroy();
        CShareData.dispose();
        this.mHomeView = null;
        mApplicationState.moveToState(ApplicationState.State.MAIN_SCREEN_DESTROYED);
    }

    @Override
    public void resume() {
        notificationService.startNotificationService();

        GlobalEventHandlingService.Message message = globalEventHandlingService.popMessage();
        if (message != null && mHomeView != null) {
            SweetAlertDialog alertDialog = new SweetAlertDialog(mHomeView.getContext(), message.messageType, R.style.alert_dialog);
            alertDialog.setConfirmText(message.title);
            alertDialog.setContentText(message.content);
            alertDialog.show();
        }

    }

    @Override
    public void pause() {
    }

    @Override
    public void destroy() {
        mUserSession.endSession();
    }

    public void initialize() {
        this.loadGatewayInfoPaymentSDK();
        ZPAnalytics.trackEvent(ZPEvents.APPLAUNCHHOME);
        initializeAppConfig();
        getZaloFriend();
        warningRoot();
    }

    private void warningRoot() {
        Subscription subscription = ObservableHelper.makeObservable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return !RootUtils.isDeviceRooted() || RootUtils.isHideWarningRooted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (!aBoolean && mHomeView != null) {
                            mNavigator.startWarningRootedActivity(mHomeView.getContext());
                        }
                    }
                });

        mCompositeSubscription.add(subscription);
    }

    private void initializeAppConfig() {
        Subscription subscription = mAppResourceRepository.initialize()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        mCompositeSubscription.add(subscription);
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
                AppVersionUtils.showDialogUpgradeAppIfNeed(mHomeView.getActivity());
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
        if (!event.isOnline) {
            return;
        }
        if (!isLoadedGateWayInfo) {
            loadGatewayInfoPaymentSDK();
        }
        initializeAppConfig();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onPayWithTransToken(final PaymentDataEvent event) {
        if (event.isConfirm) {
            showPayDialogConfirm(event);
        } else {
            pay(event.appId, event.zptranstoken, event.isAppToApp);
        }

        mEventBus.removeStickyEvent(PaymentDataEvent.class);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onRefreshPaymentSdk(RefreshPaymentSdkEvent event) {
        if (mHomeView == null) {
            return;
        }

        ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();
        paymentInfo.userInfo = new UserInfo();
        paymentInfo.userInfo.zaloPayUserId = mUser.zaloPayId;
        paymentInfo.userInfo.accessToken = mUser.accesstoken;
        WalletSDKApplication.refreshGatewayInfo(paymentInfo, new DefaultZPGatewayInfoCallBack());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiverAlertNotification(AlertNotificationEvent event) {

        if (mHomeView == null) {
            return;
        }

        final NotificationData notify = event.notify;
        if (notify.transid > 0) {
            SweetAlertDialog dialog = new SweetAlertDialog(mHomeView.getContext(), SweetAlertDialog.NORMAL_TYPE, R.style.alert_dialog);

            dialog.setTitleText(TextUtils.isEmpty(event.mTitle) ? mApplicationContext.getString(R.string.notification) : event.mTitle);
            dialog.setCancelText(mApplicationContext.getString(R.string.txt_close));
            dialog.setContentText(notify.message);
            dialog.setConfirmText(mApplicationContext.getString(R.string.view_detail));
            dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog dialog) {
                    if (mHomeView != null) {
                        mNavigator.startTransactionDetail(mHomeView.getContext(), String.valueOf(notify.transid));
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

        if (mEventBus.isRegistered(this)) {
            mEventBus.unregister(this);
        }
        ApplicationComponent applicationComponent = AndroidApplication.instance().getAppComponent();
        applicationComponent.applicationSession().clearUserSession();
    }

    public void pay(final long appId, String zptranstoken, final boolean isAppToApp) {
        showLoadingView();
        if (paymentWrapper == null) {
            paymentWrapper = getPaymentWrapper(appId, isAppToApp);
        }
        paymentWrapper.payWithToken(mHomeView.getActivity(), appId, zptranstoken);
    }

    private void showLoadingView() {
        if (mHomeView != null) {
            mHomeView.showLoading();
        }
    }

    private void hideLoadingView() {
        if (mHomeView != null) {
            mHomeView.hideLoading();
        }
    }

    private void showErrorView(String mess) {
        if (mHomeView != null) {
            mHomeView.showError(mess);
        }
    }

    private void responseToApp(Activity activity, long appId, int returnCode, String returnMessage) {
       /* // TODO: 12/1/16 kiem tra truong hop user khong du tien thanh toan
        String responseFormat = "zp-redirect-%s://result?returncode=%s&returnmessage=%s";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse(String.format(Locale.getDefault(), responseFormat, appId, returnCode, returnMessage)));
        activity.startActivity(intent);*/

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
                .setResponseListener(new AbsPWResponseListener(mHomeView.getActivity()) {
                    @Override
                    public void onError(PaymentWrapperException exception) {
                        if (mHomeView == null) {
                            return;
                        }

                        hideLoadingView();
                        showErrorView(exception.getMessage());

                        if (isAppToApp) {
                            responseToApp(mHomeView.getActivity(), appId, exception.getErrorCode(), exception.getMessage());
                        }
                    }

                    @Override
                    public void onCompleted() {
                        if (mHomeView == null) {
                            return;
                        }

                        hideLoadingView();
                        if (isAppToApp) {
                            responseToApp(mHomeView.getActivity(), appId, PaymentError.ERR_CODE_SUCCESS.value(), "");
                        }
                    }
                }).build();
    }

    private void showPayDialogConfirm(final PaymentDataEvent dataEvent) {
        if (mHomeView == null) {
            return;
        }

        DialogManager.showSweetDialogConfirm(mHomeView.getActivity(), mApplicationContext.getString(R.string.lbl_confirm_pay_order),
                mApplicationContext.getString(R.string.accept), mApplicationContext.getString(R.string.cancel),
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
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>() {
                    @Override
                    public void onError(Throwable e) {
                        Timber.d(e, "onError");
                    }
                });
        mCompositeSubscription.add(subscription);
    }
}
