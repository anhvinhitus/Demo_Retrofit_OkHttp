package vn.com.vng.zalopay.ui.presenter;

import android.os.CountDownTimer;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.eventbus.ChangeBalanceEvent;
import vn.com.vng.zalopay.data.eventbus.NotificationChangeEvent;
import vn.com.vng.zalopay.data.eventbus.ReadNotifyEvent;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.domain.model.MerchantUserInfo;
import vn.com.vng.zalopay.domain.repository.ZaloPayIAPRepository;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.game.config.AppGameDialogImpl;
import vn.com.vng.zalopay.game.config.AppGameNetworkingImpl;
import vn.com.vng.zalopay.game.config.AppGamePaymentImpl;
import vn.com.vng.zalopay.ui.view.IZaloPayView;
import vn.com.zalopay.game.businnesslogic.entity.base.AppGameError;
import vn.com.zalopay.game.businnesslogic.entity.pay.AppGamePayInfo;
import vn.com.zalopay.game.businnesslogic.interfaces.callback.IAppGameResultListener;
import vn.com.zalopay.game.controller.AppGameController;
import vn.com.zalopay.wallet.merchant.CShareData;

/**
 * Created by AnhHieu on 5/9/16.
 */
public class ZaloPayPresenterImpl extends BaseUserPresenter implements ZaloPayPresenter<IZaloPayView> {
    private final int BANNER_COUNT_DOWN_INTERVAL = 3000;
    private final int BANNER_MILLIS_IN_FUTURE = 60 * 60 * 1000; //Finish countDownTimer after 1h (60*60*1000)

    private IZaloPayView mZaloPayView;

    protected CompositeSubscription compositeSubscription = new CompositeSubscription();

    private final ZaloPayIAPRepository mZaloPayIAPRepository;

    //Banner variable
    private CountDownTimer mBannerCountDownTimer;
    //avoid case: new & release CountDownTimer continuously
    private Handler mBannerHandle = new Handler();
    private Runnable mBannerRunable = new Runnable() {
        @Override
        public void run() {
            startBannerCountDownTimer();
        }
    };

    public ZaloPayPresenterImpl(ZaloPayIAPRepository zaloPayIAPRepository) {
        this.mZaloPayIAPRepository = zaloPayIAPRepository;
    }

    @Override
    public void setView(IZaloPayView o) {
        this.mZaloPayView = o;
        if (!eventBus.isRegistered(this)) {
            eventBus.register(this);
        }
    }

    @Override
    public void destroyView() {
        unsubscribeIfNotNull(compositeSubscription);
        eventBus.unregister(this);
        this.mZaloPayView = null;
    }

    @Override
    public void resume() {
        startBannerCountDownTimer();
    }

    @Override
    public void pause() {
        stopBannerCountDownTimer();
    }

    @Override
    public void destroy() {
        mBannerCountDownTimer = null;
        mBannerHandle = null;
        mBannerRunable = null;
    }

    @Override
    public void initialize() {
        this.getTotalNotification(2000);
        this.getBanners();
        this.getBalance();
    }

    @Override
    public void getBalance() {
        Subscription subscription = balanceRepository.balance()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BalanceSubscriber());

        compositeSubscription.add(subscription);
    }

    @Override
    public void listAppResource() {
        try {
            List<Integer> insideApps = CShareData.getInstance(mZaloPayView.getActivity()).getApproveInsideApps();
            Subscription subscription = mAppResourceRepository.listAppResource(insideApps)
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new AppResourceSubscriber());

            compositeSubscription.add(subscription);
        } catch (Exception e) {
            Timber.w(e, "Get inside apps from PaymetSDK exception [%s]", e.getMessage());
        }
    }

    public List<AppResource> getListAppResourceFromDB() {
        return mAppResourceRepository.listAppResourceFromDB();
    }

    // because of delay, subscriber at startup is sometime got triggered after the immediate subscriber
    // when received notification
    private void getTotalNotification(long delay) {
        Subscription subscription = notificationRepository.totalNotificationUnRead()
                .delaySubscription(delay, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new NotificationSubscriber());
        compositeSubscription.add(subscription);
    }

    private void onGetAppResourceSuccess(List<AppResource> resources) {
        mZaloPayView.refreshInsideApps(resources);
    }

    private final class AppResourceSubscriber extends DefaultSubscriber<List<AppResource>> {
        public AppResourceSubscriber() {
        }

        @Override
        public void onNext(List<AppResource> appResources) {
            ZaloPayPresenterImpl.this.onGetAppResourceSuccess(appResources);

            Timber.d(" AppResource %s", appResources.size());
        }

        @Override
        public void onError(Throwable e) {
            if (ResponseHelper.shouldIgnoreError(e)) {
                // simply ignore the error
                // because it is handled from event subscribers
                return;
            }
        }
    }

    private class BalanceSubscriber extends DefaultSubscriber<Long> {
        public BalanceSubscriber() {
        }

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
            mBannerHandle.removeCallbacks(mBannerRunable);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            mBannerHandle.postDelayed(mBannerRunable, BANNER_COUNT_DOWN_INTERVAL);
        }
    }

    public void getBanners() {
        try {
            List banners = CShareData.getInstance(mZaloPayView.getActivity()).getBannerList();
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
    public void startGamePayWebActivity(AppResource appResource) {
        if (appResource == null) {
            return;
        }
        mZaloPayIAPRepository.getMerchantUserInfo(appResource.appid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new GamePaySubscribe(appResource));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetworkChange(NetworkChangeEvent event) {
        if (!event.isOnline && mZaloPayView != null) {
            mZaloPayView.showNetworkError();
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNotificationUpdated(NotificationChangeEvent event) {
        Timber.d("on Notification updated %s", event.read);
        if (!event.read) {
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

    private class GamePaySubscribe extends DefaultSubscriber<MerchantUserInfo> {
        private AppResource mAppResource;

        public GamePaySubscribe(AppResource appResource) {
            this.mAppResource = appResource;
        }

        @Override
        public void onNext(MerchantUserInfo merchantUserInfo) {
            Timber.d("onNext merchantInfo [%s]", merchantUserInfo);
            if (merchantUserInfo == null) {
                mZaloPayView.showError("MerchantUserInfo invalid");
                return;
            }
            AppGamePayInfo gamePayInfo = new AppGamePayInfo();
            gamePayInfo.setUid(merchantUserInfo.muid);
            gamePayInfo.setAccessToken(merchantUserInfo.maccesstoken);
            gamePayInfo.setAppId(mAppResource.appid);
            IAppGameResultListener gameResultListener = new IAppGameResultListener() {
                @Override
                public void onError(AppGameError pError) {
                    Timber.d("onError pError [%s]", pError);
                    if (pError == null) {
                        return;
                    }
                    mZaloPayView.showError(pError.messError);
                }

                @Override
                public void onLogout() {
                    Timber.d("onLogout start");
                    if (mZaloPayView == null) {
                        return;
                    }
                    mZaloPayView.onSessionExpired();
                }
            };
            Timber.d("onNext startPayFlow");
            AppGameController.startPayFlow(mZaloPayView.getActivity(), gamePayInfo, gameResultListener,
                    new AppGamePaymentImpl(), new AppGameDialogImpl(), mAppResource.webUrl, new AppGameNetworkingImpl());
        }

        @Override
        public void onError(Throwable e) {
            Timber.d("onError exception [%s]", e);
            if (ResponseHelper.shouldIgnoreError(e)) {
                return;
            }
            mZaloPayView.showErrorDialog(ErrorMessageFactory.create(mZaloPayView.getContext(), e));
        }
    }
}
