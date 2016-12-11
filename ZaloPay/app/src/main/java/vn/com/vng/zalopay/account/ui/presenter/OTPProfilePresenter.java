package vn.com.vng.zalopay.account.ui.presenter;

import android.content.Context;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.account.ui.view.IOTPProfileView;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by longlv on 25/05/2016.
 */
public class OTPProfilePresenter extends AbstractPresenter<IOTPProfileView> {
    private AccountStore.Repository mAccountRepository;
    private Context applicationContext;

    @Inject
    public OTPProfilePresenter(AccountStore.Repository accountRepository, Context applicationContext) {
        this.mAccountRepository = accountRepository;
        this.applicationContext = applicationContext;
    }

    @Override
    public void detachView() {
        hideLoading();
        super.detachView();
    }

    private void onConfirmOTPError(Throwable e) {
        hideLoading();
        if (mView != null) {
            ZPAnalytics.trackEvent(ZPEvents.OTP_LEVEL2_INPUTWRONG);
            mView.showError(ErrorMessageFactory.create(applicationContext, e));
        }
    }

    private void onVerifyOTPSuccess() {
        ZPAnalytics.trackEvent(ZPEvents.OTP_LEVEL2_INPUTOK);
        hideLoading();
        mView.confirmOTPSuccess();
    }

    public void verifyOtp(String otp) {
        showLoading();
        Subscription subscription = mAccountRepository.verifyOTPProfile(otp)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new VerifyOTPProfileSubscriber());

        mSubscription.add(subscription);
    }

    private final class VerifyOTPProfileSubscriber extends DefaultSubscriber<Boolean> {

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

            Timber.d(e, "onError " + e);
            OTPProfilePresenter.this.onConfirmOTPError(e);
        }
    }

    public void showLoading() {
        if (mView != null) {
            mView.showLoading();
        }
    }

    public void hideLoading() {
        if (mView != null) {
            mView.hideLoading();
        }
    }

    public void showRetry() {
        if (mView != null) {
            mView.showRetry();
        }
    }

    public void hideRetry() {
        if (mView != null) {
            mView.hideRetry();
        }
    }
}
