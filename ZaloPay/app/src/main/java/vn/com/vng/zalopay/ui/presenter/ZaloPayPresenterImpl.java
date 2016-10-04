package vn.com.vng.zalopay.ui.presenter;

import android.os.CountDownTimer;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;

import com.zalopay.apploader.impl.BundleServiceImpl;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.appresources.AppResourceStore;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.eventbus.ChangeBalanceEvent;
import vn.com.vng.zalopay.data.eventbus.NotificationChangeEvent;
import vn.com.vng.zalopay.data.eventbus.ReadNotifyEvent;
import vn.com.vng.zalopay.data.merchant.MerchantStore;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.data.util.ListStringUtil;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.domain.model.MerchantUserInfo;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.event.RefreshPlatformInfoEvent;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.paymentapps.PaymentAppConfig;
import vn.com.vng.zalopay.ui.view.IZaloPayView;
import vn.com.vng.zalopay.webview.entity.WebViewPayInfo;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBanner;
import vn.com.zalopay.wallet.merchant.CShareData;

/**
 * Created by AnhHieu on 5/9/16.
 * *
 */
public class ZaloPayPresenterImpl extends BaseUserPresenter implements ZaloPayPresenter<IZaloPayView> {
    private final int BANNER_COUNT_DOWN_INTERVAL = 3000;
    private final int BANNER_MILLIS_IN_FUTURE = 60 * 60 * 1000; //Finish countDownTimer after 1h (60*60*1000)

    private IZaloPayView mZaloPayView;

    protected CompositeSubscription compositeSubscription = new CompositeSubscription();

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
    public void setView(IZaloPayView o) {
        this.mZaloPayView = o;
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    @Override
    public void destroyView() {
        unsubscribeIfNotNull(compositeSubscription);
        mEventBus.unregister(this);
        this.mZaloPayView = null;
    }

    @Override
    public void resume() {
        startBannerCountDownTimer();
        if (NetworkHelper.isNetworkAvailable(mZaloPayView.getContext())) {
            mZaloPayView.hideNetworkError();
        }
    }

    @Override
    public void pause() {
        stopBannerCountDownTimer();
    }

    @Override
    public void destroy() {
        mBannerCountDownTimer = null;
        mBannerHandle = null;
        mBannerRunnable = null;
    }

    @Override
    public void initialize() {
        this.showListAppInDB();
        this.getTotalNotification(2000);
        this.getBanners();
        this.getBalance();
        this.getListAppResource();
    }

    private void showListAppInDB() {
        Subscription subscription = ObservableHelper.makeObservable(new Callable<List<AppResource>>() {
            @Override
            public List<AppResource> call() throws Exception {
                return mAppResourceRepository.listAppResourceFromDB();
            }
        }).subscribe(new AppResourceSubscriber());
        compositeSubscription.add(subscription);
    }

    @Override
    public void getBalance() {
        Subscription subscription = mBalanceRepository.balance()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BalanceSubscriber());

        compositeSubscription.add(subscription);
    }

