package vn.com.vng.zalopay.ui.presenter;

import android.os.CountDownTimer;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import com.zalopay.apploader.internal.ModuleName;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.appresources.AppResourceStore;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.eventbus.ChangeBalanceEvent;
import vn.com.vng.zalopay.data.eventbus.NotificationChangeEvent;
import vn.com.vng.zalopay.data.eventbus.ReadNotifyEvent;
import vn.com.vng.zalopay.data.eventbus.WsConnectionEvent;
import vn.com.vng.zalopay.data.merchant.MerchantStore;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.domain.model.MerchantUserInfo;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.event.RefreshPlatformInfoEvent;
import vn.com.vng.zalopay.event.SignOutEvent;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.paymentapps.PaymentAppConfig;
import vn.com.vng.zalopay.paymentapps.PaymentAppTypeEnum;
import vn.com.vng.zalopay.ui.view.IZaloPayView;
import vn.com.vng.zalopay.webview.entity.WebViewPayInfo;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBanner;
import vn.com.zalopay.wallet.merchant.CShareData;

import static vn.com.vng.zalopay.data.util.Lists.isEmptyOrNull;

/**
 * Created by AnhHieu on 5/9/16.
 * *
 */
public class ZaloPayPresenterImpl extends AbstractPresenter<IZaloPayView> implements ZaloPayPresenter<IZaloPayView> {
    private final int BANNER_COUNT_DOWN_INTERVAL = 3000;
    private int BANNER_MILLIS_IN_FUTURE = 60 * 60 * 1000; //Finish countDownTimer after 1h (60*60*1000)

    private final MerchantStore.Repository mMerchantRepository;
    private EventBus mEventBus;
    private BalanceStore.Repository mBalanceRepository;
    private AppResourceStore.Repository mAppResourceRepository;
    private NotificationStore.Repository mNotificationRepository;
    private Navigator mNavigator;

    //Banner variable
    private CountDownTimer mBannerCountDownTimer;
    //avoid case: new & release CountDownTimer continuously
    private Handler mBannerHandle = new Handler();
    private Runnable mBannerRunnable = new Runnable() {
        @Override
        public void run() {
            startBannerCountDownTimer();
        }
    };

    public ZaloPayPresenterImpl(MerchantStore.Repository mMerchantRepository,
                                EventBus eventBus,
                                BalanceStore.Repository balanceRepository,
                                AppResourceStore.Repository appResourceRepository,
                                NotificationStore.Repository notificationRepository,
                                Navigator navigator) {
        this.mMerchantRepository = mMerchantRepository;
        this.mEventBus = eventBus;
        this.mBalanceRepository = balanceRepository;
        this.mAppResourceRepository = appResourceRepository;
        this.mNotificationRepository = notificationRepository;
        this.mNavigator = navigator;
    }

    @Override
    public void attachView(IZaloPayView o) {
        super.attachView(o);
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    @Override
    public void detachView() {
        mEventBus.unregister(this);
        super.detachView();
    }

    @Override
    public void resume() {
        startBannerCountDownTimer();
        if (NetworkHelper.isNetworkAvailable(mView.getContext())) {
            mView.hideNetworkError();
        }
    }

    @Override
    public void pause() {
        stopBannerCountDownTimer();
    }

    @Override
    public void destroy() {
        super.destroy();
        mBannerCountDownTimer = null;
        mBannerHandle = null;
        mBannerRunnable = null;
    }

    @Override
    public void initialize() {

        this.getListAppResource();
        this.getTotalNotification(2000);
        this.getBanners();
        this.getBalance();

    }

    @Override
    public void getBalance() {
        Subscription subscription = mBalanceRepository.balance()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BalanceSubscriber());

        mSubscription.add(subscription);
    }

