package vn.com.vng.zalopay.authentication;

import android.content.Context;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;

/**
 * Created by hieuvm on 1/4/17.
 */

class PasswordAuthenticationProvider implements AuthenticationProvider {

    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();
    private AccountStore.Repository mAccountRepository;
    private Callback mCallback;
    private Context mContext;

    PasswordAuthenticationProvider(Context context, AccountStore.Repository accountRepository, Callback callback) {
        this.mAccountRepository = accountRepository;
        this.mCallback = callback;
        this.mContext = context;
    }

    @Override
    public void verify(String password) {
        Subscription subscription = mAccountRepository.validatePin(password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ValidatePinSubscriber());
        mCompositeSubscription.add(subscription);
    }

    @Override
    public void startVerify() {
        //empty
    }

    @Override
    public void stopVerify() {
        if (mCompositeSubscription != null) {
            mCompositeSubscription.clear();
        }
    }

    @Override
    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    private final class ValidatePinSubscriber extends DefaultSubscriber<String> {

        @Override
        public void onError(Throwable e) {
            if (mCallback != null) {
                mCallback.onError(e);
            }
        }

        @Override
        public void onNext(String hashPassword) {
            if (mCallback != null) {
                mCallback.onAuthenticated(hashPassword);
            }
        }
    }
}
