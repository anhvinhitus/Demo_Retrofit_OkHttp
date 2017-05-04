package vn.com.vng.zalopay.authentication.fingerprintsupport;

import android.content.Context;
import android.os.Handler;
import android.support.v4.os.CancellationSignal;

/**
 * Created by hieuvm on 5/4/17.
 * *
 */

public class LegacyFingerprintManagerCompatImpl
        implements FingerprintManagerCompat.FingerprintManagerCompatImpl {

    public LegacyFingerprintManagerCompatImpl() {
    }

    @Override
    public boolean hasEnrolledFingerprints(Context context) {
        return false;
    }

    @Override
    public boolean isHardwareDetected(Context context) {
        return false;
    }

    @Override
    public void authenticate(Context context, FingerprintManagerCompat.CryptoObject crypto, int flags,
                             CancellationSignal cancel, FingerprintManagerCompat.AuthenticationCallback callback, Handler handler) {
        // TODO: Figure out behavior when there is no fingerprint hardware available
    }
}
