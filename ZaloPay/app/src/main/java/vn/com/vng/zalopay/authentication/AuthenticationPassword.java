package vn.com.vng.zalopay.authentication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.zalopay.ui.widget.password.interfaces.IPasswordCallBack;
import com.zalopay.ui.widget.password.managers.PasswordManager;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.authentication.secret.KeyTools;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.service.UserSession;


/**
 * Created by lytm on 25/06/2017.
 */

public class AuthenticationPassword implements AuthenticationProvider.Callback {
    @Inject
    AccountStore.Repository accountRepository;
    boolean mSuggestFingerprint;
    private PasswordManager mPassword;
    private WeakReference<Context> mContext;
    private Intent pendingIntent;
    private KeyTools mKeyTools;
    private boolean isFinish = false; //
    private AuthenticationCallback mAuthenticationCallback;
    private AuthenticationProvider mAuthenticationProvider;
    IPasswordCallBack mPasswordCallBack = new IPasswordCallBack() {
        @Override
        public void onError(String pError) {
            Timber.d("PasswordManager onError [%s]", pError);
        }

        @Override
        public void onCheckedFingerPrint(boolean pChecked) {
            Timber.d("PasswordManager onCheckedFingerPrint [%s]", pChecked);
            mSuggestFingerprint = pChecked;
        }

        @Override
        public void onClose() {
            Timber.d("PasswordManager onClose");
        }

        @Override
        public void onComplete(String pHashPin) {
            Timber.d("PasswordManager onComplete [%s]", pHashPin);
            checkPassword(pHashPin);
            showLoading();
        }
    };

    public AuthenticationPassword(Context mContext, boolean pSuggestFingerprint, Intent pendingIntent, boolean isFinish) {
        this.mContext = new WeakReference<>(mContext);
        this.pendingIntent = pendingIntent;
        this.isFinish = isFinish;
        this.mKeyTools = new KeyTools();
        this.mSuggestFingerprint = pSuggestFingerprint;
        initPassword();
    }

    public AuthenticationPassword(Context mContext, boolean pSuggestFingerprint, AuthenticationCallback pAuthenticationCallback) {
        this.mContext = new WeakReference<>(mContext);
        this.mKeyTools = new KeyTools();
        this.mAuthenticationCallback = pAuthenticationCallback;
        this.mSuggestFingerprint = pSuggestFingerprint;
        initPassword();
    }

    public void initialize() {
        AndroidApplication.instance().getUserComponent().inject(this);
    }

    private void initPassword() {
        try {
            if (mPassword != null && mPassword.isShowing()) {
                return;
            }
            mPassword = new PasswordManager((Activity) mContext.get());
            mPassword.getBuilder()
                    .setTitle(mContext.get().getString(R.string.input_pin_to_access))
                    .showFPSuggestCheckBox(mSuggestFingerprint)
                    .setPasswordCallBack(mPasswordCallBack);
            mPassword.buildDialog();

            mPassword.show();
        } catch (Exception e) {
            Timber.d("AuthenticationPassword show password [%s]", e.getMessage());
        }
    }

    public PasswordManager getPasswordManager() {
        return mPassword;
    }

    void checkPassword(String pPass) {
        mAuthenticationProvider = new PasswordAuthenticationProvider(accountRepository, this);
        mAuthenticationProvider.verify(pPass);
    }

    @Override
    public void onAuthenticated(String password) {
        Timber.d("show password [%s]", password);
        UserSession.mHashPassword = password;
        UserSession.mLastTimeCheckPassword = System.currentTimeMillis();
        if (mSuggestFingerprint) {
            mKeyTools.storePassword(password);
        }
        if (pendingIntent != null) {
            mContext.get().startActivity(pendingIntent);
            if (isFinish) {
                ((Activity) mContext.get()).finish();
            }
        }
        if (mAuthenticationCallback != null) {
            mAuthenticationCallback.onAuthenticated(password);
        }
        try {
            mPassword.close();
        } catch (Exception e) {
            Timber.d("AuthenticationPassword close password [%s]", e.getMessage());
        }
    }

    @Override
    public void onError(Throwable e) {
        Timber.d("show password error [%s]", e);
        String message = ErrorMessageFactory.create(mContext.get(), e);
        setError(message);
        if (mAuthenticationCallback != null) {
            mAuthenticationCallback.onAuthenticationFailure();
        }
    }

    void showLoading() {
        if (mPassword != null) {

            try {
                mPassword.showLoading(true);
                mPassword.lock();
            } catch (Exception e) {
                Timber.d("AuthenticationPassword showLoading() [%s]", e.getMessage());
            }
        }
    }

    void setError(String pError) {
        if (mPassword != null) {
            try {
                mPassword.setError(pError);
                mPassword.unlock();
            } catch (Exception e) {
                Timber.d("AuthenticationPassword setError() [%s]", e.getMessage());
            }

        }
    }
}