    private void getListAppResource() {
        Subscription subscription = mAppResourceRepository.listAppResource()
                .doOnNext(new Action1<List<AppResource>>() {
                    @Override
                    public void call(List<AppResource> appResources) {
                        getListMerchantUser(appResources);
                    }
                })
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new AppResourceSubscriber());
        compositeSubscription.add(subscription);
    }

    // because of delay, subscriber at startup is sometime got triggered after the immediate subscriber
    // when received notification
    private void getTotalNotification(long delay) {
        Subscription subscription = mNotificationRepository.totalNotificationUnRead()
                .delaySubscription(delay, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new NotificationSubscriber());
        compositeSubscription.add(subscription);
    }


    private void getListMerchantUser(List<AppResource> listAppResource) {
        if (listAppResource == null) {
            return;
        }
        String strAppIds = ListStringUtil.toStringListAppId(listAppResource);

        if (TextUtils.isEmpty(strAppIds)) {
            return;
        }

        Subscription subscription = mMerchantRepository.getListMerchantUserInfo(strAppIds)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        compositeSubscription.add(subscription);
    }

    private void onGetAppResourceSuccess(List<AppResource> resources) {
        List<AppResource> listApps = new ArrayList<>(PaymentAppConfig.APP_RESOURCE_LIST);
        if (!Lists.isEmptyOrNull(resources)) {
            listApps.addAll(resources);
        }
        mZaloPayView.refreshInsideApps(listApps);
    }

    private class AppResourceSubscriber extends DefaultSubscriber<List<AppResource>> {

        @Override
        public void onNext(List<AppResource> appResources) {

            appResources.remove(new AppResource(BundleServiceImpl.ZALOPAY_INTERNAL_APPLICATION_ID));

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
        mZaloPayView.setBalance(balance);
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
                if (mZaloPayView == null) {
                    return;
                }
                mZaloPayView.changeBanner();
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
            mBannerHandle.removeCallbacks(mBannerRunnable);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            mBannerHandle.postDelayed(mBannerRunnable, BANNER_COUNT_DOWN_INTERVAL);
        }
    }

    public void getBanners() {
        try {
            List<DBanner> banners = CShareData.getInstance().getBannerList();
            if (banners != null && banners.size() > 1) {
                startBannerCountDownTimer();
            } else {
                stopBannerCountDownTimer();
            }

            mZaloPayView.showBannerAds(banners);
        } catch (Exception e) {
            Timber.w("Get banners exception: [%s]", e.getMessage());
        }
    }

    @Override
    public void startServiceWebViewActivity(int appId, String webViewUrl) {
        Subscription subscription = mMerchantRepository.getMerchantUserInfo(appId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MerchantUserInfoSubscribe(appId, webViewUrl));
        compositeSubscription.add(subscription);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetworkChange(NetworkChangeEvent event) {
        if (mZaloPayView == null) {
            return;
        }
        if (!event.isOnline) {
            mZaloPayView.showNetworkError();
        } else {
            mZaloPayView.hideNetworkError();
        }
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
        if (mZaloPayView != null) {
            mZaloPayView.setBalance(event.balance);
        }
    }

    private final class NotificationSubscriber extends DefaultSubscriber<Integer> {
        @Override
        public void onNext(Integer integer) {
            Timber.d("Got total %s unread notification messages", integer);
            if (mZaloPayView != null) {
                mZaloPayView.setTotalNotify(integer);
            }
        }
    }

    private class MerchantUserInfoSubscribe extends DefaultSubscriber<MerchantUserInfo> {
        private int mAppId;
        private String mWebViewUrl;

        private MerchantUserInfoSubscribe(int appId, String webViewUrl) {
            this.mAppId = appId;
            this.mWebViewUrl = webViewUrl;
        }

        @Override
        public void onNext(MerchantUserInfo merchantUserInfo) {
            Timber.d("onNext merchantInfo [%s]", merchantUserInfo);
            if (merchantUserInfo == null) {
                mZaloPayView.showError("MerchantUserInfo invalid");
                return;
            }
            WebViewPayInfo gamePayInfo = new WebViewPayInfo();
            gamePayInfo.setUid(merchantUserInfo.muid);
            gamePayInfo.setAccessToken(merchantUserInfo.maccesstoken);
            gamePayInfo.setAppId(mAppId);
            mNavigator.startServiceWebViewActivity(mZaloPayView.getContext(), gamePayInfo, mWebViewUrl);
        }

        @Override
        public void onError(Throwable e) {
            Timber.d(e, "onError exception");
            if (ResponseHelper.shouldIgnoreError(e)) {
                return;
            }
            mZaloPayView.showErrorDialog(ErrorMessageFactory.create(mZaloPayView.getContext(), e));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshPlatformInfoEvent(RefreshPlatformInfoEvent e) {
        Timber.d("onRefreshPlatformInfoEvent");
        this.getListAppResource();
        this.getBanners();
    }
}
