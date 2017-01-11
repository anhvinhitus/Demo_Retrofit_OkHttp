package vn.com.vng.zalopay.react;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import timber.log.Timber;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.data.exception.NetworkConnectionException;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.react.listener.DialogSimpleEventListener;
import vn.com.vng.zalopay.react.listener.SweetDialogSimpleEventListener;
import vn.com.vng.zalopay.react.model.DialogType;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.listener.ZPWOnSweetDialogListener;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * Created by huuhoa on 7/14/16.
 * Internal helpers
 */
public class Helpers {

    public static void promiseReject(Promise promise, String code, String message) {
        promise.reject(code, message);
    }

    public static void promiseResolveError(@Nullable Promise promise, @NonNull Pair<Integer, String> error) {
        promiseResolveError(promise, error.first, error.second);
    }

    public static void promiseResolveError(@Nullable Promise promise, int errorCode, String message) {
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

    public static void promiseResolve(Promise promise, int btnIndex) {
        Timber.d("promiseResolve promise [%s] btnIndex [%s]", promise, btnIndex);
        if (promise == null) {
            return;
        }
        WritableMap item = Arguments.createMap();
        item.putInt("code", btnIndex);
        promise.resolve(item);
    }

    public static void promiseResolveSuccess(Promise promise, int code, String message, Object object) {
        Timber.d("promiseResolveSuccess promise [%s]", promise);
        if (promise == null) {
            return;
        }
        WritableMap item = Arguments.createMap();
        item.putInt("code", code);

        if (!TextUtils.isEmpty(message)) {
            item.putString("message", message);
        }

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

    public static void promiseResolveSuccess(Promise promise, Object object) {
        promiseResolveSuccess(promise, PaymentError.ERR_CODE_SUCCESS.value(), "", object);
    }

    public static String readableMapToString(ReadableMap param) {
        if (param == null) {
            return "";
        }
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
        if (param == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int index = 0; index < param.size(); index++) {
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

    public static void showLoading(final Activity activity) {
        AndroidUtils.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                DialogHelper.showLoading(activity, null);
            }
        });
    }

    public static void hideLoading() {
        AndroidUtils.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                DialogHelper.hideLoading();
            }
        });
    }

    public static void showDialog(final Activity activity, final int dialogType,
                                  final String title, final String message, final ReadableArray btnNames,
                                  final Promise promise) {
        if (btnNames == null || btnNames.size() <= 0) {
            return;
        }
        AndroidUtils.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                showDialogInUIThread(activity, dialogType, title, message, btnNames, promise);
            }
        });
    }

    private static void showDialogInUIThread(Activity activity,
                                             int dialogType,
                                             String title,
                                             String message,
                                             ReadableArray btnNames,
                                             final Promise promise) {
        if (dialogType == DialogType.NO_INTERNET_TYPE) {
            DialogHelper.showNetworkErrorDialog(activity, new SweetDialogSimpleEventListener(promise, 0));
        } else {
            int sweetAlertType;
            switch (dialogType) {
                case DialogType.NORMAL_TYPE:
                    sweetAlertType = SweetAlertDialog.INFO_NO_ICON;
                    break;

                case DialogType.ERROR_TYPE:
                    sweetAlertType = SweetAlertDialog.ERROR_TYPE;
                    break;

                case DialogType.SUCCESS_TYPE:
                    sweetAlertType = SweetAlertDialog.SUCCESS_TYPE;
                    break;

                case DialogType.WARNING_TYPE:
                    sweetAlertType = SweetAlertDialog.WARNING_TYPE;
                    break;

                case DialogType.NOTIFICATION_TYPE:
                    sweetAlertType = SweetAlertDialog.NORMAL_TYPE;
                    break;

                default:
                    sweetAlertType = SweetAlertDialog.INFO_NO_ICON;
            }

            DialogHelper.showCustomDialog(activity,
                    sweetAlertType,
                    title,
                    message,
                    new ZPWOnSweetDialogListener() {
                        @Override
                        public void onClickDiaLog(int i) {
                            if (promise == null) {
                                return;
                            }
                            Helpers.promiseResolve(promise, i);
                        }
                    },
                    convert(btnNames));
        }
    }

    private static String[] convert(ReadableArray btnNames) {
        if (btnNames == null) {
            return null;
        }
        List<String> buttons = new ArrayList<>();
        for (int i = 0; i < btnNames.size(); i++) {
            String btn = btnNames.getString(i);
            if (!TextUtils.isEmpty(btn)) {
                buttons.add(btn);
            }
        }
        String[] buttonArr = new String[buttons.size()];
        buttonArr = buttons.toArray(buttonArr);
        return buttonArr;
    }

    public static Pair<Integer, String> createReactError(Context context, Throwable exception) {
        String message = ErrorMessageFactory.create(context, exception);
        int code = -1;

        if (exception instanceof BodyException) {
            code = ((BodyException) exception).errorCode;
        } else if (exception instanceof TimeoutException) {
            code = -1001;
        } else if (exception instanceof NetworkConnectionException) {
            code = -1005;
        }

        return new Pair<>(code, message);
    }
}
