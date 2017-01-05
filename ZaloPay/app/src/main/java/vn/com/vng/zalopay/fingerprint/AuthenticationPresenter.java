package vn.com.vng.zalopay.fingerprint;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;

import static vn.com.vng.zalopay.fingerprint.Stage.FINGERPRINT_DECRYPT;
import static vn.com.vng.zalopay.fingerprint.Stage.PASSWORD;

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
                || mStage == PASSWORD) {
            enterPassword();
        } else {
            String password = mPreferences.getString(Constants.PREF_KEY_PASSWORD, "");
            Timber.d("show password [%s]", password);
            if (!TextUtils.isEmpty(password)) {
                setStage(FINGERPRINT_DECRYPT);
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

        if (mStage == FINGERPRINT_DECRYPT) {
            mView.showFingerprintSuccess();
        }

        mView.hideLoading();
        mView.clearPassword();
        mView.onAuthenticated(password);
    }

    @Override
    public void onError(Throwable e) {
        Timber.d("onError: ");
        if (mStage == FINGERPRINT_DECRYPT) {
            handleErrorFingerprint(e);
        } else {
            handleErrorPassword(e);
        }
    }

    private void handleErrorFingerprint(Throwable e) {
        if (e != null) {
            mView.showFingerprintError(e.getMessage());
        }
    }

    private void handleErrorPassword(Throwable e) {
        if (mView == null) {
            return;
        }
        String message = ErrorMessageFactory.create(mApplicationContext, e);
        mView.hideLoading();
        mView.setErrorVerifyPassword(message);
    }

}