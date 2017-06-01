package vn.com.vng.zalopay.authentication.fingerprintsupport;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.os.CancellationSignal;

import timber.log.Timber;

/**
 * Created by hieuvm on 5/4/17.
 * *
 */
@TargetApi(23)
@RequiresApi(api = Build.VERSION_CODES.M)
final class MarshmallowFingerprintManagerCompatImpl implements FingerprintManagerCompat.FingerprintManagerCompatImpl {

    private final FingerprintManager mFingerprintManager;

    MarshmallowFingerprintManagerCompatImpl(Context context) {
        mFingerprintManager = getFingerprintManagerOrNull(context);
    }

    private FingerprintManager getFingerprintManagerOrNull(Context context) {
        try {
            return (FingerprintManager) context.getSystemService(Activity.FINGERPRINT_SERVICE);
        } catch (Exception ignore) {
            return null;
        }
    }

    public boolean hasEnrolledFingerprints(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            Timber.d("permission PERMISSION_GRANTED deny");
            return false;
        }
        return mFingerprintManager != null && mFingerprintManager.hasEnrolledFingerprints();
    }

    public boolean isHardwareDetected(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            Timber.d("permission PERMISSION_GRANTED deny");
            return false;
        }

        return (mFingerprintManager != null) && mFingerprintManager.isHardwareDetected();
    }

    @Override
    public void authenticate(Context context, FingerprintManagerCompat.CryptoObject crypto, int flags,
                             CancellationSignal cancel, FingerprintManagerCompat.AuthenticationCallback callback, Handler handler) {

        if (mFingerprintManager == null) {
            return;
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mFingerprintManager.authenticate(wrapCryptoObject(crypto),
                cancel != null ? ((android.os.CancellationSignal) cancel.getCancellationSignalObject()) : null, flags,
                wrapCallback(callback), handler);
    }

    private static FingerprintManager.CryptoObject wrapCryptoObject(
            FingerprintManagerCompat.CryptoObject cryptoObject) {
        if (cryptoObject == null) {
            return null;
        } else if (cryptoObject.getCipher() != null) {
            return new FingerprintManager.CryptoObject(cryptoObject.getCipher());
        } else if (cryptoObject.getSignature() != null) {
            return new FingerprintManager.CryptoObject(cryptoObject.getSignature());
        } else if (cryptoObject.getMac() != null) {
            return new FingerprintManager.CryptoObject(cryptoObject.getMac());
        } else {
            return null;
        }
    }

    private static FingerprintManagerCompat.CryptoObject unwrapCryptoObject(
            FingerprintManager.CryptoObject cryptoObject) {
        if (cryptoObject == null) {
            return null;
        } else if (cryptoObject.getCipher() != null) {
            return new FingerprintManagerCompat.CryptoObject(cryptoObject.getCipher());
        } else if (cryptoObject.getSignature() != null) {
            return new FingerprintManagerCompat.CryptoObject(cryptoObject.getSignature());
        } else if (cryptoObject.getMac() != null) {
            return new FingerprintManagerCompat.CryptoObject(cryptoObject.getMac());
        } else {
            return null;
        }
    }

    private static FingerprintManager.AuthenticationCallback wrapCallback(
            final FingerprintManagerCompat.AuthenticationCallback callback) {
        return new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                callback.onAuthenticationError(errMsgId, errString);
            }

            @Override
            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                callback.onAuthenticationHelp(helpMsgId, helpString);
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                callback.onAuthenticationSucceeded(new FingerprintManagerCompat.AuthenticationResult(
                        unwrapCryptoObject(result.getCryptoObject())));
            }

            @Override
            public void onAuthenticationFailed() {
                callback.onAuthenticationFailed();
            }
        };
    }
}