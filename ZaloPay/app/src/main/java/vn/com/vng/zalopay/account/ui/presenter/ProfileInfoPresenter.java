package vn.com.vng.zalopay.account.ui.presenter;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.account.ui.view.IProfileInfoView;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.repository.BalanceRepository;
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
    }

    @Override
    public void destroyView() {
        unsubscribeIfNotNull(compositeSubscription);
        mView = null;
    }

    @Override
    public void resume() {
        mView.updateUserInfo(userConfig.getCurrentUser());
        mView.updateBannerView("http://vn-live.slatic.net/cms/landing-page-banner/bank/1200x250-123pay-birthday.jpg");
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
        BalanceRepository repository = AndroidApplication.instance().getUserComponent().balanceRepository();
        Subscription subscription = repository.balance()
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

            Timber.tag(TAG).e(e, " exception ");
        }

        @Override
        public void onNext(Long aLong) {
            ProfileInfoPresenter.this.onGetBalanceSuccess(aLong);
        }
    }

    public void sigoutAndCleanData() {
        AndroidApplication.instance().getAppComponent().applicationSession().clearUserSession();
    }
}
