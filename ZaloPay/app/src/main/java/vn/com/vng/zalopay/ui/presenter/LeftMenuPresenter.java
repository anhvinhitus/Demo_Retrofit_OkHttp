package vn.com.vng.zalopay.ui.presenter;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Singleton;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.data.eventbus.ChangeBalanceEvent;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
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

    public LeftMenuPresenter(User user) {
        this.user = user;
    }

    @Override
    public void setView(ILeftMenuView iLeftMenuView) {
        menuView = iLeftMenuView;
        menuView.setUserInfo(user);
        eventBus.register(this);
    }

    @Override
    public void destroyView() {
        eventBus.unregister(this);
        unsubscribeIfNotNull(compositeSubscription);
        menuView = null;
    }

    public void initialize() {
        this.getBalance();
        this.initializeAppConfig();
        this.initializeZaloPay();
    }

    public void initializeAppConfig() {
        appConfigRepository.initialize()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
    }

    public void initializeZaloPay() {
        zaloPayRepository.initialize()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
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

    public void getBalance() {
        Subscription subscription = zaloPayRepository.balance()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BalanceSubscriber());

        compositeSubscription.add(subscription);
    }

    private final void onGetBalanceSuccess(Long balance) {
        Timber.tag(TAG).d("onGetBalanceSuccess %s", balance);
        menuView.setBalance(balance);
    }

    private class BalanceSubscriber extends DefaultSubscriber<Long> {
        public BalanceSubscriber() {
        }

        @Override
        public void onCompleted() {
            super.onCompleted();
        }

        @Override
        public void onError(Throwable e) {
            Timber.tag(TAG).e(e, " exception ");
        }

        @Override
        public void onNext(Long aLong) {
            LeftMenuPresenter.this.onGetBalanceSuccess(aLong);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEventMainThread(ZaloProfileInfoEvent event) {
        Timber.tag(TAG).d("avatar %s displayName %s", event.avatar, event.displayName);

        //UPDATE USERINFO
        user.avatar = event.avatar;
        user.dname = event.displayName;

        if (menuView != null) {
            menuView.setAvatar(event.avatar);
            menuView.setDisplayName(event.displayName);
        }

        eventBus.removeStickyEvent(ZaloProfileInfoEvent.class);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ChangeBalanceEvent event) {
        //Timber.d("event bus test %s; ThreadName:%s", event.balance, Thread.currentThread().getName());
        if (menuView != null) {
            menuView.setBalance(event.balance);
        }
    }
}
