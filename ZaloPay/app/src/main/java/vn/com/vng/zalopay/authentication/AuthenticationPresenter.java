package vn.com.vng.zalopay.authentication;

import android.content.Context;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.authentication.fingerprintsupport.FingerprintManagerCompat;
import vn.com.vng.zalopay.authentication.secret.KeyTools;
import vn.com.vng.zalopay.exception.FingerprintException;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;

/**
 * Created by hieuvm on 12/27/16.
 */

public class AuthenticationPresenter extends AbstractPresenter<IAuthenticationView> implements AuthenticationProvider.Callback {

    private final Context mApplicationContext;
    private final KeyTools mKeyTools;


    private AuthenticationProvider mAuthenticationProvider;

    @Inject
    AuthenticationPresenter(Context applicationContext) {
        this.mApplicationContext = applicationContext;
        this.mKeyTools = new KeyTools();

    }

    void onViewCreated() {
        mAuthenticationProvider = new FingerprintProvider(mApplicationContext, mKeyTools, this);
    }

    public void resume() {
        if (mAuthenticationProvider != null) {
            mAuthenticationProvider.startVerify();
        }
    }

    public void pause() {
        if (mAuthenticationProvider != null) {
            mAuthenticationProvider.stopVerify();
        }
    }


    @Override
    public void onAuthenticated(String password) {
        Timber.d("onAuthenticated: %s", password);
        if (mView == null) {
            return;
        }
        mView.showFingerprintSuccess();
        mView.hideLoading();
        mView.onAuthenticated(password);
    }

    @Override
    public void onError(Throwable e) {
        Timber.d(e, "Authentication error");
        handleErrorFingerprint(e);

    }

    private void handleErrorFingerprint(Throwable e) {
        if (mView == null) {
            return;
        }

        if (!(e instanceof FingerprintException)) {
            return;
        }

        FingerprintException fingerException = (FingerprintException) e;

        boolean notRetry = (fingerException.mErrorCode == FingerprintManagerCompat.FINGERPRINT_ERROR_TIMEOUT
                || fingerException.mErrorCode == FingerprintManagerCompat.FINGERPRINT_ERROR_LOCKOUT
                || fingerException.mErrorCode == FingerprintManagerCompat.FINGERPRINT_ERROR_CANCELED
                || fingerException.mErrorCode == FingerprintManagerCompat.FINGERPRINT_ERROR_HW_UNAVAILABLE);

        mView.showFingerprintError(e.getMessage(), !notRetry);
    }


}