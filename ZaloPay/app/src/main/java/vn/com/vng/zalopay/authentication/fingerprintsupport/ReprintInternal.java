package vn.com.vng.zalopay.authentication.fingerprintsupport;


import android.content.Context;
import android.os.Build;

import com.samsung.android.sdk.SsdkVendorCheck;

/**
 * Created by hieuvm on 5/5/17.
 * *
 */

class ReprintInternal {

    static FingerprintManagerCompat.FingerprintManagerCompatImpl IMPL;

    public static void initialize(Context context) {
        if (IMPL != null) {
            return;
        }

        if (Build.VERSION.SDK_INT < 17) {
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Api23FingerprintManagerCompatImpl fingerprintManagerCompat = new Api23FingerprintManagerCompatImpl();
            if (fingerprintManagerCompat.isHardwareDetected(context)) {
                IMPL = fingerprintManagerCompat;
            }
        }

        if (IMPL == null) {
            IMPL = new LegacyFingerprintManagerCompatImpl();
        }
    }


  /*  static {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            IMPL = new Api23FingerprintManagerCompatImpl();
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
