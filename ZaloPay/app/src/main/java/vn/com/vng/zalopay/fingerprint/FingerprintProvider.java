package vn.com.vng.zalopay.fingerprint;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.support.annotation.Nullable;

import timber.log.Timber;

/**
 * Created by hieuvm on 1/3/17.
 */

public class FingerprintProvider {

    private FingerprintManager mFingerprintManager;
    private KeyguardManager mKeyguardManager;

    @TargetApi(Build.VERSION_CODES.M)
    public FingerprintProvider(Context context) {
        if (!FingerprintUiHelper.checkAndroidMVersion()) {
            return;
        }

        try {
            mKeyguardManager = (KeyguardManager) context.getSystemService(Activity.KEYGUARD_SERVICE);
            mFingerprintManager = (FingerprintManager) context.getSystemService(Activity.FINGERPRINT_SERVICE);
        } catch (Exception ex) {
            Timber.d(ex, " create instance fingerprint api error");
        }
    }

    @Nullable
    public FingerprintManager getFingerprintManager() {
        return mFingerprintManager;
    }

    @Nullable
    public KeyguardManager getKeyguardManager() {
        return mKeyguardManager;
    }

}
