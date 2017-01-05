package vn.com.vng.zalopay.fingerprint;

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

public class PasswordAuthenticationProvider implements AuthenticationProvider {

    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();
    private Stage mStage = Stage.PASSWORD;
    private AccountStore.Repository mAccountRepository;
    private Callback mCallback;
    private Context mContext;

    public PasswordAuthenticationProvider(Context context, AccountStore.Repository accountRepository, Callback callback) {
        this.mAccountRepository = accountRepository;
        this.mCallback = callback;
        this.mContext = context;
    }

    @Override
    public void setStage(Stage stage) {
        mStage = stage;
    }

    @Override
    public void verify(String password) {
        Subscription subscription = mAccountRepository.validatePin(password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ValidatePinSubscriber(password));
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

    private final class ValidatePinSubscriber extends DefaultSubscriber<Boolean> {

        public String password;

        ValidatePinSubscriber(String password) {
            this.password = password;
        }

        @Override
        public void onError(Throwable e) {
            if (mCallback != null) {
                mCallback.onError(e);
            }
        }

        @Override
        public void onCompleted() {
            if (mCallback != null) {
                mCallback.onAuthenticated(password);
            }
        }
    }
}
