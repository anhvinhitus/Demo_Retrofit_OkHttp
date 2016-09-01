package vn.com.vng.zalopay.ui.presenter;

import android.app.Activity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.eventbus.ChangeBalanceEvent;
import vn.com.vng.zalopay.data.eventbus.NotificationChangeEvent;
import vn.com.vng.zalopay.data.eventbus.ReadNotifyEvent;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.domain.model.MerchantUserInfo;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.repository.ZaloPayIAPRepository;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.game.AppGameConfigImpl;
import vn.com.vng.zalopay.game.AppGameDialogImpl;
import vn.com.vng.zalopay.game.AppGameNetworkingImpl;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.ui.view.IZaloPayView;
import vn.com.zalopay.game.businnesslogic.entity.base.AppGameError;
import vn.com.zalopay.game.businnesslogic.entity.pay.AppGamePayInfo;
import vn.com.zalopay.game.businnesslogic.interfaces.callback.IAppGameResultListener;
import vn.com.zalopay.game.controller.AppGameController;
import vn.com.zalopay.wallet.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.merchant.CShareData;

/**
 * Created by AnhHieu on 5/9/16.
 */
public class ZaloPayPresenterImpl extends BaseUserPresenter implements ZaloPayPresenter<IZaloPayView> {

    private IZaloPayView mZaloPayView;
    private ZaloPayIAPRepository mZaloPayIAPRepository;

    protected CompositeSubscription compositeSubscription = new CompositeSubscription();

    public ZaloPayPresenterImpl(ZaloPayIAPRepository zaloPayIAPRepository) {
        mZaloPayIAPRepository = zaloPayIAPRepository;
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
    }

    @Override
    public void pause() {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void initialize() {
        this.getTotalNotification(2000);
        this.listAppResource();
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
        Subscription subscription = mAppResourceRepository.listAppResource()
                .delaySubscription(3, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new AppResourceSubscriber());

        compositeSubscription.add(subscription);
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
        // mZaloPayView.insertApps(resources);
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

    public void getBanners() {
        try {
            mZaloPayView.showBannerAds(CShareData.getInstance(mZaloPayView.getActivity()).getBannerList());
        } catch (Exception e) {
            Timber.w("Get banners exception: [%s]", e.getMessage());
        }
    }

    @Override
    public void startGamePayWebActivity(int appId) {
        mZaloPayIAPRepository.getMerchantUserInfo(appId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new GamePaySubscribe(appId));
    }

    @Override
    public void payOrder(Order order, final AppGamePayInfo appGamePayInfo) {
        showLoadingView();
        PaymentWrapper paymentWrapper = new PaymentWrapper(balanceRepository, zaloPayRepository, transactionRepository, new PaymentWrapper.IViewListener() {
            @Override
            public Activity getActivity() {
                if (mZaloPayView != null) {
                    return mZaloPayView.getActivity();
                }
                return null;
            }
        }, new PaymentWrapper.IResponseListener() {
            @Override
            public void onParameterError(String param) {

                Timber.d("onParameterError");

                if (mZaloPayView == null) {
                    return;
                }

                if ("order".equalsIgnoreCase(param)) {
                    mZaloPayView.showError(applicationContext.getString(R.string.order_invalid));
                } else if ("uid".equalsIgnoreCase(param)) {
                    mZaloPayView.showError(applicationContext.getString(R.string.user_invalid));
                } else if ("token".equalsIgnoreCase(param)) {
                    mZaloPayView.showError(applicationContext.getString(R.string.order_invalid));
                }

                hideLoadingView();
            }

            @Override
            public void onPreComplete(boolean isSuccessful,String transId) {

            }

            @Override
            public void onResponseError(PaymentError paymentError) {
                Timber.d("onResponseError");
                if (mZaloPayView == null) {
                    return;
                }

                if (paymentError == PaymentError.ERR_CODE_INTERNET) {
                    mZaloPayView.showError(applicationContext.getString(R.string.exception_no_connection_try_again));
                }

                hideLoadingView();
            }

            @Override
            public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
                Timber.d("onResponseSuccess");
                hideLoadingView();
                if (mZaloPayView == null || zpPaymentResult == null
                        || zpPaymentResult.paymentInfo == null) {
                    return;
                }
                IAppGameResultListener iAppGameResultListener = new IAppGameResultListener() {
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

                AppGamePayInfo appGamePayInfo2 = new AppGamePayInfo();
                appGamePayInfo2.setAppId(appGamePayInfo.getAppId());
                appGamePayInfo2.setApptransid(zpPaymentResult.paymentInfo.appTransID);
                appGamePayInfo2.setUid(appGamePayInfo.getUid());
                appGamePayInfo2.setAccessToken(appGamePayInfo.getAccessToken());

                AppGameController.viewPayResult(mZaloPayView.getActivity(), appGamePayInfo2, iAppGameResultListener,
                        new AppGameDialogImpl(), new AppGameConfigImpl(), new AppGameNetworkingImpl());
            }

            @Override
            public void onResponseTokenInvalid() {
                Timber.d("onResponseTokenInvalid");
                if (mZaloPayView == null) {
                    return;
                }
                hideLoadingView();
            }

            @Override
            public void onAppError(String msg) {
                Timber.d("onAppError msg [%s]", msg);
                if (mZaloPayView == null) {
                    return;
                }
                if (mZaloPayView.getContext() != null) {
                    mZaloPayView.showError(mZaloPayView.getContext().getString(R.string.exception_generic));
                }
                hideLoadingView();
            }

            @Override
            public void onNotEnoughMoney() {
                Timber.d("onNotEnoughMoney");

                if (mZaloPayView == null) {
                    return;
                }
                hideLoadingView();
                navigator.startDepositActivity(applicationContext);
            }
        });

        paymentWrapper.payWithOrder(order);
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
        private int mAppId;

        public GamePaySubscribe(int appId) {
            this.mAppId = appId;
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
            gamePayInfo.setAppId(mAppId);
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
                    new AppGameDialogImpl(), new AppGameConfigImpl(), new AppGameNetworkingImpl());
        }

        @Override
        public void onError(Throwable e) {
            if (e instanceof BodyException) {
                mZaloPayView.showError(((BodyException) e).getMessage());
            }
        }
    }

    private void showLoadingView() {
        if (mZaloPayView == null) {
            return;
        }
        mZaloPayView.showLoading();
    }

    private void hideLoadingView() {
        if (mZaloPayView == null) {
            return;
        }
        mZaloPayView.hideLoading();
    }
}
