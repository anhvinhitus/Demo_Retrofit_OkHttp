package vn.com.vng.zalopay.authentication.fingerprintsupport;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompatApi23;
import android.support.v4.os.CancellationSignal;

/**
 * Created by hieuvm on 5/4/17.
 * *
 */

@RequiresApi(api = Build.VERSION_CODES.M)
final class MarshmallowFingerprintManagerCompatImpl implements FingerprintManagerCompat.FingerprintManagerCompatImpl {

    MarshmallowFingerprintManagerCompatImpl() {
    }


    @Override
    public boolean hasEnrolledFingerprints(Context context) {
        return FingerprintManagerCompatApi23.hasEnrolledFingerprints(context);
    }

    @Override
    public boolean isHardwareDetected(Context context) {
        return FingerprintManagerCompatApi23.isHardwareDetected(context);
    }

    @Override
    public void authenticate(Context context, FingerprintManagerCompat.CryptoObject crypto, int flags,
                             CancellationSignal cancel, FingerprintManagerCompat.AuthenticationCallback callback, Handler handler) {
        FingerprintManagerCompatApi23.authenticate(context, wrapCryptoObject(crypto), flags,
                cancel != null ? cancel.getCancellationSignalObject() : null,
                wrapCallback(callback), handler);
    }

    private static FingerprintManagerCompatApi23.CryptoObject wrapCryptoObject(
            FingerprintManagerCompat.CryptoObject cryptoObject) {
        if (cryptoObject == null) {
            return null;
        } else if (cryptoObject.getCipher() != null) {
            return new FingerprintManagerCompatApi23.CryptoObject(cryptoObject.getCipher());
        } else if (cryptoObject.getSignature() != null) {
            return new FingerprintManagerCompatApi23.CryptoObject(cryptoObject.getSignature());
        } else if (cryptoObject.getMac() != null) {
            return new FingerprintManagerCompatApi23.CryptoObject(cryptoObject.getMac());
        } else {
            return null;
        }
    }

    static FingerprintManagerCompat.CryptoObject unwrapCryptoObject(
            FingerprintManagerCompatApi23.CryptoObject cryptoObject) {
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

    private static FingerprintManagerCompatApi23.AuthenticationCallback wrapCallback(
            final FingerprintManagerCompat.AuthenticationCallback callback) {
        return new FingerprintManagerCompatApi23.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                callback.onAuthenticationError(errMsgId, errString);
            }

            @Override
            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                callback.onAuthenticationHelp(helpMsgId, helpString);
            }

            @Override
            public void onAuthenticationSucceeded(
                    FingerprintManagerCompatApi23.AuthenticationResultInternal result) {
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