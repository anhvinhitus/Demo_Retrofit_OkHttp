package vn.com.vng.zalopay.utils;

import android.content.Context;
import android.text.TextUtils;

import vn.com.vng.zalopay.authentication.fingerprintsupport.FingerprintManagerCompat;
import vn.com.vng.zalopay.data.cache.UserConfig;

/**
 * Created by lytm on 26/06/2017.
 */

public class PasswordUtil {
    private PasswordUtil() {
        // private constructor for utils class
    }

    public static boolean detectShowFingerPrint(Context context, UserConfig pUserConfig) {
        FingerprintManagerCompat mFingerprintManagerCompat = FingerprintManagerCompat.from(context);
        String password = pUserConfig.getEncryptedPassword();
        if (mFingerprintManagerCompat.isFingerprintAvailable() && !TextUtils.isEmpty(password)) {
            return true;
        }
        return false;
    }

    public static boolean detectFingerPrint(Context context) {
        FingerprintManagerCompat mFingerprintManagerCompat = FingerprintManagerCompat.from(context);
        return mFingerprintManagerCompat.isFingerprintAvailable();
    }

    public static boolean detectSuggestFingerprint(Context context, UserConfig pUserConfig) {
        if (!detectFingerPrint(context)) {
            return false;
        }
        return !detectShowFingerPrint(context, pUserConfig);
    }
}
