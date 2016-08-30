package vn.com.vng.zalopay.account.ui.presenter;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import vn.com.vng.zalopay.account.ui.view.IChangePinContainer;
import vn.com.vng.zalopay.account.ui.view.IChangePinVerifyView;
import vn.com.vng.zalopay.account.ui.view.IChangePinView;
import vn.com.vng.zalopay.data.NetworkError;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;

/**
 * Created by AnhHieu on 8/25/16.
 */
public class ChangePinPresenter extends BaseUserPresenter implements IChangePinPresenter<IChangePinContainer, IChangePinView, IChangePinVerifyView> {

    IChangePinContainer mChangePinContainer;
    IChangePinView mChangePinView;
    IChangePinVerifyView mChangePinVerifyView;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    @Override
    public void setChangePassView(IChangePinView iChangePinView) {
        mChangePinView = iChangePinView;
    }

    @Override
    public void destroyChangePassView() {
        mChangePinView = null;
    }

    @Override
    public void setVerifyView(IChangePinVerifyView iChangePinVerifyView) {
        mChangePinVerifyView = iChangePinVerifyView;
    }

    @Override
    public void destroyVerifyView() {
        mChangePinVerifyView = null;
    }

    @Override
    public void setView(IChangePinContainer iChangePinContainer) {
        mChangePinContainer = iChangePinContainer;
    }

    @Override
    public void destroyView() {
        mChangePinContainer = null;
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

    @Override
    public void pinValid(boolean valid) {
        if (mChangePinContainer != null) {
            mChangePinContainer.onPinValid(valid);
        }
    }

    @Override
    public void changePin(String oldPin, String newPin) {

        if (mChangePinView != null) {
            mChangePinView.showLoading();
        }

        Subscription subscription = accountRepository.recoveryPin(oldPin, newPin)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ChangePinSubscriber());
        compositeSubscription.add(subscription);
    }

    @Override
    public void verify(String otp) {
        if (mChangePinVerifyView != null) {
            mChangePinVerifyView.showLoading();
        }
        Subscription subscription = accountRepository.verifyRecoveryPin(otp)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new VerifySubscriber());
        compositeSubscription.add(subscription);

    }

    @Override
    public void checkPinValidAndSubmit() {
        if (mChangePinView != null) {
            mChangePinView.checkPinValidAndSubmit();
        }
    }

    @Override
    public void checkOtpValidAndSubmit() {
        if (mChangePinVerifyView != null) {
            mChangePinVerifyView.checkOtpValidAndSubmit();
        }
    }

    private void onChangePinSuccess() {
        mChangePinView.hideLoading();
        mChangePinContainer.nextPage();
    }

    private void onChangePinError(Throwable e) {
        mChangePinView.hideLoading();
        String message = ErrorMessageFactory.create(applicationContext, e);
        mChangePinView.showError(message);
        if (e instanceof BodyException) {
            int code = ((BodyException)e).errorCode;
            if (code == NetworkError.OLD_PIN_NOT_MATCH) {
                mChangePinView.requestFocusOldPin();
            }
        }
    }

    private void onVerifyOTPError(Throwable e) {
        mChangePinVerifyView.hideLoading();
        String message = ErrorMessageFactory.create(applicationContext, e);
        mChangePinVerifyView.showError(message);
    }


    private void onVerifyOTPSuccess() {
        mChangePinVerifyView.hideLoading();
        mChangePinContainer.onVerifySuccess();
    }


    private class ChangePinSubscriber extends DefaultSubscriber<BaseResponse> {
        public ChangePinSubscriber() {
        }

        @Override
        public void onNext(BaseResponse baseResponse) {
            ChangePinPresenter.this.onChangePinSuccess();
        }

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            if (ResponseHelper.shouldIgnoreError(e)) {
                return;
            }
            ChangePinPresenter.this.onChangePinError(e);
        }
    }

    private final class VerifySubscriber extends DefaultSubscriber<BaseResponse> {
        public VerifySubscriber() {
        }

        @Override
        public void onNext(BaseResponse baseResponse) {
            ChangePinPresenter.this.onVerifyOTPSuccess();
        }

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            if (ResponseHelper.shouldIgnoreError(e)) {
                return;
            }

            ChangePinPresenter.this.onVerifyOTPError(e);
        }
    }
}
