package vn.com.vng.zalopay.account.ui.presenter;

import android.content.Context;
import android.text.TextUtils;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.account.ui.view.IChangePinContainer;
import vn.com.vng.zalopay.account.ui.view.IChangePinVerifyView;
import vn.com.vng.zalopay.account.ui.view.IChangePinView;
import vn.com.vng.zalopay.authentication.KeyTools;
import vn.com.vng.zalopay.data.NetworkError;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.data.exception.NetworkConnectionException;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;

import static vn.com.vng.zalopay.data.NetworkError.OTP_CHANGE_PASSWORF_WRONG;

/**
 * Created by AnhHieu on 8/25/16.
 * *
 */
public class ChangePinPresenter extends AbstractPresenter<IChangePinContainer>
        implements IChangePinPresenter<IChangePinContainer, IChangePinView, IChangePinVerifyView> {

    private IChangePinView mChangePinView;
    private IChangePinVerifyView mChangePinVerifyView;

    private final int LIMIT_CHANGE_PASSWORD_ERROR = 3;

    private int numberError;

    private AccountStore.Repository mAccountRepository;
    private final Context mApplicationContext;
    private CompositeSubscription mSubscription = new CompositeSubscription();

    private String mNewPassword;

    KeyTools mKeyTools;

    public ChangePinPresenter(Context context, AccountStore.Repository accountRepository, KeyTools keytool) {
        mApplicationContext = context;
        mAccountRepository = accountRepository;
        mKeyTools = keytool;
    }

    @Override
    public void attachView(IChangePinContainer iChangePinContainer) {
        super.attachView(iChangePinContainer);
        initPagerContent();
    }

    private void initPagerContent() {
        Subscription subscription = mAccountRepository.getChangePinState()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultSubscriber<Boolean>() {
                    @Override
                    public void onNext(Boolean isReceivedOtp) {
                        if (isReceivedOtp == null || !isReceivedOtp) {
                            mView.initPagerContent(0);
                        } else {
                            mView.initPagerContent(1);
                        }
                    }
                });
        mSubscription.add(subscription);
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
    public void changePin(String oldPin, String newPin) {
        Subscription subscription = mAccountRepository.changePassword(oldPin, newPin)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ChangePinSubscriber());
        mSubscription.add(subscription);
    }

    @Override
    public void verify(String otp) {
        Subscription subscription = mAccountRepository.verifyChangePassword(otp)
                .doOnNext(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (!TextUtils.isEmpty(mNewPassword) && mKeyTools.isHavePassword()) {
                            boolean encrypt = mKeyTools.encrypt(mNewPassword);
                            Timber.d("encrypt result %s", encrypt);
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new VerifySubscriber());
        mSubscription.add(subscription);
    }

    private void onChangePinSuccess(String newPassword) {
        this.mNewPassword = newPassword;

        if (mChangePinView != null) {
            mChangePinView.hideLoading();
        }

        if (mView != null) {
            mView.nextPage();
        }
    }

    private void onChangePinError(Throwable e) {
        if (mChangePinView == null) {
            return;
        }

        mChangePinView.hideLoading();
        String message = ErrorMessageFactory.create(mApplicationContext, e);
        if (e instanceof NetworkConnectionException) {
            mChangePinView.showNetworkErrorDialog();
        } else if (e instanceof BodyException) {
            final int code = ((BodyException) e).errorCode;
            mChangePinView.showError(message, new ZPWOnEventConfirmDialogListener() {
                @Override
                public void onCancelEvent() {
                    onDialogDismiss();
                }

                @Override
                public void onOKevent() {
                    onDialogDismiss();
                }

                private void onDialogDismiss() {
                    if (code == NetworkError.OLD_PIN_NOT_MATCH) {
                        if (numberError == LIMIT_CHANGE_PASSWORD_ERROR) {
                            mView.onChangePinOverLimit();
                        } else {
                            mChangePinView.requestFocusOldPin();
                        }

                        numberError++;
                    }
                }
            });
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
                mChangePinView.showNetworkErrorDialog();
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
        mView.onVerifySuccess();
    }

    private class ChangePinSubscriber extends DefaultSubscriber<String> {

        @Override
        public void onStart() {
            if (mChangePinView != null) {
                mChangePinView.showLoading();
            }
        }

        @Override
        public void onNext(String hashPassword) {
            ChangePinPresenter.this.onChangePinSuccess(hashPassword);
        }

        @Override
        public void onError(Throwable e) {
            if (ResponseHelper.shouldIgnoreError(e)) {
                return;
            }
            ChangePinPresenter.this.onChangePinError(e);
        }
    }

    private final class VerifySubscriber extends DefaultSubscriber<Boolean> {

        @Override
        public void onStart() {
            if (mChangePinVerifyView != null) {
                mChangePinVerifyView.showLoading();
            }
        }

        @Override
        public void onNext(Boolean aBoolean) {
            ChangePinPresenter.this.onVerifyOTPSuccess();
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
