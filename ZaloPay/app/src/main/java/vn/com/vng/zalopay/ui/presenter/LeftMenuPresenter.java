package vn.com.vng.zalopay.ui.presenter;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Singleton;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.event.NetworkChangeEvent;
import vn.com.vng.zalopay.interactor.event.ZaloPayNameEvent;
import vn.com.vng.zalopay.interactor.event.ZaloProfileInfoEvent;
import vn.com.vng.zalopay.ui.view.ILeftMenuView;

/**
 * Created by AnhHieu on 5/11/16.
 */

@Singleton
public class LeftMenuPresenter extends BaseUserPresenter implements IPresenter<ILeftMenuView> {
    private ILeftMenuView menuView;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();
    private User user;

    private boolean isInitiated;

    public LeftMenuPresenter(User user) {
        this.user = user;
    }

    @Override
    public void setView(ILeftMenuView iLeftMenuView) {
        menuView = iLeftMenuView;
        if (!eventBus.isRegistered(this)) {
            eventBus.register(this);
        }
    }

    @Override
    public void destroyView() {
        eventBus.unregister(this);
        unsubscribeIfNotNull(compositeSubscription);
        menuView = null;
    }

    public void initialize() {
        menuView.setUserInfo(user);
        this.initializeZaloPay();
    }

    private void initializeZaloPay() {
        Subscription subscription = transactionRepository.initialize()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                        isInitiated = true;
                    }
                });
        compositeSubscription.add(subscription);
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

    private void getBalance() {
        Subscription subscription = balanceRepository.balance()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultSubscriber<Long>());

        compositeSubscription.add(subscription);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEventMainThread(ZaloProfileInfoEvent event) {
        //UPDATE USERINFO
        user.avatar = event.avatar;
        user.displayName = event.displayName;

        if (menuView != null) {
            menuView.setAvatar(event.avatar);
            menuView.setDisplayName(event.displayName);
        }

        eventBus.removeStickyEvent(ZaloProfileInfoEvent.class);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNetworkChange(NetworkChangeEvent event) {
        if (event.isOnline && !isInitiated) {
            this.getBalance();
            this.initializeZaloPay();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onZaloPayNameEventMainThread(ZaloPayNameEvent event) {
        if (menuView != null) {
            menuView.setZaloPayName(event.zaloPayName);
        }
    }

}
