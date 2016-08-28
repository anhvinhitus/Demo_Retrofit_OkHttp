package vn.com.vng.zalopay.withdraw.ui.presenter;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.data.eventbus.ChangeBalanceEvent;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;
import vn.com.vng.zalopay.withdraw.ui.view.IWithdrawHomeView;
import vn.com.zalopay.wallet.data.GlobalData;

/**
 * Created by longlv on 11/08/2016.
 */
public class WithdrawHomePresenter extends BaseUserPresenter implements IPresenter<IWithdrawHomeView> {

    private IWithdrawHomeView mView;
    private User mUser;

    CompositeSubscription compositeSubscription = new CompositeSubscription();

    public WithdrawHomePresenter(User user) {
        mUser = user;
    }

    public void updateUserInfo() {
        mView.updateUserInfo(mUser);
    }

    @Override
    public void setView(IWithdrawHomeView iWithdrawView) {
        mView = iWithdrawView;
    }

    @Override
    public void destroyView() {
        unsubscribeIfNotNull(compositeSubscription);
        mView = null;
    }

    @Override
    public void resume() {
        eventBus.register(this);
        mView.updateBalance(balanceRepository.currentBalance());
        updateBalance();
        updateUserInfo();
    }

    @Override
    public void pause() {
        eventBus.unregister(this);
    }

    @Override
    public void destroy() {
        GlobalData.initApplication(null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ChangeBalanceEvent event) {
        Timber.d("onEventMainThread ChangeBalanceEvent");
        if (mView != null) {
            mView.updateBalance(event.balance);
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNetworkChange(NetworkChangeEvent event) {
        if (event.isOnline) {
            updateBalance();
        }
    }

    protected void updateBalance() {
        Subscription subscription = balanceRepository.updateBalance()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        compositeSubscription.add(subscription);
    }

}
