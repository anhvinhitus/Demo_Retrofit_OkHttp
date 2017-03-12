package vn.com.vng.zalopay.banner.ui.presenter;

import android.os.CountDownTimer;
import android.os.Handler;
import android.view.MotionEvent;

import com.zalopay.apploader.internal.ModuleName;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.banner.model.BannerInternalFunction;
import vn.com.vng.zalopay.banner.model.BannerType;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.vng.zalopay.ui.subscribe.MerchantUserInfoSubscribe;
import vn.com.vng.zalopay.ui.subscribe.StartPaymentAppSubscriber;
import vn.com.vng.zalopay.banner.ui.view.IBannerView;
import vn.com.vng.zalopay.data.appresources.AppResourceStore;
import vn.com.vng.zalopay.data.merchant.MerchantStore;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.event.RefreshPlatformInfoEvent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBanner;

/**
 * Created by longlv on 12/14/16.
 * *
 */

public class BannerPresenter extends AbstractPresenter<IBannerView> {
    private final int BANNER_COUNT_DOWN_INTERVAL = 3000;
    private int BANNER_MILLIS_IN_FUTURE = 60 * 60 * 1000; //Finish countDownTimer after 1h (60*60*1000)

    private EventBus mEventBus;
    private MerchantStore.Repository mMerchantRepository;
    private AppResourceStore.Repository mAppResourceRepository;
    private Navigator mNavigator;

    //Banner variable
    private boolean mIsFirstStartCountDown = true;
    private CountDownTimer mBannerCountDownTimer;
    //avoid case: new & release CountDownTimer continuously
    private Handler mBannerHandle = new Handler();
    private Runnable mBannerRunnable = new Runnable() {
        @Override
        public void run() {
            startBannerCountDownTimer();
        }
    };

    @Inject
    BannerPresenter(MerchantStore.Repository merchantRepository,
                    AppResourceStore.Repository appResourceRepository,
                    EventBus eventBus,
                    Navigator navigator) {
        mMerchantRepository = merchantRepository;
        mAppResourceRepository = appResourceRepository;
        mEventBus = eventBus;
        mNavigator = navigator;
    }

    @Override
    public void attachView(IBannerView o) {
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
        getBanners();
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

    private void startBannerCountDownTimer() {
        if (mBannerCountDownTimer != null) {
            return;
        }
        mIsFirstStartCountDown = true;
        mBannerCountDownTimer = new CountDownTimer(BANNER_MILLIS_IN_FUTURE, BANNER_COUNT_DOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
//                Timber.d("onTick changeBanner, currentTime[%s]", System.currentTimeMillis());
                if (mIsFirstStartCountDown) {
                    mIsFirstStartCountDown = false;
                    return;
                }
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

    private void stopBannerCountDownTimer() {
        if (mBannerCountDownTimer == null) {
            return;
        }
        mBannerCountDownTimer.cancel();
        mBannerCountDownTimer = null;
    }

    public void onTouchBanner(MotionEvent event) {
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

    private void getBanners() {
        Timber.d("getBanners");
        try {
            List<DBanner> banners = CShareDataWrapper.getBannerList();
            if (banners != null && banners.size() > 1) {
                startBannerCountDownTimer();
            } else {
                stopBannerCountDownTimer();
            }
            mView.showBannerAds(banners);
        } catch (Exception e) {
            Timber.w(e, "Get banners exception");
        }
    }

    private void startPaymentApp(AppResource app) {
        Subscription subscription = mAppResourceRepository.existResource(app.appid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new StartPaymentAppSubscriber(mNavigator, mView.getActivity(), app));
        mSubscription.add(subscription);
    }

    private void startServiceWebViewActivity(long appId, String webViewUrl) {
        Subscription subscription = mMerchantRepository.getMerchantUserInfo(appId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MerchantUserInfoSubscribe(mNavigator, mView.getActivity(), appId, webViewUrl));
        mSubscription.add(subscription);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshPlatformInfoEvent(RefreshPlatformInfoEvent e) {
        Timber.d("onRefreshPlatformInfoEvent");
        getBanners();
    }

    public void handleTouchBannerItem(DBanner banner, int position) {

        if (banner == null) {
            return;
        }
        if (banner.bannertype == BannerType.InternalFunction.getValue()) {
            if (banner.function == BannerInternalFunction.Deposit.getValue()) {
                mNavigator.startDepositActivity(mView.getActivity());
            } else if (banner.function == BannerInternalFunction.WithDraw.getValue()) {
                mNavigator.startBalanceManagementActivity(mView.getActivity());
            } else if (banner.function == BannerInternalFunction.SaveCard.getValue()) {
                mNavigator.startLinkCardActivity(mView.getActivity());
            } else if (banner.function == BannerInternalFunction.Pay.getValue()) {
                mNavigator.startScanToPayActivity(mView.getActivity());
            } else if (banner.function == BannerInternalFunction.TransferMoney.getValue()) {
                mNavigator.startTransferMoneyActivity(mView.getActivity());
            } else if (banner.function == BannerInternalFunction.RedPacket.getValue()) {
                mNavigator.startMiniAppActivity(mView.getActivity(), ModuleName.RED_PACKET);
            }
        } else if (banner.bannertype == BannerType.PaymentApp.getValue()) {
            startPaymentApp(new AppResource(banner.appid));
        } else if (banner.bannertype == BannerType.ServiceWebView.getValue()) {
            startServiceWebViewActivity(banner.appid, banner.webviewurl);
        } else if (banner.bannertype == BannerType.WebPromotion.getValue()) {
            mNavigator.startWebViewActivity(mView.getActivity(), banner.webviewurl);
        }
        trackBannerEvent(position);
    }

    private void trackBannerEvent(int position) {
        switch (position) {
            case 0:
                ZPAnalytics.trackEvent(ZPEvents.TAPBANNERPOSITION1);
                break;
            case 1:
                ZPAnalytics.trackEvent(ZPEvents.TAPBANNERPOSITION2);
                break;
            case 2:
                ZPAnalytics.trackEvent(ZPEvents.TAPBANNERPOSITION3);
                break;
            case 3:
                ZPAnalytics.trackEvent(ZPEvents.TAPBANNERPOSITION4);
                break;
            default:
                ZPAnalytics.trackEvent(ZPEvents.TAPBANNERPOSITION4);
                break;
        }
    }
}
