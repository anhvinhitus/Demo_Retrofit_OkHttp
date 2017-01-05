package vn.com.vng.zalopay.fingerprint;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;

import timber.log.Timber;

/**
 * Created by hieuvm on 1/5/17.
 */

final class FingerprintUtil {

    static boolean isFingerprintAuthAvailable(Context context) {
        if (!checkAndroidMVersion()) {
            return false;
        }

        return isHardwarePresent(context) && hasFingerprintRegistered(context);
    }

    @TargetApi(Build.VERSION_CODES.M)
    static boolean isHardwarePresent(Context context) {
        if (!checkAndroidMVersion()) {
            return false;
        }

        FingerprintManager fingerprintManager = getFingerprintManager(context);
        if (fingerprintManager != null) {
            try {
                return fingerprintManager.isHardwareDetected();
            } catch (SecurityException ignored) {
            }
        }

        return false;
    }

    static boolean isKeyguardSecure(Context context) {
        if (!checkAndroidMVersion()) {
            return false;
        }

        KeyguardManager keyguardManager = getKeyguardManager(context);
        return keyguardManager != null && keyguardManager.isKeyguardSecure();
    }

    @TargetApi(Build.VERSION_CODES.M)
    static boolean hasFingerprintRegistered(Context context) {
        if (!checkAndroidMVersion()) {
            return false;
        }

        FingerprintManager fingerprintManager = getFingerprintManager(context);
        if (fingerprintManager != null) {
            try {
                return fingerprintManager.hasEnrolledFingerprints();
            } catch (SecurityException ignored) {
            }
        }

        return false;
    }

    static boolean checkAndroidMVersion() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    static FingerprintManager getFingerprintManager(Context context) {
        if (!checkAndroidMVersion()) {
            return null;
        }

        try {
            return (FingerprintManager) context.getSystemService(Activity.FINGERPRINT_SERVICE);
        } catch (Exception e) {
            Timber.d(e, " create instance fingerprint api error");
            return null;
        }
    }


    static KeyguardManager getKeyguardManager(Context context) {
        if (!checkAndroidMVersion()) {
            return null;
        }
        try {
            return (KeyguardManager) context.getSystemService(Activity.KEYGUARD_SERVICE);
        } catch (Exception e) {
            Timber.d(e, " create instance fingerprint api error");
            return null;
        }
    }
}
