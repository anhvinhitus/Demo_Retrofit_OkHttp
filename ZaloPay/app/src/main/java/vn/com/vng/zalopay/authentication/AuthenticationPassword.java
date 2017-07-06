package vn.com.vng.zalopay.authentication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.zalopay.ui.widget.password.interfaces.IPinCallBack;
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
    private PasswordManager mPassword;
    private WeakReference<Context> mContext;
    private Intent pendingIntent;
    private KeyTools mKeyTools;
    private boolean isFinish = false; //
    private AuthenticationCallback mAuthenticationCallback;
    private boolean mSuggestFingerprint;
    @Inject
    AccountStore.Repository accountRepository;

    public void initialize() {
        AndroidApplication.instance().getUserComponent().inject(this);
    }

    private AuthenticationProvider mAuthenticationProvider;

    public AuthenticationPassword(Context mContext, boolean pSuggestFingerprint, Intent pendingIntent, boolean isFinish) {
        this.mContext = new WeakReference<Context>(mContext);
        this.pendingIntent = pendingIntent;
        this.isFinish = isFinish;
        this.mKeyTools = new KeyTools();
        this.mSuggestFingerprint = pSuggestFingerprint;
        initPassword();
    }

    public AuthenticationPassword(Context mContext, boolean pSuggestFingerprint, AuthenticationCallback pAuthenticationCallback) {
        this.mContext = new WeakReference<Context>(mContext);
        this.mKeyTools = new KeyTools();
        this.mAuthenticationCallback = pAuthenticationCallback;
        this.mSuggestFingerprint = pSuggestFingerprint;
        initPassword();
    }

    private void initPassword() {
        mPassword = new PasswordManager((Activity) mContext.get(), mContext.get().getString(R.string.input_pin_to_access), null, null, mSuggestFingerprint, new IPinCallBack() {
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
            public void onCancel() {
                Timber.d("PasswordManager onCancel");
            }

            @Override
            public void onComplete(String pHashPin) {
                Timber.d("PasswordManager onComplete [%s]", pHashPin);
                checkPassword(pHashPin);

            }
        });
        mPassword.show();
    }

    public PasswordManager getPasswordManager() {
        return mPassword;
    }

    private void checkPassword(String pPass) {
        mAuthenticationProvider = new PasswordAuthenticationProvider(mContext.get(), accountRepository, this);
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
        mPassword.closePinView();
    }

    @Override
    public void onError(Throwable e) {
        Timber.d("show password error [%s]", e);
        String message = ErrorMessageFactory.create(mContext.get(), e);
        mPassword.setErrorMessage(message);
        if (mAuthenticationCallback != null) {
            mAuthenticationCallback.onAuthenticationFailure();
        }
    }
}
