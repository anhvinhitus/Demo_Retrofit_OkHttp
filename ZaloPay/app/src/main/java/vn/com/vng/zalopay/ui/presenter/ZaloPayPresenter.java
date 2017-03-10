package vn.com.vng.zalopay.ui.presenter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

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
import vn.com.vng.zalopay.data.util.NetworkHelper;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.event.RefreshPlatformInfoEvent;
import vn.com.vng.zalopay.event.SignOutEvent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.paymentapps.PaymentAppConfig;
import vn.com.vng.zalopay.paymentapps.PaymentAppTypeEnum;
import vn.com.vng.zalopay.ui.subscribe.MerchantUserInfoSubscribe;
import vn.com.vng.zalopay.ui.subscribe.StartPaymentAppSubscriber;
import vn.com.vng.zalopay.ui.view.IZaloPayView;

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
    public final int mNumberTopApp = 6;
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
        //  BusComponent.subscribe(APP_SUBJECT, this, new ComponentSubscriber(), AndroidSchedulers.mainThread());
    }

    private void unregisterEvent() {
        mEventBus.unregister(this);
        // BusComponent.unregister(this);
    }

    @Override
    public void detachView() {
        unregisterEvent();
        super.detachView();
    }

    @Override
    public void resume() {
        if (NetworkHelper.isNetworkAvailable(mView.getContext())) {
            mView.hideNetworkError();
        }
    }

    @Override
    public void pause() {
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public void initialize() {
        this.getListAppResource();
        this.getTotalNotification(2000);
        this.getBalance();
        this.ensureAppResourceAvailable();
    }

    @Override
    public void getBalance() {
        Subscription subscription = mBalanceRepository.balance()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BalanceSubscriber());

        mSubscription.add(subscription);
    }

    public void getListAppResource() {
        Subscription subscription = mAppResourceRepository.getListAppHome()
                .doOnNext(this::getListMerchantUser)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new AppResourceSubscriber());
        mSubscription.add(subscription);
    }

    @Override
    public void startPaymentApp(AppResource app) {
        Subscription subscription = mAppResourceRepository.existResource(app.appid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new StartPaymentAppSubscriber(mNavigator, mView.getActivity(), app));
        mSubscription.add(subscription);
    }

    @Override
    public void handleLaunchApp(AppResource app) {
        Timber.d("onclick app %s %s %s ", app.appType, app.appid, app.appname);
        if (app.appType == PaymentAppTypeEnum.REACT_NATIVE.getValue()) {
            if (app.appid == PaymentAppConfig.Constants.RED_PACKET) {
                mNavigator.startMiniAppActivity(mView.getActivity(), ModuleName.RED_PACKET);
            } else {
                AppResource appResource = getAppResource(app.appid);
                if (appResource == null) {
                    appResource = new AppResource(app.appid);
                }
                startPaymentApp(appResource);
            }
        } else if (app.appType == PaymentAppTypeEnum.WEBVIEW.getValue()) {
            startServiceWebViewActivity(app.appid, app.webUrl);
        } else if (app.appType == PaymentAppTypeEnum.INTERNAL_APP.getValue()) {
            if (app.appid == PaymentAppConfig.Constants.TRANSFER_MONEY) {
                mNavigator.startTransferMoneyActivity(mView.getActivity());
            } else if (app.appid == PaymentAppConfig.Constants.RECEIVE_MONEY) {
                mNavigator.startReceiveMoneyActivity(mView.getContext());
            }
        }
    }

    // because of delay, subscriber at startup is sometime got triggered after the immediate subscriber
    // when received notification
    public void getTotalNotification(long delay) {
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

        mView.enableShowShow(isEnableShowShow);
        mView.refreshInsideApps(resources);
        mView.setRefreshing(false);
    }

    private class AppResourceSubscriber extends DefaultSubscriber<List<AppResource>> {

        @Override
        public void onNext(List<AppResource> appResources) {
            ZaloPayPresenter.this.onGetAppResourceSuccess(appResources);
        }

        @Override
        public void onError(Throwable e) {
            Timber.d(e, "Get application resource error");
            if (mView == null) {
                return;
            }

            mView.setRefreshing(false);
        }
    }

    private class BalanceSubscriber extends DefaultSubscriber<Long> {
        @Override
        public void onNext(Long aLong) {
            ZaloPayPresenter.this.onGetBalanceSuccess(aLong);
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
        getBalance();
        ensureAppResourceAvailable();
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshPlatformInfoEvent(RefreshPlatformInfoEvent e) {
        Timber.d("onRefreshPlatformInfoEvent");
        if (System.currentTimeMillis() / 1000 - mLastTimeRefreshApp <= 120) {
            return;
        }

        Timber.d("Fetch list application");
        Subscription subscription = mAppResourceRepository.fetchListAppHome()
                .doOnNext(this::getListMerchantUser)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new AppResourceSubscriber());
        mSubscription.add(subscription);
    }

    private void ensureAppResourceAvailable() {
        Subscription subscription = mAppResourceRepository.ensureAppResourceAvailable()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        mSubscription.add(subscription);
    }
    
    public List<AppResource> setBannerInListApp(List<AppResource> pFullListApp) {

        List<AppResource> mNewListApp = new ArrayList<>();
        int inDex = 0;
        for (AppResource pItem : pFullListApp) {
            mNewListApp.add(pItem);

            if(pItem !=null && inDex == mNumberTopApp -1) {
                //hardcode test
                AppResource banner = new AppResource(0101, 0101,"Banner") ;
                mNewListApp.add(banner);
            }
            inDex ++;
        }
        return mNewListApp;
    }

    public int getHeightViewBottomView(View pTopView, int pNumberItemView, int pNumberApp) {
        double heightItem = pTopView.getHeight() / 2;
        int numberRow = (int) Math.ceil((pNumberItemView / (double) pNumberApp));
        return (int) (heightItem * numberRow);
    }



   /* private class ComponentSubscriber extends DefaultSubscriber<Object> {
        @Override
        public void onNext(Object event) {
            if (event instanceof ChangeBalanceEvent) {
                if (mView != null) {
                    mView.setBalance(((ChangeBalanceEvent) event).balance);
                }
            } else if (event instanceof NotificationChangeEvent) {
                if (!((NotificationChangeEvent) event).isRead()) {
                    getTotalNotification(0);
                }
            }
        }
    }*/
}
