package vn.com.vng.zalopay.ui.presenter;

import android.content.Context;
import android.util.SparseIntArray;

import com.zalopay.apploader.internal.ModuleName;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.data.appresources.AppResourceStore;
import vn.com.vng.zalopay.data.eventbus.WsConnectionEvent;
import vn.com.vng.zalopay.data.merchant.MerchantStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.event.RefreshPlatformInfoEvent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.network.NetworkHelper;
import vn.com.vng.zalopay.paymentapps.PaymentAppConfig;
import vn.com.vng.zalopay.paymentapps.PaymentAppTypeEnum;
import vn.com.vng.zalopay.ui.subscribe.MerchantUserInfoSubscribe;
import vn.com.vng.zalopay.ui.subscribe.StartPaymentAppSubscriber;
import vn.com.vng.zalopay.ui.view.IZaloPayView;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;

import static vn.com.vng.zalopay.data.util.Lists.isEmptyOrNull;
import static vn.com.vng.zalopay.paymentapps.PaymentAppConfig.getAppResource;

/**
 * Created by AnhHieu on 5/9/16.
 * *
 */
public class ZaloPayPresenter extends AbstractPresenter<IZaloPayView> implements IZaloPayPresenter<IZaloPayView> {
    private final MerchantStore.Repository mMerchantRepository;
    private EventBus mEventBus;
    private AppResourceStore.Repository mAppResourceRepository;
    private Navigator mNavigator;

    private Context mContext;

    private long mLastTimeRefreshApp;

    private final static SparseIntArray sActionMap;

    static {
        sActionMap = new SparseIntArray(15);
        sActionMap.put(0, ZPEvents.TAPAPPICON_1_1);
        sActionMap.put(1, ZPEvents.TAPAPPICON_1_2);
        sActionMap.put(2, ZPEvents.TAPAPPICON_1_3);
        sActionMap.put(3, ZPEvents.TAPAPPICON_2_1);
        sActionMap.put(4, ZPEvents.TAPAPPICON_2_2);
        sActionMap.put(5, ZPEvents.TAPAPPICON_2_3);
        sActionMap.put(6, ZPEvents.TAPAPPICON_3_1);
        sActionMap.put(7, ZPEvents.TAPAPPICON_3_2);
        sActionMap.put(8, ZPEvents.TAPAPPICON_3_3);
        sActionMap.put(9, ZPEvents.TAPAPPICON_4_1);
        sActionMap.put(10, ZPEvents.TAPAPPICON_4_2);
        sActionMap.put(11, ZPEvents.TAPAPPICON_4_3);
        sActionMap.put(12, ZPEvents.TAPAPPICON_5_1);
        sActionMap.put(13, ZPEvents.TAPAPPICON_5_2);
        sActionMap.put(14, ZPEvents.TAPAPPICON_5_3);
    }

    @Inject
    ZaloPayPresenter(Context context, MerchantStore.Repository mMerchantRepository,
                     EventBus eventBus,
                     AppResourceStore.Repository appResourceRepository,
                     Navigator navigator) {
        this.mMerchantRepository = mMerchantRepository;
        this.mEventBus = eventBus;
        this.mAppResourceRepository = appResourceRepository;
        this.mNavigator = navigator;
        this.mContext = context;
    }

    @Override
    public void initialize() {
        getListAppResource();
    }

    @Override
    public void attachView(IZaloPayView iHomeListAppView) {
        super.attachView(iHomeListAppView);
        registerEvent();
    }

    @Override
    public void detachView() {
        unregisterEvent();
        super.detachView();
    }

    @Override
    public void resume() {
        if (NetworkHelper.isNetworkAvailable(mContext)) {
            if (mView != null) {
                mView.hideNetworkError();
            }
        }
    }

    @Override
    public void startServiceWebViewActivity(long appId, String webViewUrl) {
        Subscription subscription = mMerchantRepository.getMerchantUserInfo(appId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MerchantUserInfoSubscribe(mNavigator, mView.getActivity(), appId, webViewUrl));
        mSubscription.add(subscription);
    }

