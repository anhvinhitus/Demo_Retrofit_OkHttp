package vn.com.vng.zalopay.ui.presenter;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.Person;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.ui.view.ITransferMoneyView;

/**
 * Created by AnhHieu on 9/11/16.
 * *
 */
public class TransferMoneyViaAccountNamePresenter extends BaseUserPresenter implements IPresenter<ITransferMoneyView> {

    ITransferMoneyView mView;
    CompositeSubscription compositeSubscription = new CompositeSubscription();

    @Override
    public void setView(ITransferMoneyView iTransferMoneyView) {
        mView = iTransferMoneyView;
    }

    @Override
    public void destroyView() {
        unsubscribeIfNotNull(compositeSubscription);
        mView = null;
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

    @Inject
    public TransferMoneyViaAccountNamePresenter() {
    }

    public void getUserInfo(String zpName) {
        showLoadingView();
        Subscription subscription = accountRepository.getUserInfoByZaloPayName(zpName)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new UserInfoSubscriber(zpName));
        compositeSubscription.add(subscription);
    }

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

    private class UserInfoSubscriber extends DefaultSubscriber<Person> {

        String zaloPayName;

        public UserInfoSubscriber(String zaloPayName) {
            this.zaloPayName = zaloPayName;
        }

        @Override
        public void onError(Throwable e) {
            Timber.d(e, "onError");
            if (ResponseHelper.shouldIgnoreError(e)) {
                return;
            }
            
            hideLoadingView();
            String message = ErrorMessageFactory.create(applicationContext, e);
            mView.showError(message);
        }

        @Override
        public void onNext(Person person) {
            hideLoadingView();
            mView.onGetProfileSuccess(person, zaloPayName);
        }
    }
}
