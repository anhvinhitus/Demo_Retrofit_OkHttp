package vn.com.zalopay.wallet.business.data;

import android.text.TextUtils;

import timber.log.Timber;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.controller.SDKApplication;

/***
 * log class
 */
public class Log {
    public static final boolean IS_LOG_ENABLE = !SDKApplication.isReleaseBuild();

    public static final String TAG = "ZALOPAY SDK";
    public static final String DEFAULT_ERROR = "ERROR";

    public static void e(String pObject, Exception pException) {
        Timber.tag(!TextUtils.isEmpty(pObject) ? pObject : TAG).e("%s", pException != null ? pException.getMessage() : DEFAULT_ERROR);
    }

    public static void e(Object pObject, Exception pException) {
        Timber.tag((pObject != null) ? pObject.getClass().getSimpleName() : TAG).e("%s", pException != null ? pException.getMessage() : DEFAULT_ERROR);
    }

    public static void w(Object pObject, Exception pException) {
        if (IS_LOG_ENABLE) {
            Timber.tag((pObject != null) ? pObject.getClass().getSimpleName() : TAG).w("%s", pException != null ? pException.getMessage() : DEFAULT_ERROR);
        }
    }

    public static void i(Object pObject, Exception pException) {
        if (IS_LOG_ENABLE) {
            Timber.tag((pObject != null) ? pObject.getClass().getSimpleName() : TAG).i("%s", pException != null ? pException.getMessage() : DEFAULT_ERROR);
        }
    }

    public static void e(Object pObject, String pMessage) {
        Timber.tag((pObject != null) ? pObject.getClass().getSimpleName() : TAG).e("%s", !TextUtils.isEmpty(pMessage) ? pMessage : DEFAULT_ERROR);
    }

    public static void w(Object pObject, String pMessage) {
        if (IS_LOG_ENABLE) {
            Timber.tag((pObject != null) ? pObject.getClass().getSimpleName() : TAG).w("%s", !TextUtils.isEmpty(pMessage) ? pMessage : DEFAULT_ERROR);
        }
    }

    public static void d(Object pObject, String pMessage, Object pObjectToLog) {
        if (IS_LOG_ENABLE) {
            Timber.tag((pObject != null) ? pObject.getClass().getSimpleName() : TAG).d("%s %s", pMessage, GsonUtils.toJsonString(pObjectToLog));
        }
    }

    public static void e(Object pObject, String pMessage, Object pObjectToLog) {
        Timber.tag((pObject != null) ? pObject.getClass().getSimpleName() : TAG).e("%s %s", pMessage, GsonUtils.toJsonString(pObjectToLog));
    }

    public static void i(Object pObject, String pMessage) {
        if (IS_LOG_ENABLE) {
            Timber.tag((pObject != null) ? pObject.getClass().getSimpleName() : TAG).i("%s", !TextUtils.isEmpty(pMessage) ? pMessage : DEFAULT_ERROR);
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

    public static void i(String pObject, String pMessage) {
        if (IS_LOG_ENABLE) {
            Timber.tag(!TextUtils.isEmpty(pObject) ? pObject : TAG).i("%s", !TextUtils.isEmpty(pMessage) ? pMessage : DEFAULT_ERROR);
        }
    }
}
