package vn.com.vng.zalopay.ui.presenter;

import android.app.Activity;
import android.text.TextUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.model.ZaloFriend;
import vn.com.vng.zalopay.event.DonateMoneyEvent;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.event.PaymentDataEvent;
import vn.com.vng.zalopay.internal.di.components.ApplicationComponent;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.ui.view.IHomeView;
import vn.com.vng.zalopay.utils.AppVersionUtils;
import vn.com.zalopay.wallet.application.WalletSDKApplication;
import vn.com.zalopay.wallet.data.GlobalData;
import vn.com.zalopay.wallet.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.entity.user.UserInfo;
import vn.com.zalopay.wallet.listener.ZPWGatewayInfoCallback;
import vn.com.zalopay.wallet.merchant.CShareData;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * Created by AnhHieu on 5/24/16.
 *
 */
public class MainPresenter extends BaseUserPresenter implements IPresenter<IHomeView> {

    IHomeView homeView;

    private boolean isLoadedGateWayInfo;

    PaymentWrapper paymentWrapper;

    CompositeSubscription compositeSubscription = new CompositeSubscription();

    @Inject
    public MainPresenter() {
    }

    private void getZaloFriend() {
        FriendStore.Repository friendRepository = AndroidApplication.instance().getUserComponent().friendRepository();
        Subscription subscription = friendRepository.retrieveZaloFriendsAsNeeded()
                .delaySubscription(5, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<List<ZaloFriend>>());
        compositeSubscription.add(subscription);
    }

    @Override
    public void setView(IHomeView iHomeView) {
        this.homeView = iHomeView;
        if (!eventBus.isRegistered(this)) {
            eventBus.register(this);
        }
    }

    @Override
    public void destroyView() {
        eventBus.unregister(this);
        unsubscribeIfNotNull(compositeSubscription);
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
        this.initializeAppConfig();
        this.getZaloFriend();
    }

    private void initializeAppConfig() {
        Subscription subscription = mAppResourceRepository.initialize()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        compositeSubscription.add(subscription);
    }

    private void loadGatewayInfoPaymentSDK() {
        User user = userConfig.getCurrentUser();
        final ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();
        UserInfo userInfo = new UserInfo();
        userInfo.zaloUserId = String.valueOf(user.zaloId);
        userInfo.zaloPayUserId = user.zaloPayId;
        userInfo.accessToken = user.accesstoken;
        paymentInfo.userInfo = userInfo;
        WalletSDKApplication.loadGatewayInfo(homeView.getActivity(), paymentInfo, new ZPWGatewayInfoCallback() {
            @Override
            public void onFinish() {
                Timber.d("load payment sdk finish");
                isLoadedGateWayInfo = true;
                if (homeView != null) {
                    homeView.refreshBanners();
                }
            }

            @Override
            public void onProcessing() {
            }

            @Override
            public void onError(String pMessage) {
                Timber.d("load payment sdk error: %s", TextUtils.isEmpty(pMessage) ? "" : pMessage);
            }

            @Override
            public void onUpVersion(String latestVersion, String msg) {
                boolean upgradeApp = AppVersionUtils.needUpgradeApp(latestVersion, msg);
                if (!upgradeApp || homeView == null) {
                    return;
                }
                AppVersionUtils.showUpgradeAppDialog(homeView.getActivity());
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
        pay(event.appId, event.zptranstoken);
        eventBus.removeStickyEvent(PaymentDataEvent.class);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onReceiverDonateMoney(DonateMoneyEvent event) {

        eventBus.removeStickyEvent(DonateMoneyEvent.class);

        if (homeView == null) {
            return;
        }

        final NotificationData notify = event.notify;
        if (notify.transid > 0) {
            SweetAlertDialog dialog = new SweetAlertDialog(homeView.getContext(), SweetAlertDialog.NORMAL_TYPE, R.style.alert_dialog);

            dialog.setTitleText("Tặng tiền");
            dialog.setCancelText(applicationContext.getString(R.string.txt_close));
            dialog.setConfirmText(notify.message);
            dialog.setConfirmText(applicationContext.getString(R.string.view_detail));
            dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog dialog) {
                    if (homeView != null) {
                        navigator.startTransactionDetail(homeView.getContext(), String.valueOf(notify.transid));
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
        compositeSubscription.add(subscription);

        ApplicationComponent applicationComponent = AndroidApplication.instance().getAppComponent();
        applicationComponent.applicationSession().clearUserSession();
    }


    public void pay(long appId, String zptranstoken) {
        showLoadingView();
        if (paymentWrapper == null) {
            paymentWrapper = new PaymentWrapper(balanceRepository, zaloPayRepository, transactionRepository, new PaymentWrapper.IViewListener() {
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
                        homeView.showError(applicationContext.getString(R.string.order_invalid));
                    } else if ("uid".equalsIgnoreCase(param)) {
                        homeView.showError(applicationContext.getString(R.string.user_invalid));
                    } else if ("token".equalsIgnoreCase(param)) {
                        homeView.showError(applicationContext.getString(R.string.order_invalid));
                    }

                    hideLoadingView();
                }

                @Override
                public void onPreComplete(boolean isSuccessful, String transId) {

                }

                @Override
                public void onResponseError(PaymentError paymentError) {
                    Timber.d("onResponseError");
                    if (homeView == null) {
                        return;
                    }

                    if (paymentError == PaymentError.ERR_CODE_INTERNET) {
                        homeView.showError(applicationContext.getString(R.string.exception_no_connection_try_again));
                    }

                    hideLoadingView();
                }

                @Override
                public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
                    Timber.d("onResponseSuccess");
                    hideLoadingView();

                   /* if (homeView != null && homeView.getActivity() != null) {
                        homeView.getActivity().finish();
                    }*/
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
                    if (homeView.getContext() != null) {
                        homeView.showError(homeView.getContext().getString(R.string.exception_generic));
                    }
                    hideLoadingView();
                }

                @Override
                public void onNotEnoughMoney() {

                    Timber.d("onNotEnoughMoney");

                    if (homeView == null) {
                        return;
                    }
                    hideLoadingView();
                    navigator.startDepositActivity(applicationContext);

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
}
