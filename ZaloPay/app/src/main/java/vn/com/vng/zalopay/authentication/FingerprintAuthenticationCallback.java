package vn.com.vng.zalopay.authentication;

import android.content.Context;

import java.lang.ref.WeakReference;

import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.authentication.fingerprintsupport.FingerprintManagerCompat;

/**
 * Created by hieuvm on 12/29/16.
 */
final class FingerprintAuthenticationCallback extends FingerprintManagerCompat.AuthenticationCallback {
    private final WeakReference<FingerprintProvider> mFingerprintProvider;
    private final Context mContext;

    FingerprintAuthenticationCallback(Context context, FingerprintProvider fingerprintUiHelper) {
        this.mFingerprintProvider = new WeakReference<>(fingerprintUiHelper);
        this.mContext = context;
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        Timber.d("onAuthenticationError: errorCode %s errString %s", errorCode, errString);
        if (mFingerprintProvider.get() == null) {
            return;
        }

        boolean showError = true;
        switch (errorCode) {
            case FingerprintManagerCompat.FINGERPRINT_ERROR_HW_UNAVAILABLE:
                errString = mContext.getString(R.string.fingerprint_error_hw_unavailable);
                break;
            case FingerprintManagerCompat.FINGERPRINT_ERROR_TIMEOUT:
                errString = mContext.getString(R.string.fingerprint_error_timeout);
                break;
            case FingerprintManagerCompat.FINGERPRINT_ERROR_LOCKOUT:
                errString = mContext.getString(R.string.finger_too_many_attempts);
                break;
            case FingerprintManagerCompat.FINGERPRINT_ERROR_CANCELED:
                errString = mContext.getString(R.string.finger_error_canceled);
                break;
            default:
                showError = false;
                break;
        }

        if (showError) {
            mFingerprintProvider.get().onAuthenticationError(errorCode, errString);
        }

    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        Timber.d("onAuthenticationHelp: helpCode %s helpString %s", helpCode, helpString);
        if (mFingerprintProvider.get() == null) {
            return;
        }
        boolean showError = true;

        switch (helpCode) {
            case FingerprintManagerCompat.FINGERPRINT_ACQUIRED_TOO_FAST:
                helpString = mContext.getString(R.string.finger_moved_too_fast);
                break;
            case FingerprintManagerCompat.FINGERPRINT_ACQUIRED_TOO_SLOW:
                helpString = mContext.getString(R.string.finger_moved_too_slow);
                break;
            case FingerprintManagerCompat.STATUS_QUALITY_FAILED:
                helpString = mContext.getString(R.string.finger_moved_too_slow);
                break;
            default:
                showError = false;
                break;
        }

        if (showError) {
            mFingerprintProvider.get().onAuthenticationHelp(helpCode, helpString);
        }
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
        if (mFingerprintProvider.get() == null) {
            return;
        }
        mFingerprintProvider.get().onAuthenticationSucceeded(result);
    }

    @Override
    public void onAuthenticationFailed() {
        Timber.d("onAuthenticationFailed");
        if (mFingerprintProvider.get() == null) {
            return;
        }
        mFingerprintProvider.get().onAuthenticationFailed();
    }


}
