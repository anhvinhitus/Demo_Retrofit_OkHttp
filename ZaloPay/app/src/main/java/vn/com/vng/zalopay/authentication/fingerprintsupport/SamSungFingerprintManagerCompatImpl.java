package vn.com.vng.zalopay.authentication.fingerprintsupport;

import android.content.Context;
import android.os.Handler;
import android.support.v4.os.CancellationSignal;

import com.samsung.android.sdk.pass.Spass;
import com.samsung.android.sdk.pass.SpassFingerprint;

import timber.log.Timber;

/**
 * Created by hieuvm on 5/5/17.
 * *
 */

final class SamSungFingerprintManagerCompatImpl implements FingerprintManagerCompat.FingerprintManagerCompatImpl {
    private final Context mContext;
    private final Spass mSpass;
    private SpassFingerprint mSpassFingerprint;

    SamSungFingerprintManagerCompatImpl(Context context) {
        this.mContext = context;
        Spass s;
        try {
            s = new Spass();
            s.initialize(context);
        } catch (SecurityException e) {
            throw e;
        } catch (Exception ignored) {
            s = null;
        }

        mSpass = s;
    }


    @Override
    public boolean hasEnrolledFingerprints(Context context) {
        try {
            if (isHardwareDetected(context)) {
                if (mSpassFingerprint == null) {
                    mSpassFingerprint = new SpassFingerprint(context);
                }
                return mSpassFingerprint.hasRegisteredFinger();
            }
        } catch (Exception e) {
            Timber.d(e);
        }
        return false;
    }

    @Override
    public boolean isHardwareDetected(Context context) {
        return mSpass != null && mSpass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT);
    }

    @Override
    public void authenticate(Context context, FingerprintManagerCompat.CryptoObject crypto, int flags, CancellationSignal cancel, FingerprintManagerCompat.AuthenticationCallback callback, Handler handler) {

    }

    private static void cancelFingerprintRequest(SpassFingerprint spassFingerprint) {
        try {
            spassFingerprint.cancelIdentify();
        } catch (Throwable t) {
            // There's no way to query if there's an active identify request,
            // so just try to cancel and ignore any exceptions.
        }
    }
}
