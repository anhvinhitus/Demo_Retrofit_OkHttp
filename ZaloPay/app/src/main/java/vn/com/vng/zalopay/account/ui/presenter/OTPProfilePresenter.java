package vn.com.vng.zalopay.account.ui.presenter;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.account.ui.view.IOTPProfileView;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;

/**
 * Created by longlv on 25/05/2016.
 */
public class OTPProfilePresenter extends BaseUserPresenter implements IPresenter<IOTPProfileView> {

    IOTPProfileView mView;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    @Override
    public void setView(IOTPProfileView iProfileView) {
        mView = iProfileView;
    }

    @Override
    public void destroyView() {
        hideLoading();
        unsubscribe();
        this.mView = null;
    }

    private void unsubscribe() {
        unsubscribeIfNotNull(compositeSubscription);
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

    private void onConfirmOTPError(Throwable e) {
        hideLoading();
        mView.confirmOTPError();
    }

    private void onVerifyOTPSuccess() {
        hideLoading();
        mView.confirmOTPSuccess();
    }

    public void verifyOtp(String otp) {
        showLoading();
        Subscription subscription = accountRepository.verifyOTPProfile(otp)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new VerifyOTPProfileSubscriber());

        compositeSubscription.add(subscription);
    }

    private final class VerifyOTPProfileSubscriber extends DefaultSubscriber<Boolean> {
        public VerifyOTPProfileSubscriber() {
        }

        @Override
        public void onNext(Boolean permissions) {
            OTPProfilePresenter.this.onVerifyOTPSuccess();
        }

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            if (ResponseHelper.shouldIgnoreError(e)) {
                return;
            }

            Timber.e(e, "onError " + e);
            OTPProfilePresenter.this.onConfirmOTPError(e);
        }
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
