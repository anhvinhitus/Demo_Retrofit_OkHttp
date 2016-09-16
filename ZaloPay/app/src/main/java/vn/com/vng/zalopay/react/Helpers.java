package vn.com.vng.zalopay.react;

import android.text.TextUtils;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import timber.log.Timber;
import vn.com.vng.zalopay.react.error.PaymentError;

/**
 * Created by huuhoa on 7/14/16.
 * Internal helpers
 */
public class Helpers {
    public static void promiseReject(Promise promise, String code, String message) {
        promise.reject(code, message);
    }

    public static void promiseResolveError(Promise promise, int errorCode, String message) {
        Timber.d("promiseResolveError start errorCode [%s] message [%s]", errorCode, message);
        if (promise == null) {
            Timber.i("Null promise. Doing nothing");
            return;
        }

        WritableMap item = Arguments.createMap();
        item.putInt("code", errorCode);
        if (!TextUtils.isEmpty(message)) {
            item.putString("message", message);
        }
        promise.resolve(item);
    }

    public static void promiseResolveDialog(Promise promise, int btnIndex) {
        Timber.d("promiseResolveDialog promise [%s] btnIndex [%s]", promise, btnIndex);
        if (promise == null) {
            return;
        }
        WritableMap item = Arguments.createMap();
        item.putInt("code", btnIndex);
        promise.resolve(item);
    }

    public static void promiseResolveSuccess(Promise promise, Object object) {
        Timber.d("promiseResolveSuccess promise [%s]", promise);
        if (promise == null) {
            return;
        }
        WritableMap item = Arguments.createMap();
        item.putInt("code", PaymentError.ERR_CODE_SUCCESS.value());
        if (object != null) {
            if (object instanceof WritableMap) {
                item.putMap("data", (WritableMap) object);
            } else if (object instanceof WritableArray) {
                item.putArray("data", (WritableArray) object);
            } else if (object instanceof Boolean) {
                item.putBoolean("data", (Boolean) object);
            } else if (object instanceof Double) {
                item.putDouble("data", (Double) object);
            } else if (object instanceof Long) {
                item.putDouble("data", (Long) object);
            } else if (object instanceof Integer) {
                item.putInt("data", (Integer) object);
            }
        }
        promise.resolve(item);
    }

    public static String readableMapToString(ReadableMap param) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        ReadableMapKeySetIterator iterator = param.keySetIterator();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            builder.append(key);
            builder.append(": ");
            ReadableType type = param.getType(key);
            switch (type) {
                case Boolean:
                    builder.append(param.getBoolean(key));
                    break;
                case String:
                    builder.append(param.getString(key));
                    break;
                case Number:
                    builder.append(param.getDouble(key));
                    break;
                case Null:
                    builder.append("NULL");
                case Map:
                    builder.append(readableMapToString(param.getMap(key)));
                    break;
                case Array: {
                    ReadableArray array = param.getArray(key);
                    builder.append(readableArrayToString(array));
                }
                default:
                    builder.append("UNSUPPORTED");
                    break;
            }
            builder.append(", ");
        }
        builder.append("}");
        return builder.toString();
    }

    public static String readableArrayToString(ReadableArray param) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int index = 0; index < param.size(); index ++) {
            ReadableType type = param.getType(index);
            switch (type) {
                case Boolean:
                    builder.append(param.getBoolean(index));
                    break;
                case String:
                    builder.append(param.getString(index));
                    break;
                case Number:
                    builder.append(param.getDouble(index));
                    break;
                case Null:
                    builder.append("NULL");
                case Map:
                    builder.append(readableMapToString(param.getMap(index)));
                    break;
                case Array: {
                    ReadableArray array = param.getArray(index);
                    builder.append(readableArrayToString(array));
                }
                default:
                    builder.append("UNSUPPORTED");
                    break;
            }
            builder.append(", ");
        }
        builder.append("]");
        return builder.toString();
    }
}
