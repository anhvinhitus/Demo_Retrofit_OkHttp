package vn.com.vng.zalopay.ui.presenter;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.eventbus.ChangeBalanceEvent;
import vn.com.vng.zalopay.data.eventbus.NotificationChangeEvent;
import vn.com.vng.zalopay.data.eventbus.ReadNotifyEvent;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.network.NetworkHelper;
import vn.com.vng.zalopay.ui.view.IZaloPayView;

/**
 * Created by AnhHieu on 5/9/16.
 * *
 */
public class ZaloPayPresenter extends AbstractPresenter<IZaloPayView> {
    private EventBus mEventBus;
    private BalanceStore.Repository mBalanceRepository;
    private NotificationStore.Repository mNotificationRepository;

    private Context mContext;

    @Inject
    ZaloPayPresenter(Context context,
                     EventBus eventBus,
                     BalanceStore.Repository balanceRepository,
                     NotificationStore.Repository notificationRepository) {
        this.mEventBus = eventBus;
        this.mBalanceRepository = balanceRepository;
        this.mNotificationRepository = notificationRepository;
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
            mView.setBalance(mBalanceRepository.currentBalance());
            fetchBalance();
        }
    }

    /*
    * Local functions
    * */
    public void initialize() {
        getTotalNotification(100);
        fetchBalance();
    }

    private void fetchBalance() {
        Subscription subscription = mBalanceRepository.fetchBalance()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultSubscriber<>());

        mSubscription.add(subscription);
    }

    private void getBalanceLocal() {
        Subscription subscription = mBalanceRepository.balanceLocal()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BalanceSubscriber());

        mSubscription.add(subscription);
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

    /*
    * Event bus
    * */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetworkChange(NetworkChangeEvent event) {
        if (mView == null) {
            return;
        }
        if (!event.isOnline) {
            return;
        }
        fetchBalance();
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

    /*
    * Custom subscribers
    * */
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

    private final class NotificationSubscriber extends DefaultSubscriber<Integer> {
        @Override
        public void onNext(Integer integer) {
            Timber.d("Got total %s unread notification messages", integer);
            if (mView != null) {
                mView.setTotalNotify(integer);
            }
        }
    }
}