    private void getListAppResource() {
        Subscription subscription = mAppResourceRepository.listInsideAppResource()
                .doOnNext(new Action1<List<AppResource>>() {
                    @Override
                    public void call(List<AppResource> appResources) {
                        getListMerchantUser(appResources);
                    }
                })
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new AppResourceSubscriber());
        mSubscription.add(subscription);
    }

    @Override
    public void startPaymentApp(AppResource app) {
        Subscription subscription = mAppResourceRepository.existResource(app.appid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new StartPaymentAppSubscriber(app));
        mSubscription.add(subscription);
    }

    @Override
    public void handleLaunchApp(AppResource app) {
        Timber.d("onclick app %s %s %s ", app.appType, app.appid, app.appname);
        if (app.appType == PaymentAppTypeEnum.NATIVE.getValue()) {
            if (app.appid == PaymentAppConfig.Constants.TRANSFER_MONEY) {
                mNavigator.startTransferMoneyActivity(mView.getActivity());
            } else if (app.appid == PaymentAppConfig.Constants.RED_PACKET) {
                mNavigator.startMiniAppActivity(mView.getActivity(), ModuleName.RED_PACKET);
            } else if (app.appid == PaymentAppConfig.Constants.RECEIVE_MONEY) {
                mNavigator.startReceiveMoneyActivity(mView.getContext());
            } else {
                AppResource appResource = PaymentAppConfig.getAppResource(app.appid);
                if (appResource == null) {
                    appResource = new AppResource(app.appid);
                }
                startPaymentApp(appResource);
            }
        } else if (app.appType == PaymentAppTypeEnum.WEBVIEW.getValue()) {
            startServiceWebViewActivity(app.appid, app.webUrl);
        }
    }

    // because of delay, subscriber at startup is sometime got triggered after the immediate subscriber
    // when received notification
    private void getTotalNotification(long delay) {
        Subscription subscription = mNotificationRepository.totalNotificationUnRead()
                .delaySubscription(delay, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new NotificationSubscriber());
        mSubscription.add(subscription);
    }

    private void getListMerchantUser(List<AppResource> listAppResource) {
        Timber.d("getListMerchantUser: [%s]", listAppResource.size());
        if (isEmptyOrNull(listAppResource)) {
            return;
        }

        List<Long> listId = toStringListAppId(listAppResource);

        Subscription subscription = mMerchantRepository.getListMerchantUserInfo(listId)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        mSubscription.add(subscription);
    }

    private List<Long> toStringListAppId(List<AppResource> listAppResource) {
        List<Long> listId = new ArrayList<>();

        for (AppResource appResource : listAppResource) {
            if (appResource == null) {
                continue;
            }

            listId.add(appResource.appid);
        }
        return listId;
    }

    private int numberCallAppResource;

    private void onGetAppResourceSuccess(List<AppResource> resources) {
        numberCallAppResource++;
        Timber.d("get app resource call : " + numberCallAppResource);

        boolean isEnableShowShow = resources.contains(
                PaymentAppConfig.getAppResource(PaymentAppConfig.Constants.SHOW_SHOW));
        mView.enableShowShow(isEnableShowShow);

        resources.removeAll(PaymentAppConfig.EXCLUDE_APP_RESOURCE_LIST);

        List<AppResource> listApps = new ArrayList<>(PaymentAppConfig.APP_RESOURCE_LIST);

        if (!Lists.isEmptyOrNull(resources)) {
            listApps.addAll(resources);
        }

        mView.refreshInsideApps(listApps);
    }

    private class AppResourceSubscriber extends DefaultSubscriber<List<AppResource>> {

        @Override
        public void onNext(List<AppResource> appResources) {
            ZaloPayPresenterImpl.this.onGetAppResourceSuccess(appResources);
            Timber.d(" AppResource %s", appResources.size());
        }
    }

    private class BalanceSubscriber extends DefaultSubscriber<Long> {
        @Override
        public void onNext(Long aLong) {
            ZaloPayPresenterImpl.this.onGetBalanceSuccess(aLong);
        }
    }

    private void onGetBalanceSuccess(Long balance) {
        Timber.d("onGetBalanceSuccess %s", balance);
        mView.setBalance(balance);
    }

    @Override
    public void startBannerCountDownTimer() {
        if (mBannerCountDownTimer != null) {
            return;
        }
        mBannerCountDownTimer = new CountDownTimer(BANNER_MILLIS_IN_FUTURE, BANNER_COUNT_DOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
//                Timber.d("onTick currentTime [%s]", System.currentTimeMillis());
                if (mView == null) {
                    return;
                }
                mView.changeBanner();
            }

            @Override
            public void onFinish() {
                mBannerCountDownTimer = null;
                startBannerCountDownTimer();
            }
        };
        mBannerCountDownTimer.start();

    }

    @Override
    public void stopBannerCountDownTimer() {
        if (mBannerCountDownTimer == null) {
            return;
        }
        mBannerCountDownTimer.cancel();
        mBannerCountDownTimer = null;
    }

    @Override
    public void onTouchBanner(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            stopBannerCountDownTimer();
            if (mBannerHandle != null) {
                mBannerHandle.removeCallbacks(mBannerRunnable);
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mBannerHandle != null) {
                mBannerHandle.postDelayed(mBannerRunnable, BANNER_COUNT_DOWN_INTERVAL);
            }
        }
    }


    int numberRefreshBanner;

    public void getBanners() {
        try {
            List<DBanner> banners = CShareData.getInstance().getBannerList();
            if (banners != null && banners.size() > 1) {
                startBannerCountDownTimer();
            } else {
                stopBannerCountDownTimer();
            }
            Timber.d("getBanners: %s", numberRefreshBanner++);
            mView.showBannerAds(banners);
        } catch (Exception e) {
            Timber.w(e, "Get banners exception");
        }
    }

    @Override
    public void startServiceWebViewActivity(long appId, String webViewUrl) {
        Subscription subscription = mMerchantRepository.getMerchantUserInfo(appId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MerchantUserInfoSubscribe(appId, webViewUrl));
        mSubscription.add(subscription);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWsConnectionChanged(WsConnectionEvent event) {
        if (event.isConnect) {
            mView.hideNetworkError();
        } else {
            mView.showWsConnectError();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetworkChange(NetworkChangeEvent event) {
        if (mView == null) {
            return;
        }
        if (!event.isOnline) {
            mView.showNetworkError();
            return;
        }
        getBalance();
        mView.hideNetworkError();

    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNotificationUpdated(NotificationChangeEvent event) {
        Timber.d("on Notification updated state %s", event.isRead());
        if (!event.isRead()) {
            getTotalNotification(0);
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onReadNotify(ReadNotifyEvent event) {
        Timber.d("onReadNotify");
        getTotalNotification(0);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBalanceChangeEvent(ChangeBalanceEvent event) {
        if (mView != null) {
            mView.setBalance(event.balance);
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onSignOutEvent(SignOutEvent event) {
        mEventBus.unregister(this);
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

    private class MerchantUserInfoSubscribe extends DefaultSubscriber<MerchantUserInfo> {
        private long mAppId;
        private String mWebViewUrl;

        private MerchantUserInfoSubscribe(long appId, String webViewUrl) {
            this.mAppId = appId;
            this.mWebViewUrl = webViewUrl;
        }

        @Override
        public void onNext(MerchantUserInfo merchantUserInfo) {
            Timber.d("onNext merchantInfo [%s]", merchantUserInfo);
            if (merchantUserInfo == null) {
                mView.showError("MerchantUserInfo invalid");
                return;
            }
            WebViewPayInfo gamePayInfo = new WebViewPayInfo();
            gamePayInfo.setUid(merchantUserInfo.muid);
            gamePayInfo.setAccessToken(merchantUserInfo.maccesstoken);
            gamePayInfo.setAppId(mAppId);
            mNavigator.startServiceWebViewActivity(mView.getContext(), gamePayInfo, mWebViewUrl);
        }

        @Override
        public void onError(Throwable e) {
            Timber.d(e, "onError exception");
            if (ResponseHelper.shouldIgnoreError(e)) {
                return;
            }
            mView.showErrorDialog(ErrorMessageFactory.create(mView.getContext(), e));
        }
    }


    private class StartPaymentAppSubscriber extends DefaultSubscriber<Boolean> {
        private AppResource app;

        StartPaymentAppSubscriber(AppResource app) {
            this.app = app;
        }

        @Override
        public void onNext(Boolean result) {
            if (mView == null) {
                return;
            }

            if (result) {
                mNavigator.startPaymentApplicationActivity(mView.getContext(), app);
            } else {
                mView.showErrorDialog(mView.getContext().getString(R.string.application_downloading));
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshPlatformInfoEvent(RefreshPlatformInfoEvent e) {
        Timber.d("onRefreshPlatformInfoEvent");
        this.getListAppResource();
        this.getBanners();
    }
}
