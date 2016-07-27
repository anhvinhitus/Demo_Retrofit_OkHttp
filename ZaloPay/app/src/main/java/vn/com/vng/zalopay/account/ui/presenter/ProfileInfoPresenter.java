package vn.com.vng.zalopay.account.ui.presenter;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.account.ui.view.IProfileInfoView;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.interactor.event.ZaloProfileInfoEvent;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;

/**
 * Created by longlv on 19/05/2016.
 */
public class ProfileInfoPresenter extends BaseUserPresenter implements IPresenter<IProfileInfoView> {

    IProfileInfoView mView;
    private UserConfig mUserConfig;
    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    public ProfileInfoPresenter(UserConfig userConfig) {
        mUserConfig = userConfig;
    }

    @Override
    public void setView(IProfileInfoView iProfileInfoView) {
        mView = iProfileInfoView;
        eventBus.register(this);
    }

    @Override
    public void destroyView() {
        eventBus.unregister(this);
        unsubscribeIfNotNull(compositeSubscription);
        mView = null;
    }

    @Override
    public void resume() {
        mView.updateUserInfo(userConfig.getCurrentUser());
        getBalance();
        checkShowOrHideChangePinView();
    }

    private void checkShowOrHideChangePinView() {
        if (mUserConfig == null || mUserConfig.getCurrentUser() == null) {
            mView.showHideChangePinView(false);
        }
        if (mUserConfig.getCurrentUser().profilelevel < 2) {
            mView.showHideChangePinView(false);
        } else {
            mView.showHideChangePinView(true);
        }
    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {
        destroyView();
    }

    public void showLoading() {
        mView.showLoading();
    }

    public void hideLoading() {
        mView.hideLoading();
    }

    public void showRetry() {
        mView.showRetry();
    }

    public void hideRetry() {
        mView.hideRetry();
    }

    private void getBalance() {
        Subscription subscription = balanceRepository.balance()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BalanceSubscriber());

        compositeSubscription.add(subscription);
    }

    private void onGetBalanceSuccess(Long aLong) {
        mView.updateBalance(aLong);
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
            if (ResponseHelper.shouldIgnoreError(e)) {
                return;
            }

            Timber.e(e, " exception ");
        }

        @Override
        public void onNext(Long aLong) {
            ProfileInfoPresenter.this.onGetBalanceSuccess(aLong);
        }
    }

    public void signOutAndCleanData() {
        AndroidApplication.instance().getAppComponent().applicationSession().clearUserSession();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEventMainThread(ZaloProfileInfoEvent event) {
        Timber.d("onEventMainThread event %s", event);
        //UPDATE USERINFO
        if (mView != null) {
            mView.updateUserInfo(userConfig.getCurrentUser());
        }

        eventBus.removeStickyEvent(ZaloProfileInfoEvent.class);
    }
}
