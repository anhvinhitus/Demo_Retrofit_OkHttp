package vn.com.vng.zalopay.ui.presenter;

import android.content.Context;
import android.util.SparseIntArray;

import com.zalopay.apploader.internal.ModuleName;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.data.appresources.AppResourceStore;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.eventbus.ChangeBalanceEvent;
import vn.com.vng.zalopay.data.eventbus.NotificationChangeEvent;
import vn.com.vng.zalopay.data.eventbus.ReadNotifyEvent;
import vn.com.vng.zalopay.data.eventbus.WsConnectionEvent;
import vn.com.vng.zalopay.data.merchant.MerchantStore;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.event.LoadIconFontEvent;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.event.RefreshPlatformInfoEvent;
import vn.com.vng.zalopay.event.SignOutEvent;
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
import static vn.com.vng.zalopay.paymentapps.PaymentAppConfig.Constants;
import static vn.com.vng.zalopay.paymentapps.PaymentAppConfig.getAppResource;

/**
 * Created by AnhHieu on 5/9/16.
 * *
 */
public class ZaloPayPresenter extends AbstractPresenter<IZaloPayView> implements IZaloPayPresenter<IZaloPayView> {

    private final MerchantStore.Repository mMerchantRepository;
    private EventBus mEventBus;
    private BalanceStore.Repository mBalanceRepository;
    private AppResourceStore.Repository mAppResourceRepository;
    private NotificationStore.Repository mNotificationRepository;
    private Navigator mNavigator;

    private Context mContext;

    private long mLastTimeRefreshApp;

    @Inject
    ZaloPayPresenter(Context context, MerchantStore.Repository mMerchantRepository,
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
        this.mContext = context;
    }

    @Override
    public void attachView(IZaloPayView o) {
        super.attachView(o);
        registerEvent();
    }

    private void registerEvent() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    private void unregisterEvent() {
        mEventBus.unregister(this);
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
    public void pause() {
    }

    @Override
    public void initialize() {
        getListAppResource();
        getTotalNotification(100);
        getBalanceLocal();
    }

    private void getBalanceLocal() {
        Subscription subscription = mBalanceRepository.balanceLocal()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BalanceSubscriber());

        mSubscription.add(subscription);
    }

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
        Subscription subscription = mAppResourceRepository.fetchAppResource()
                .doOnNext(this::getListMerchantUser)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new AppResourceSubscriber());
        mSubscription.add(subscription);
    }

    private void startExternalApp(AppResource app) {
        Subscription subscription = mAppResourceRepository.existResource(app.appid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new StartPaymentAppSubscriber(mNavigator, mView.getActivity(), app));
        mSubscription.add(subscription);
    }

    @Override
    public void launchApp(AppResource app, int position) {

        Timber.d("launchApp appType [%s] appid [%s] appname [%s] ", app.appType, app.appid, app.appname);

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
            }
        }

        Timber.d("Tap on app at position %d", position);

        int action = sActionMap.get(position, -1);
        if (action >= 0) {
            ZPAnalytics.trackEvent(sActionMap.get(position));
        }
    }

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

    private void onGetAppResourceSuccess(List<AppResource> resources) {
        Timber.d("get app resource success - size [%s]", resources.size());

        if (mView == null) {
            return;
        }

        mLastTimeRefreshApp = System.currentTimeMillis() / 1000;

        AppResource showhow = getAppResource(Constants.SHOW_SHOW);

        boolean isEnableShowShow = resources.contains(showhow);

        if (isEnableShowShow) {
            resources.remove(showhow);
        }

        mView.setAppItems(resources);
        mView.setRefreshing(false);
    }

    private void setRefreshing(boolean value) {
        if (mView != null) {
            mView.setRefreshing(value);
        }
    }

    private class AppResourceSubscriber extends DefaultSubscriber<List<AppResource>> {

        @Override
        public void onNext(List<AppResource> appResources) {
            onGetAppResourceSuccess(appResources);
        }

        @Override
        public void onError(Throwable e) {
            Timber.d(e, "Get application resource error");
            setRefreshing(false);
        }
    }

    private class BalanceSubscriber extends DefaultSubscriber<Long> {
        @Override
        public void onNext(Long aLong) {
            onGetBalanceSuccess(aLong);
        }
    }

    private void onGetBalanceSuccess(Long balance) {
        Timber.d("onGetBalanceSuccess %s", balance);
        mView.setBalance(balance);
    }

    @Override
    public void startServiceWebViewActivity(long appId, String webViewUrl) {
        Subscription subscription = mMerchantRepository.getMerchantUserInfo(appId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MerchantUserInfoSubscribe(mNavigator, mView.getActivity(), appId, webViewUrl));
        mSubscription.add(subscription);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWsConnectionChanged(WsConnectionEvent event) {
        if (mView == null) {
            return;
        }

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
        mView.hideNetworkError();

        ensureAppResourceAvailable();
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

    private void ensureAppResourceAvailable() {
        Subscription subscription = mAppResourceRepository.ensureAppResourceAvailable()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        mSubscription.add(subscription);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshPlatformInfoEvent(RefreshPlatformInfoEvent e) {
        Timber.d("onRefreshPlatformInfoEvent");

        if (System.currentTimeMillis() / 1000 - mLastTimeRefreshApp <= 120) {
            return;
        }
        fetchListAppResource();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onLoadIconFontSuccess(LoadIconFontEvent event) {
        mEventBus.removeStickyEvent(LoadIconFontEvent.class);
       /* if (mView != null) {
            mView.refreshIconFont();
        }*/
    }

}
