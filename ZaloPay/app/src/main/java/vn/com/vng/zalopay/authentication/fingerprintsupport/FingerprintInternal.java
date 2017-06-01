package vn.com.vng.zalopay.authentication.fingerprintsupport;


import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.os.CancellationSignal;

import com.samsung.android.sdk.SsdkVendorCheck;

import timber.log.Timber;

/**
 * Created by hieuvm on 5/5/17.
 * *
 */

class FingerprintInternal {

    private static FingerprintManagerCompat.FingerprintManagerCompatImpl IMPL;

    synchronized static void initialize(Context context) {
        if (IMPL != null) {
            return;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            IMPL = new LegacyFingerprintManagerCompatImpl();
            return;
        }

        boolean isSamsungDevice = SsdkVendorCheck.isSamsungDevice();

        if (isSamsungDevice) {
            FingerprintManagerCompat.FingerprintManagerCompatImpl ssFingerprintManager = new SamSungFingerprintManagerCompatImpl(context);
            if (ssFingerprintManager.isHardwareDetected(context)) {
                IMPL = ssFingerprintManager;
            }
        }

        Timber.d("initialize: IMPL SamSung %s", IMPL);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            MarshmallowFingerprintManagerCompatImpl fingerprintManagerCompat = new MarshmallowFingerprintManagerCompatImpl(context);
            if (fingerprintManagerCompat.isHardwareDetected(context)) {
                IMPL = fingerprintManagerCompat;
            }
        }

        Timber.d("initialize: IMPL Android %s", IMPL);

        if (IMPL == null) {
            IMPL = new LegacyFingerprintManagerCompatImpl();
        }
    }

    static boolean isHardwareDetected(Context context) {
        if (IMPL == null) {
            throw new IllegalStateException("FingerprintManagerCompat not initialized");
        }

        return IMPL.isHardwareDetected(context);
    }

    static boolean hasEnrolledFingerprints(Context context) {
        if (IMPL == null) {
            throw new IllegalStateException("FingerprintManagerCompat not initialized");
        }

        return IMPL.hasEnrolledFingerprints(context);
    }

    static void authenticate(Context context, @Nullable FingerprintManagerCompat.CryptoObject crypto, int flags,
                             @Nullable CancellationSignal cancel, @NonNull FingerprintManagerCompat.AuthenticationCallback callback,
                             @Nullable Handler handler) {
        if (IMPL == null) {
            throw new IllegalStateException("FingerprintManagerCompat not initialized");
        }

        IMPL.authenticate(context, crypto, flags, cancel, callback, handler);
    }

  /*  static {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            IMPL = new MarshmallowFingerprintManagerCompatImpl();
        }

        if (IMPL == null || !IMPL.isHardwareDetected(AndroidApplication.instance())) {
            if (version >= 17 && SsdkVendorCheck.isSamsungDevice()) {
                IMPL = new SamSungFingerprintManagerCompatImpl(AndroidApplication.instance());
            }
        }

        if (IMPL == null || !IMPL.isHardwareDetected(AndroidApplication.instance())) {
            IMPL = new LegacyFingerprintManagerCompatImpl();
        }
    }*/
}
