package vn.com.vng.zalopay.ui.presenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Singleton;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.cache.SqlZaloPayScope;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.interactor.event.ChangeBalanceEvent;
import vn.com.vng.zalopay.interactor.event.ZaloProfileInfoEvent;
import vn.com.vng.zalopay.ui.view.ILeftMenuView;

/**
 * Created by AnhHieu on 5/11/16.
 */

@Singleton
public class LeftMenuPresenter extends BaseUserPresenter implements Presenter<ILeftMenuView> {
    private ILeftMenuView menuView;

    private EventBus eventBus;
    private CompositeSubscription compositeSubscription = new CompositeSubscription();
    private User user;
    private SqlZaloPayScope sqlZaloPayScope;

    public LeftMenuPresenter(EventBus eventBus, User user, SqlZaloPayScope sqlZaloPayScope) {
        this.eventBus = eventBus;
        this.user = user;
        this.sqlZaloPayScope = sqlZaloPayScope;
    }

    @Override
    public void setView(ILeftMenuView iLeftMenuView) {
        eventBus.register(this);
        menuView = iLeftMenuView;
    }

    @Override
    public void destroyView() {
        eventBus.unregister(this);
        unsubscribeIfNotNull(compositeSubscription);
        menuView = null;
    }

    public void initialize() {
        menuView.setUserInfo(user);
    }

    @Override
    public void resume() {
        long balance = sqlZaloPayScope.getDataManifest(Constants.MANIF_BALANCE, 0);
        menuView.setBalance(balance);
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

    @Subscribe
    public void onEventMainThread(ZaloProfileInfoEvent event) {

        Timber.tag(TAG).d("avatar %s displayName %s", event.avatar, event.displayName);
        menuView.setAvatar(event.avatar);
        menuView.setDisplayName(event.displayName);
    }

    @Subscribe
    public void onEventMainThread(ChangeBalanceEvent event) {
        menuView.setBalance(event.balance);
    }
}
