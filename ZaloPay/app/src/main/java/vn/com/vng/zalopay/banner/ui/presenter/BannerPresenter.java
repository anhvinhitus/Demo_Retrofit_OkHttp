package vn.com.vng.zalopay.banner.ui.presenter;

import android.os.CountDownTimer;
import android.os.Handler;
import android.view.MotionEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.ui.subscribe.MerchantUserInfoSubscribe;
import vn.com.vng.zalopay.ui.subscribe.StartPaymentAppSubscriber;
import vn.com.vng.zalopay.banner.ui.view.IBannerView;
import vn.com.vng.zalopay.data.appresources.AppResourceStore;
import vn.com.vng.zalopay.data.merchant.MerchantStore;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.event.RefreshPlatformInfoEvent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBanner;
import vn.com.zalopay.wallet.merchant.CShareData;

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
        try {
            List<DBanner> banners = CShareData.getInstance().getBannerList();
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

    public void startPaymentApp(AppResource app) {
        Subscription subscription = mAppResourceRepository.existResource(app.appid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new StartPaymentAppSubscriber(mNavigator, mView.getActivity(), app));
        mSubscription.add(subscription);
    }

    public void startServiceWebViewActivity(long appId, String webViewUrl) {
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
}
