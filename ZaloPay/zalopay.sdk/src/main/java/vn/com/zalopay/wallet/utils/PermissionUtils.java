package vn.com.zalopay.wallet.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import vn.com.zalopay.wallet.business.data.Constants;

/**
 * request permission at runtime class
 */
public class PermissionUtils {
    public static boolean checkIfAlreadyhavePermission(Context pContext) {
        int result = ContextCompat.checkSelfPermission(pContext, Manifest.permission.RECEIVE_SMS);

        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public static void requestForSpecificPermission(Activity pActivity) {
        ActivityCompat.requestPermissions(pActivity, new String[]{Manifest.permission.RECEIVE_SMS}, Constants.REQUEST_CODE_SMS);
    }

    /***
     * android 5. need to request permission at runtime
     *
     * @return
     */
    public static boolean isNeedToRequestPermissionAtRuntime() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            return true;
        }

        return false;
    }
}
