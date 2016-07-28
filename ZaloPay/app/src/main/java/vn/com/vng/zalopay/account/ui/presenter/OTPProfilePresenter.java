package vn.com.vng.zalopay.account.ui.presenter;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
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
    private Subscription subscriptionLogin;

    @Override
    public void setView(IOTPProfileView iProfileView) {
        mView = iProfileView;
    }

    @Override
    public void destroyView() {
        hideLoading();
        this.mView = null;
    }

    private void unsubscribe() {
        unsubscribeIfNotNull(subscriptionLogin);
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

    private void onVerifyOTPSucess() {
        hideLoading();
        mView.confirmOTPSuccess();
    }

    public void verifyOtp(String otp) {
        showLoading();
        subscriptionLogin = accountRepository.verifyOTPProfile(otp)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new VerifyOTPProfileSubscriber());
    }

    private final class VerifyOTPProfileSubscriber extends DefaultSubscriber<Boolean> {
        public VerifyOTPProfileSubscriber() {
        }

        @Override
        public void onNext(Boolean permissions) {
            OTPProfilePresenter.this.onVerifyOTPSucess();
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
