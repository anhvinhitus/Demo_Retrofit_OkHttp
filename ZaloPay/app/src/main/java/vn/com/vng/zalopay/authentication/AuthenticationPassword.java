package vn.com.vng.zalopay.authentication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.zalopay.ui.widget.password.interfaces.IPinCallBack;
import com.zalopay.ui.widget.password.managers.PasswordManager;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.authentication.secret.KeyTools;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.service.UserSession;
import vn.com.vng.zalopay.utils.PasswordUtil;

/**
 * Created by lytm on 25/06/2017.
 */

public class AuthenticationPassword implements AuthenticationProvider.Callback {
    private PasswordManager mPassword;
    private Context mContext;
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

    public AuthenticationPassword(Context mContext, Intent pendingIntent, boolean isFinish) {
        this.mContext = mContext;
        this.pendingIntent = pendingIntent;
        this.isFinish = isFinish;
        this.mKeyTools = new KeyTools();
        initPassword();
    }

    public AuthenticationPassword(Context mContext, AuthenticationCallback pAuthenticationCallback) {
        this.mContext = mContext;
        this.mKeyTools = new KeyTools();
        this.mAuthenticationCallback = pAuthenticationCallback;
        initPassword();
    }

    private void initPassword() {
        mPassword = new PasswordManager((Activity) mContext, mContext.getString(R.string.input_pin_to_access), null, null, PasswordUtil.detectFingerPrint(mContext), new IPinCallBack() {
            @Override
            public void onError(String pError) {

            }

            @Override
            public void onCheckedFingerPrint(boolean pChecked) {
                mSuggestFingerprint = pChecked;
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onComplete(String pHashPin) {
                checkPassword(pHashPin);

            }
        });
        mPassword.show();
    }

    private void checkPassword(String pPass) {
        mAuthenticationProvider = new PasswordAuthenticationProvider(mContext, accountRepository, this);
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
            mContext.startActivity(pendingIntent);
            if (isFinish) {
                ((Activity) mContext).finish();
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
        mPassword.setErrorMessage(e.getMessage());
        if (mAuthenticationCallback != null) {
            mAuthenticationCallback.onAuthenticationFailure();
        }
    }
}
