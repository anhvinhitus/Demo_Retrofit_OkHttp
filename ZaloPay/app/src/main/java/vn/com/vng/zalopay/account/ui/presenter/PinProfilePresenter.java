package vn.com.vng.zalopay.account.ui.presenter;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.account.ui.view.IPinProfileView;
import vn.com.vng.zalopay.data.NetworkError;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by longlv on 25/05/2016.
 *
 */
public class PinProfilePresenter extends BaseUserPresenter implements IPresenter<IPinProfileView> {

    private IPinProfileView mView;

    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();
    private AccountStore.Repository mAccountRepository;

    @Inject
    public PinProfilePresenter(AccountStore.Repository accountRepository) {
        this.mAccountRepository = accountRepository;
    }

    @Override
    public void setView(IPinProfileView iProfileView) {
        mView = iProfileView;
    }

    @Override
    public void destroyView() {
        hideLoading();
        this.mView = null;
    }

    private void unsubscribe() {
        unsubscribeIfNotNull(mCompositeSubscription);
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {
        this.unsubscribe();
    }

    public void updateProfile(String pin, String phone) {
        showLoading();
        Subscription subscriptionLogin = mAccountRepository.updateUserProfileLevel2(pin, phone)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new UpdateProfileSubscriber(phone));
        mCompositeSubscription.add(subscriptionLogin);
    }

    private final class UpdateProfileSubscriber extends DefaultSubscriber<Boolean> {
        private String phone;

        UpdateProfileSubscriber(String phone) {
            this.phone = phone;
        }

        @Override
        public void onNext(Boolean result) {
            Timber.d("updateProfile success " + result);
            PinProfilePresenter.this.onUpdateProfileSuccess(phone);
        }

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            if (ResponseHelper.shouldIgnoreError(e)) {
                return;
            }

            Timber.e(e, "update Profile Subscriber onError [%s]", e.getMessage());
            if (e instanceof BodyException) {
                if (((BodyException)e).errorCode == NetworkError.USER_EXISTED) {
                    ZPAnalytics.trackEvent(ZPEvents.UPDATEPROFILE2_ZPN_INUSED);
                }
                PinProfilePresenter.this.onUpdateProfileError(e.getMessage());
            } else {
                PinProfilePresenter.this.onUpdateProfileError("Cập nhật thông tin người dùng thất bại.");
            }
        }
    }

    private void onUpdateProfileError(String error) {
        hideLoading();
        mView.showError(error);
    }

    private void onUpdateProfileSuccess(String phone) {
        hideLoading();
        mView.updateProfileSuccess(phone);
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
