package vn.com.zalopay.wallet.utils;

import android.text.TextUtils;

import timber.log.Timber;
import vn.com.zalopay.wallet.controller.WalletSDKApplication;

/***
 * log class
 */
public class Log {
    public static final boolean IS_LOG_ENABLE = (WalletSDKApplication.isReleaseBuild()) ? false : true;

    public static final String TAG = "ZALOPAY SDK";

    public static final String DEFAULT_ERROR = "ERROR";

    public static void e(String pObject, Exception pException) {
        Timber.tag(!TextUtils.isEmpty(pObject) ? pObject : TAG).e("%s", pException != null ? pException.getMessage() : DEFAULT_ERROR);
    }

    public static void d(String pObject, Exception pException) {
        if (IS_LOG_ENABLE) {
            Timber.tag(!TextUtils.isEmpty(pObject) ? pObject : TAG).d("%s", pException != null ? pException.getMessage() : DEFAULT_ERROR);
        }
    }

    public static void e(Object pObject, Exception pException) {
        Timber.tag((pObject != null) ? pObject.getClass().getName() : TAG).e("%s", pException != null ? pException.getMessage() : DEFAULT_ERROR);
    }

    public static void w(Object pObject, Exception pException) {
        if (IS_LOG_ENABLE) {
            Timber.tag((pObject != null) ? pObject.getClass().getName() : TAG).w("%s", pException != null ? pException.getMessage() : DEFAULT_ERROR);
        }
    }

    public static void d(Object pObject, Exception pException) {
        if (IS_LOG_ENABLE) {
            Timber.tag((pObject != null) ? pObject.getClass().getName() : TAG).d("%s", pException != null ? pException.getMessage() : DEFAULT_ERROR);
        }
    }

    public static void i(Object pObject, Exception pException) {
        if (IS_LOG_ENABLE) {
            Timber.tag((pObject != null) ? pObject.getClass().getName() : TAG).i("%s", pException != null ? pException.getMessage() : DEFAULT_ERROR);
        }
    }

    public static void e(Object pObject, String pMessage) {
        Timber.tag((pObject != null) ? pObject.getClass().getName() : TAG).e("%s", !TextUtils.isEmpty(pMessage) ? pMessage : DEFAULT_ERROR);
    }

    public static void w(Object pObject, String pMessage) {
        if (IS_LOG_ENABLE) {
            Timber.tag((pObject != null) ? pObject.getClass().getName() : TAG).w("%s", !TextUtils.isEmpty(pMessage) ? pMessage : DEFAULT_ERROR);
        }
    }

    public static void d(Object pObject, String pMessage) {
        if (IS_LOG_ENABLE) {
            Timber.tag((pObject != null) ? pObject.getClass().getName() : TAG).d("%s", !TextUtils.isEmpty(pMessage) ? pMessage : DEFAULT_ERROR);
        }
    }

    public static void i(Object pObject, String pMessage) {
        if (IS_LOG_ENABLE) {
            Timber.tag((pObject != null) ? pObject.getClass().getName() : TAG).i("%s", !TextUtils.isEmpty(pMessage) ? pMessage : DEFAULT_ERROR);
        }
    }

    public static void e(String pObject, String pMessage) {
        Timber.tag(!TextUtils.isEmpty(pObject) ? pObject : TAG).e("%s", !TextUtils.isEmpty(pMessage) ? pMessage : DEFAULT_ERROR);
    }

    public static void w(String pObject, String pMessage) {
        if (IS_LOG_ENABLE) {
            Timber.tag(!TextUtils.isEmpty(pObject) ? pObject : TAG).w("%s", !TextUtils.isEmpty(pMessage) ? pMessage : DEFAULT_ERROR);
        }
    }

    public static void d(String pObject, String pMessage) {
        if (IS_LOG_ENABLE) {
            Timber.tag(!TextUtils.isEmpty(pObject) ? pObject : TAG).d("%s", !TextUtils.isEmpty(pMessage) ? pMessage : DEFAULT_ERROR);
        }
    }

    public static void i(String pObject, String pMessage) {
        if (IS_LOG_ENABLE) {
            Timber.tag(!TextUtils.isEmpty(pObject) ? pObject : TAG).i("%s", !TextUtils.isEmpty(pMessage) ? pMessage : DEFAULT_ERROR);
        }
    }
}
