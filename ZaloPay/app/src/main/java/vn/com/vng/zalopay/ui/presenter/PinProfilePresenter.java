package vn.com.vng.zalopay.ui.presenter;

import android.content.Context;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.ui.view.IPinProfileView;

/**
 * Created by AnhHieu on 9/10/16.
 * *
 */
public class PinProfilePresenter extends BaseUserPresenter implements IPresenter<IPinProfileView> {

    @Inject
    public PinProfilePresenter(AccountStore.Repository accountRepository, Context applicationContext) {
        this.mAccountRepository = accountRepository;
        this.mApplicationContext = applicationContext;
    }

    IPinProfileView pinProfileView;
    CompositeSubscription compositeSubscription = new CompositeSubscription();
    private AccountStore.Repository mAccountRepository;
    private Context mApplicationContext;

    @Override
    public void attachView(IPinProfileView iPinProfileView) {
        pinProfileView = iPinProfileView;
    }

    @Override
    public void detachView() {
        unsubscribeIfNotNull(compositeSubscription);
        pinProfileView = null;
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

    private void showLoadingView() {
        if (pinProfileView != null) {
            pinProfileView.showLoading();
        }
    }

    private void hideLoadingView() {
        if (pinProfileView != null) {
            pinProfileView.hideLoading();
        }
    }

    public void validatePin(String pin) {
        showLoadingView();
        Subscription subscription = mAccountRepository.validatePin(pin)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ValidatePinSubscriber());
        compositeSubscription.add(subscription);
    }

    private class ValidatePinSubscriber extends DefaultSubscriber<Boolean> {

        @Override
        public void onError(Throwable e) {
            Timber.d(e, "valid pin");
            if (ResponseHelper.shouldIgnoreError(e)) {
                return;
            }

            hideLoadingView();
            if (pinProfileView != null) {
                pinProfileView.setError(ErrorMessageFactory.create(mApplicationContext, e));
                pinProfileView.clearPin();
            }
        }

        @Override
        public void onCompleted() {
            Timber.d("onCompleted");
            hideLoadingView();
            pinProfileView.onPinSuccess();
        }
    }
}
