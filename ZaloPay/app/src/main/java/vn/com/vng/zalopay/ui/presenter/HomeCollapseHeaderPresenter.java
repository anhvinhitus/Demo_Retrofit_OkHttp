package vn.com.vng.zalopay.ui.presenter;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.eventbus.ChangeBalanceEvent;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.network.NetworkHelper;
import vn.com.vng.zalopay.ui.view.IHomeCollapseHeaderView;

/**
 * Created by Duke on 5/11/17.
 */

public class HomeCollapseHeaderPresenter extends AbstractPresenter<IHomeCollapseHeaderView> {
    private EventBus mEventBus;
    private BalanceStore.Repository mBalanceRepository;

    private Context mContext;

    @Inject
    HomeCollapseHeaderPresenter(Context context,
                     EventBus eventBus,
                     BalanceStore.Repository balanceRepository) {
        this.mEventBus = eventBus;
        this.mBalanceRepository = balanceRepository;
        this.mContext = context;
    }

    @Override
    public void attachView(IHomeCollapseHeaderView o) {
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
                .subscribe(new HomeCollapseHeaderPresenter.BalanceSubscriber());

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
}
