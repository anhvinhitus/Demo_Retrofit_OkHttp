package vn.com.vng.zalopay.authentication;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.text.TextUtils;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.exception.FingerprintException;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;

import static vn.com.vng.zalopay.data.NetworkError.INCORRECT_PIN;

/**
 * Created by hieuvm on 12/27/16.
 */

public class AuthenticationPresenter extends AbstractPresenter<IAuthenticationView> implements AuthenticationProvider.Callback {

    private AccountStore.Repository mAccountRepository;
    private Context mApplicationContext;

    @Inject
    SharedPreferences mPreferences;

    private Stage mStage = Stage.FINGERPRINT_DECRYPT;

    @Inject
    KeyTools mKeyTools;

    private AuthenticationProvider mAuthenticationProvider;

    @Inject
    AuthenticationPresenter(AccountStore.Repository accountRepository,
                            Context applicationContext) {
        this.mAccountRepository = accountRepository;
        this.mApplicationContext = applicationContext;
    }


    void onViewCreated() {
        updateStage();
        if (!FingerprintUtil.isKeyguardSecure(mApplicationContext)
                || !FingerprintUtil.isFingerprintAuthAvailable(mApplicationContext)
                || mStage == Stage.PASSWORD) {
            enterPassword();
        } else {
            String password = mPreferences.getString(Constants.PREF_KEY_PASSWORD, "");
            Timber.d("show password [%s]", password);
            if (!TextUtils.isEmpty(password)) {
                setStage(Stage.FINGERPRINT_DECRYPT);
                return;
            }
            enterPassword();
        }
    }

    void verify(String password) {
        switch (mStage) {
            case FINGERPRINT_DECRYPT:
                enterPassword();
                break;
            case PASSWORD:
                verifyPassword(password);
                break;
        }
    }

    private void enterPassword(Stage stage) {
        setStage(stage);
        updateStage();
        if (mView != null) {
            mView.showKeyboard();
        }

        mAuthenticationProvider.stopVerify();
    }

    private void enterPassword() {
        enterPassword(Stage.PASSWORD);
    }

    private void verifyPassword(String password) {
        if (mView != null) {
            mView.showLoading();
        }

        mAuthenticationProvider.verify(password);
    }

    private void updateStage() {
        switch (mStage) {
            case FINGERPRINT_DECRYPT:
                mAuthenticationProvider = new FingerprintProvider(mApplicationContext, mKeyTools, this);
                break;
            case PASSWORD:
                mAuthenticationProvider = new PasswordAuthenticationProvider(mApplicationContext, mAccountRepository, this);
                break;
        }

        mView.updateStage(mStage);
    }


    public void resume() {
        mAuthenticationProvider.startVerify();
    }

    public void pause() {
        mAuthenticationProvider.stopVerify();
    }


    public void setStage(Stage stage) {
        Timber.d("setStage: [%s]", stage);
        mStage = stage;
    }

    public Stage getStage() {
        return mStage;
    }

    @Override
    public void onAuthenticated(String password) {
        Timber.d("onAuthenticated: %s", password);

        if (mView == null) {
            return;
        }

        if (mStage == Stage.FINGERPRINT_DECRYPT) {
            mView.showFingerprintSuccess();
        }

        mView.hideLoading();
        mView.clearPassword();
        mView.onAuthenticated(password);
    }

    @Override
    public void onError(Throwable e) {
        Timber.d(e, "onError: ");
        if (mStage == Stage.FINGERPRINT_DECRYPT) {
            handleErrorFingerprint(e);
        } else {
            handleErrorPassword(e);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void handleErrorFingerprint(Throwable e) {
        if (mView == null) {
            return;
        }

        if (!(e instanceof FingerprintException)) {
            return;
        }

        boolean notRetry = ((FingerprintException) e).mErrorCode == FingerprintManager.FINGERPRINT_ERROR_TIMEOUT
                || ((FingerprintException) e).mErrorCode == FingerprintManager.FINGERPRINT_ERROR_LOCKOUT
                || ((FingerprintException) e).mErrorCode == FingerprintManager.FINGERPRINT_ERROR_CANCELED
                || ((FingerprintException) e).mErrorCode == FingerprintManager.FINGERPRINT_ERROR_HW_UNAVAILABLE
                ;

        mView.showFingerprintError(e.getMessage(), !notRetry);
    }

    private void handleErrorPassword(Throwable e) {
        if (mView == null) {
            return;
        }

        String message = ErrorMessageFactory.create(mApplicationContext, e);
        mView.hideLoading();
        mView.clearPassword();
        mView.setErrorVerifyPassword(message);
        if (e instanceof BodyException) {
            if (((BodyException) e).errorCode == INCORRECT_PIN) {
                mView.showKeyboard();
            }
        }
    }

}