package vn.com.vng.zalopay.transfer.ui;

import android.content.Context;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.Person;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.vng.zalopay.ui.view.ITransferMoneyView;

/**
 * Created by AnhHieu on 9/11/16.
 * *
 */
public class TransferViaZaloPayNamePresenter extends AbstractPresenter<ITransferMoneyView> {
    private AccountStore.Repository mAccountRepository;
    private Context mApplicationContext;

    @Inject
    TransferViaZaloPayNamePresenter(AccountStore.Repository accountRepository,
                                    Context applicationContext) {
        this.mAccountRepository = accountRepository;
        this.mApplicationContext = applicationContext;
    }

    void getUserInfo(String zpName) {
        showLoadingView();
        Subscription subscription = mAccountRepository.getUserInfoByZaloPayName(zpName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new UserInfoSubscriber(zpName));
        mSubscription.add(subscription);
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

        UserInfoSubscriber(String zaloPayName) {
            this.zaloPayName = zaloPayName;
        }

        @Override
        public void onError(Throwable e) {
            Timber.d(e, "onError");
            if (ResponseHelper.shouldIgnoreError(e)) {
                return;
            }
            
            hideLoadingView();
            String message = ErrorMessageFactory.create(mApplicationContext, e);
            mView.showError(message);
        }

        @Override
        public void onNext(Person person) {
            hideLoadingView();
            mView.onGetProfileSuccess(person, zaloPayName);
        }
    }
}
