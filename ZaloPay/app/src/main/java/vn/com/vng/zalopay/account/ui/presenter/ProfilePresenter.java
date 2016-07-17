package vn.com.vng.zalopay.account.ui.presenter;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.account.ui.view.IProfileView;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;

/**
 * Created by longlv on 25/05/2016.
 */
public class ProfilePresenter extends BaseUserPresenter implements IPresenter<IProfileView> {

    IProfileView mView;
    private UserConfig mUserConfig;
    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    public ProfilePresenter(UserConfig userConfig) {
        mUserConfig = userConfig;
    }

    @Override
    public void setView(IProfileView iProfileView) {
        mView = iProfileView;
    }

    @Override
    public void destroyView() {
        unsubscribeIfNotNull(compositeSubscription);
        mView = null;
    }

    @Override
    public void resume() {
        getBalance();
        mView.updateUserInfo(userConfig.getCurrentUser());
    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {

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

            Timber.tag(TAG).e(e, " exception ");
        }

        @Override
        public void onNext(Long aLong) {
            ProfilePresenter.this.onGetBalanceSuccess(aLong);
        }
    }

    private void getBalance() {
        BalanceStore.Repository repository = AndroidApplication.instance().getUserComponent().balanceRepository();
        Subscription subscription = repository.balance()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BalanceSubscriber());

        compositeSubscription.add(subscription);
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
}
