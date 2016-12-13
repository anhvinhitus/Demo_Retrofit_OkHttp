package vn.com.vng.zalopay.ui.presenter;

import android.content.Context;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.ui.view.IPinProfileView;

import static vn.com.vng.zalopay.data.NetworkError.INCORRECT_PIN;

/**
 * Created by AnhHieu on 9/10/16.
 * *
 */
public class PinProfilePresenter extends AbstractPresenter<IPinProfileView> {

    @Inject
    public PinProfilePresenter(AccountStore.Repository accountRepository, Context applicationContext) {
        this.mAccountRepository = accountRepository;
        this.mApplicationContext = applicationContext;
    }

    private AccountStore.Repository mAccountRepository;
    private Context mApplicationContext;

    private void showLoadingView() {
        if (mView != null) {
            mView.showLoading();
        }
    }

    private void hideLoadingView() {
        if (mView != null) {
            mView.hideLoading();
        }
    }

    public void validatePin(String pin) {
        showLoadingView();
        Subscription subscription = mAccountRepository.validatePin(pin)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ValidatePinSubscriber());
        mSubscription.add(subscription);
    }

    private class ValidatePinSubscriber extends DefaultSubscriber<Boolean> {

        @Override
        public void onError(Throwable e) {
            Timber.d(e, "valid pin");
            if (ResponseHelper.shouldIgnoreError(e)) {
                return;
            }

            if (mView == null) {
                return;
            }
            hideLoadingView();
            mView.setError(ErrorMessageFactory.create(mApplicationContext, e));
            mView.clearPin();
            if (e instanceof BodyException) {
                if (((BodyException) e).errorCode == INCORRECT_PIN) {
                    mView.showKeyboard();
                }
            }
        }

        @Override
        public void onCompleted() {
            Timber.d("onCompleted");
            hideLoadingView();
            mView.onPinSuccess();
        }
    }
}
