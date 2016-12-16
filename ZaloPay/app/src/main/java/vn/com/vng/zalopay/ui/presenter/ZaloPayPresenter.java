package vn.com.vng.zalopay.ui.presenter;

import android.content.Context;

import com.zalopay.apploader.internal.ModuleName;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
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

    private Context mContext;

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

        this.getListAppResource(false);
        this.getTotalNotification(2000);
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

    private void getListAppResource(boolean isShouldUpdate) {

        Observable<List<AppResource>> observable = isShouldUpdate ? mAppResourceRepository.fetchListAppHome() :
                mAppResourceRepository.getListAppHome();

        Subscription subscription = observable
                .doOnNext(new Action1<List<AppResource>>() {
                    @Override
                    public void call(List<AppResource> appResources) {
                        getListMerchantUser(appResources);
                    }
                })
                .compose(this.<List<AppResource>>applySchedulers())
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

        AppResource showhow = getAppResource(Constants.SHOW_SHOW);

        boolean isEnableShowShow = resources.contains(showhow);

        if (isEnableShowShow) {
            resources.remove(showhow);
        }

        mView.enableShowShow(isEnableShowShow);
        mView.refreshInsideApps(resources);
    }

    private class AppResourceSubscriber extends DefaultSubscriber<List<AppResource>> {

        @Override
        public void onNext(List<AppResource> appResources) {
            ZaloPayPresenter.this.onGetAppResourceSuccess(appResources);
            Timber.d(" AppResource %s", appResources.size());
        }

        @Override
        public void onError(Throwable e) {
            Timber.d(e, "Get application resource error");
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshPlatformInfoEvent(RefreshPlatformInfoEvent e) {
        Timber.d("onRefreshPlatformInfoEvent");
        this.getListAppResource(true);
    }
}
