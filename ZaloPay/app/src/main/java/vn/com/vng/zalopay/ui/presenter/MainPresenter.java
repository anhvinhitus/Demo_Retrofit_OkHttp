package vn.com.vng.zalopay.ui.presenter;

import android.app.Activity;
import android.text.TextUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import okhttp3.OkHttpClient;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.model.ZaloFriend;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.internal.di.components.ApplicationComponent;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.ui.view.IHomeView;
import vn.com.zalopay.wallet.application.ZingMobilePayApplication;
import vn.com.zalopay.wallet.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.entity.user.UserInfo;
import vn.com.zalopay.wallet.listener.ZPWGatewayInfoCallback;

/**
 * Created by AnhHieu on 5/24/16.
 */
public class MainPresenter extends BaseUserPresenter implements IPresenter<IHomeView> {

    IHomeView homeView;

    FriendStore.Repository mFriendRepository;
    OkHttpClient mOkHttpClient;

    private boolean isLoadedGateWayInfo;

    public MainPresenter(FriendStore.Repository friendRepository, OkHttpClient okHttpClient) {
        this.mFriendRepository = friendRepository;
        this.mOkHttpClient = okHttpClient;
    }

    public void getZaloFriend() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                    }
                }
                if (homeView == null || homeView.getActivity() == null || mFriendRepository == null) {
                    return;
                }
                mFriendRepository.retrieveZaloFriendsAsNeeded().subscribe(new DefaultSubscriber<List<ZaloFriend>>());
            }
        }).start();
    }

    @Override
    public void setView(IHomeView iHomeView) {
        this.homeView = iHomeView;
    }

    @Override
    public void destroyView() {
        this.mFriendRepository = null;
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

    }

    public void initialize() {
        ZingMobilePayApplication.setHttpClient(mOkHttpClient.newBuilder());
        this.loadGatewayInfoPaymentSDK();
        this.initializeAppConfig();
    }

    private void initializeAppConfig() {
        mAppResourceRepository.initialize()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
    }

    private void loadGatewayInfoPaymentSDK() {
        User user = userConfig.getCurrentUser();
        ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();
        UserInfo userInfo = new UserInfo();
        userInfo.zaloUserId = String.valueOf(user.zaloId);
        userInfo.zaloPayUserId = user.uid;
        userInfo.accessToken = user.accesstoken;
        paymentInfo.userInfo = userInfo;
        ZingMobilePayApplication.loadGatewayInfo(homeView.getActivity(), paymentInfo, new ZPWGatewayInfoCallback() {
            @Override
            public void onFinish() {
                Timber.d("loadGatewayInfoPaymentSDK finish");
                isLoadedGateWayInfo = true;
            }

            @Override
            public void onProcessing() {
            }

            @Override
            public void onError(String pMessage) {

                if (!TextUtils.isEmpty(pMessage)) {
                    Timber.d("loadGatewayInfoPaymentSDK onError %s", pMessage);
                } else {
                    Timber.d("loadGatewayInfoPaymentSDK onError null");
                }
            }

            @Override
            public void onUpVersion(String s, String s1) {
                
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNetworkChange(NetworkChangeEvent event) {
        if (event.isOnline && !isLoadedGateWayInfo) {
            loadGatewayInfoPaymentSDK();
        }
    }


    public void logout() {
        passportRepository.logout()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>());
        ApplicationComponent applicationComponent = AndroidApplication.instance().getAppComponent();
        applicationComponent.applicationSession().clearUserSession();
    }

    PaymentWrapper paymentWrapper;

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
                public void onResponseCancel() {
                    Timber.d("onResponseCancel");
                    if (homeView == null) {
                        return;
                    }
                    hideLoadingView();
/*
                    hideLoadingView();
                    homeView.resumeScanner();*/
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

    protected void updateTransaction() {
        transactionRepository.updateTransaction()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>());
    }

    protected void updateBalance() {
        balanceRepository.updateBalance()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
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
