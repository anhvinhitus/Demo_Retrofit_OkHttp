package vn.com.vng.zalopay.authentication;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;

import java.lang.ref.WeakReference;

import timber.log.Timber;
import vn.com.vng.zalopay.R;

/**
 * Created by hieuvm on 12/29/16.
 */
@TargetApi(Build.VERSION_CODES.M)
final class FingerprintAuthenticationCallback extends FingerprintManager.AuthenticationCallback {
    private WeakReference<FingerprintProvider> fingerprintProvider;
    private Context mContext;

    FingerprintAuthenticationCallback(Context context, FingerprintProvider fingerprintUiHelper) {
        this.fingerprintProvider = new WeakReference<>(fingerprintUiHelper);
        this.mContext = context;
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        Timber.d("onAuthenticationError: errorCode %s errString %s", errorCode, errString);
        if (fingerprintProvider.get() == null) {
            return;
        }

        boolean showError = true;
        switch (errorCode) {
            case FingerprintManager.FINGERPRINT_ERROR_HW_UNAVAILABLE:
                errString = mContext.getString(R.string.fingerprint_error_hw_unavailable);
                break;
            case FingerprintManager.FINGERPRINT_ERROR_TIMEOUT:
                errString = mContext.getString(R.string.fingerprint_error_timeout);
                break;
            case FingerprintManager.FINGERPRINT_ERROR_LOCKOUT:
                errString = mContext.getString(R.string.finger_too_many_attempts);
                break;
            case FingerprintManager.FINGERPRINT_ERROR_CANCELED:
                errString = mContext.getString(R.string.finger_error_canceled);
                break;
            default:
                showError = false;
                break;
        }

        if (showError) {
            fingerprintProvider.get().onAuthenticationError(errorCode, errString);
        }

    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        Timber.d("onAuthenticationHelp: helpCode %s helpString %s", helpCode, helpString);
        if (fingerprintProvider.get() == null) {
            return;
        }
        boolean showError = true;
        switch (helpCode) {
            case FingerprintManager.FINGERPRINT_ACQUIRED_TOO_FAST:
                helpString = mContext.getString(R.string.finger_moved_too_fast);
                break;
            case FingerprintManager.FINGERPRINT_ACQUIRED_TOO_SLOW:
                helpString = mContext.getString(R.string.finger_moved_too_slow);
                break;
            default:
                showError = false;
                break;
        }

        if (showError) {
            fingerprintProvider.get().onAuthenticationHelp(helpCode, helpString);
        }
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        if (fingerprintProvider.get() == null) {
            return;
        }
        fingerprintProvider.get().onAuthenticationSucceeded(result);
    }

    @Override
    public void onAuthenticationFailed() {
        Timber.d("onAuthenticationFailed");
        if (fingerprintProvider.get() == null) {
            return;
        }
        fingerprintProvider.get().onAuthenticationFailed();
    }
}
