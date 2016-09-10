package vn.com.vng.zalopay.ui.presenter;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.ui.view.IPinProfileView;

/**
 * Created by AnhHieu on 9/10/16.
 * *
 */
public class PinProfilePresenter extends BaseUserPresenter implements IPresenter<IPinProfileView> {

    @Inject
    public PinProfilePresenter() {
    }

    IPinProfileView pinProfileView;
    CompositeSubscription compositeSubscription = new CompositeSubscription();

    @Override
    public void setView(IPinProfileView iPinProfileView) {
        pinProfileView = iPinProfileView;
    }

    @Override
    public void destroyView() {
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
        Subscription subscription = accountRepository.validatePin(pin)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ValidatePinSubscriber());
        compositeSubscription.add(subscription);
    }

    private class ValidatePinSubscriber extends DefaultSubscriber<Boolean> {

        @Override
        public void onError(Throwable e) {
            Timber.d(e,"onError");
            hideLoadingView();
            pinProfileView.setError(ErrorMessageFactory.create(applicationContext, e));
        }

        @Override
        public void onCompleted() {
            Timber.d("onCompleted");
            hideLoadingView();
            pinProfileView.onPinSuccess();
        }
    }
}