    @Override
    public void launchApp(AppResource app, int position) {
        Timber.d("launchApp appType [%s] appid [%s] appname [%s] ", app.appType, app.appid, app.appname);
//
        if (app.appType == PaymentAppTypeEnum.REACT_NATIVE.getValue()) {
            if (app.appid == PaymentAppConfig.Constants.RED_PACKET) {
                mNavigator.startMiniAppActivity(mView.getActivity(), ModuleName.RED_PACKET);
            } else {
                AppResource appResource = getAppResource(app.appid);
                if (appResource == null) {
                    appResource = new AppResource(app.appid);
                }
                startExternalApp(appResource);
            }
        } else if (app.appType == PaymentAppTypeEnum.WEBVIEW.getValue()) {
            startServiceWebViewActivity(app.appid, app.webUrl);
        } else if (app.appType == PaymentAppTypeEnum.INTERNAL_APP.getValue()) {
            if (app.appid == PaymentAppConfig.Constants.TRANSFER_MONEY) {
                mNavigator.startTransferMoneyActivity(mView.getActivity());
            } else if (app.appid == PaymentAppConfig.Constants.RECEIVE_MONEY) {
                mNavigator.startReceiveMoneyActivity(mView.getActivity());
            } else if (app.appid == PaymentAppConfig.Constants.DEPOSIT) {
                mNavigator.startDepositActivity(mView.getActivity());
            }
        }

        Timber.d("Tap on app at position %d", position);

        int action = sActionMap.get(position, -1);
        if (action >= 0) {
            ZPAnalytics.trackEvent(sActionMap.get(position));
        }
    }

    /*
    * Local functions
    * */
    public void getListAppResource() {
        Subscription subscription = mAppResourceRepository.getListAppHome()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(this::onGetAppResourceSuccess)
                .doOnError(throwable -> setRefreshing(false))
                .skipLast(1)
                .doOnNext(this::getListMerchantUser)
                .subscribe(new DefaultSubscriber<>());
        mSubscription.add(subscription);
    }

    private void fetchListAppResource() {
        Timber.d("Fetch list application");
        Subscription subscription = mAppResourceRepository.fetchListAppHome()
                .doOnNext(this::getListMerchantUser)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(this::onGetAppResourceSuccess)
                .doOnError(e -> {
                    Timber.d(e, "Get application resource error");
                    setRefreshing(false);
                })
                .subscribe(new DefaultSubscriber<>());
        mSubscription.add(subscription);
    }

    private void startExternalApp(AppResource app) {
        Subscription subscription = mAppResourceRepository.isAppResourceAvailable(app.appid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new StartPaymentAppSubscriber(mNavigator, mView.getActivity(), app));
        mSubscription.add(subscription);
    }

    private void onGetAppResourceSuccess(List<AppResource> resources) {
        Timber.d("get app resource success - size [%s]", resources.size());

        if (mView == null) {
            return;
        }

        mView.setAppItems(resources);
        mView.setRefreshing(false);

        mLastTimeRefreshApp = System.currentTimeMillis() / 1000;
    }

    private void setRefreshing(boolean value) {
        if (mView != null) {
            mView.setRefreshing(value);
        }
    }

    private void getListMerchantUser(List<AppResource> listAppResource) {
        Timber.d("getListMerchantUser: [%s]", listAppResource.size());
        if (isEmptyOrNull(listAppResource)) {
            return;
        }

        Subscription subscription = Observable.from(listAppResource)
                .filter(appResource -> appResource != null)
                .map(appResource -> appResource.appid)
                .toList()
                .concatMap(mMerchantRepository::getListMerchantUserInfo)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        mSubscription.add(subscription);
    }

    private void ensureAppResourceAvailable() {
        Subscription subscription = mAppResourceRepository.ensureAppResourceAvailable()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        mSubscription.add(subscription);
    }

    private void registerEvent() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    private void unregisterEvent() {
        if (mEventBus.isRegistered(this)) {
            mEventBus.unregister(this);
        }
    }

    /*
    * Event bus
    * */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshPlatformInfoEvent(RefreshPlatformInfoEvent e) {
        Timber.d("onRefreshPlatformInfoEvent");

        if (System.currentTimeMillis() / 1000 - mLastTimeRefreshApp <= 120) {
            return;
        }
        fetchListAppResource();
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
        mView.hideNetworkError();
        ensureAppResourceAvailable();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWsConnectionChanged(WsConnectionEvent event) {
        if (mView == null) {
            return;
        }

        Timber.d("on socket connection changed: [isConnect %s]", event.isConnect);

        if (event.isConnect) {
            mView.hideNetworkError();
        } else {
            mView.showWsConnectError();
        }
    }
}