package vn.com.vng.zalopay.account.ui.presenter;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.account.ui.view.IOTPRecoveryPinView;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;

/**
 * Created by longlv on 25/05/2016.
 */
public class OTPRecoveryPinPresenter extends BaseUserPresenter implements IPresenter<IOTPRecoveryPinView> {

    IOTPRecoveryPinView mView;
    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    @Override
    public void setView(IOTPRecoveryPinView iProfileView) {
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
    }

    private void onConfirmOTPError(Throwable e) {
        hideLoading();
        String message = ErrorMessageFactory.create(applicationContext, e);
        mView.confirmOTPError(message);

    }


    private void onVerifyOTPSuccess() {
        hideLoading();
        mView.confirmOTPSuccess();
    }

    public void verifyOtp(String otp) {
        showLoading();
        Subscription subscription = accountRepository.verifyRecoveryPin(otp)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new VerifyOTPRecoveryPinSubscriber());
        compositeSubscription.add(subscription);
    }

    private final class VerifyOTPRecoveryPinSubscriber extends DefaultSubscriber<BaseResponse> {
        public VerifyOTPRecoveryPinSubscriber() {
        }

        @Override
        public void onNext(BaseResponse baseResponse) {
            OTPRecoveryPinPresenter.this.onVerifyOTPSuccess();
        }

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            if (ResponseHelper.shouldIgnoreError(e)) {
                return;
            }

            OTPRecoveryPinPresenter.this.onConfirmOTPError(e);
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
