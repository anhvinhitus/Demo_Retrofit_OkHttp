package vn.com.vng.zalopay.fingerprint;

import android.annotation.TargetApi;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;

import java.lang.ref.WeakReference;

/**
 * Created by hieuvm on 12/29/16.
 */
@TargetApi(Build.VERSION_CODES.M)
final class FingerprintAuthenticationCallback extends FingerprintManager.AuthenticationCallback {
    private WeakReference<FingerprintProvider> fingerprintUiHelper;

    FingerprintAuthenticationCallback(FingerprintProvider fingerprintUiHelper) {
        this.fingerprintUiHelper = new WeakReference<>(fingerprintUiHelper);
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        if (fingerprintUiHelper.get() == null) {
            return;
        }
        fingerprintUiHelper.get().onAuthenticationError(errorCode, errString);
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        if (fingerprintUiHelper.get() == null) {
            return;
        }
        fingerprintUiHelper.get().onAuthenticationHelp(helpCode, helpString);
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        if (fingerprintUiHelper.get() == null) {
            return;
        }
        fingerprintUiHelper.get().onAuthenticationSucceeded(result);
    }

    @Override
    public void onAuthenticationFailed() {
        if (fingerprintUiHelper.get() == null) {
            return;
        }
        fingerprintUiHelper.get().onAuthenticationFailed();
    }
}
