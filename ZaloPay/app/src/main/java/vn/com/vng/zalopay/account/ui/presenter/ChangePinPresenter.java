package vn.com.vng.zalopay.account.ui.presenter;

import android.content.Context;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.view.IChangePinContainer;
import vn.com.vng.zalopay.account.ui.view.IChangePinVerifyView;
import vn.com.vng.zalopay.account.ui.view.IChangePinView;
import vn.com.vng.zalopay.data.NetworkError;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.data.exception.NetworkConnectionException;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;

import static vn.com.vng.zalopay.data.NetworkError.OTP_CHANGE_PASSWORF_WRONG;

/**
 * Created by AnhHieu on 8/25/16.
 */
public class ChangePinPresenter extends BaseUserPresenter
        implements IChangePinPresenter<IChangePinContainer, IChangePinView, IChangePinVerifyView> {

    private IChangePinContainer mChangePinContainer;
    private IChangePinView mChangePinView;
    private IChangePinVerifyView mChangePinVerifyView;

    private final int LIMIT_CHANGE_PASSWORD_ERROR = 3;

    private int numberError;

    private AccountStore.Repository mAccountRepository;
    private final Context mApplicationContext;
    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    @Inject
    public ChangePinPresenter(Context context, AccountStore.Repository accountRepository) {
        mApplicationContext = context;
        mAccountRepository = accountRepository;
    }

    @Override
    public void setChangePassView(IChangePinView iChangePinView) {
        numberError = 0;
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
        unsubscribeIfNotNull(mCompositeSubscription);
        mChangePinContainer = null;
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
    public void changePin(String oldPin, String newPin) {

        if (mChangePinView != null) {
            mChangePinView.showLoading();
        }

        Subscription subscription = mAccountRepository.recoveryPin(oldPin, newPin)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ChangePinSubscriber());
        mCompositeSubscription.add(subscription);
    }

    @Override
    public void verify(String otp) {
        if (mChangePinVerifyView != null) {
            mChangePinVerifyView.showLoading();
        }
        Subscription subscription = mAccountRepository.verifyRecoveryPin(otp)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new VerifySubscriber());
        mCompositeSubscription.add(subscription);

    }

    private void onChangePinSuccess() {
        mChangePinView.hideLoading();
        mChangePinContainer.nextPage();
    }


    private void onChangePinError(Throwable e) {
        mChangePinView.hideLoading();
        String message = ErrorMessageFactory.create(mApplicationContext, e);
        if (e instanceof NetworkConnectionException) {
            mChangePinView.showWarning(mChangePinView.getContext().getString(R.string.exception_no_connection_try_again));
        } else if (e instanceof BodyException) {
            mChangePinView.showError(message);
            int code = ((BodyException) e).errorCode;
            if (code == NetworkError.OLD_PIN_NOT_MATCH) {
                if (numberError == LIMIT_CHANGE_PASSWORD_ERROR) {
                    mChangePinContainer.onChangePinOverLimit();
                } else {
                    mChangePinView.requestFocusOldPin();
                }

                numberError++;
            }
        } else {
            mChangePinView.showError(message);
        }
    }

    private void onVerifyOTPError(Throwable e) {
        if (mChangePinVerifyView != null) {
            mChangePinVerifyView.hideLoading();
        }
        if (e instanceof NetworkConnectionException) {
            if (mChangePinView != null) {
                mChangePinView.showWarning(mApplicationContext.getString(R.string.exception_no_connection_try_again));
            }
        } else {
            if (e instanceof BodyException) {
                if (((BodyException) e).errorCode == OTP_CHANGE_PASSWORF_WRONG) {
                    ZPAnalytics.trackEvent(ZPEvents.OTP_CHANGEPASSWORD_INPUTWRONG);
                }
            }

            String message = ErrorMessageFactory.create(mApplicationContext, e);
            if (mChangePinVerifyView != null) {
                mChangePinVerifyView.showError(message);
            }
        }
    }

    private void onVerifyOTPSuccess() {
        ZPAnalytics.trackEvent(ZPEvents.OTP_CHANGEPASSWORD_INPUTOK);
        mChangePinVerifyView.hideLoading();
        mChangePinContainer.onVerifySuccess();
    }

    private class ChangePinSubscriber extends DefaultSubscriber<BaseResponse> {
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
