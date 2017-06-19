package vn.com.zalopay.utility;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

public class FingerprintUtils {
    public static boolean deviceSupportFingerPrint(Context context) {
        try {
            return FingerprintUtils.isFingerPrintValid(context);
        } catch (Exception ex) {
            Log.e("isDeviceSupportFinger", ex.getMessage());
        }
        return false;
    }

    public static boolean isFingerPrintValid(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // TODO: code here to check fingerprint
            KeyguardManager keyguardManager = context.getSystemService(KeyguardManager.class);
            FingerprintManager fingerprintManager = context.getSystemService(FingerprintManager.class);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return false;
            }

            // The user has set up a fingerprint or lock screen && fingerprints are registered.
            if (keyguardManager.isKeyguardSecure() && fingerprintManager.hasEnrolledFingerprints())
                return true;
        }
        return false;
    }
}