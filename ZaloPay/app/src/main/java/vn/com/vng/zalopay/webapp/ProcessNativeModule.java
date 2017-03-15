package vn.com.vng.zalopay.webapp;

import org.json.JSONObject;

import vn.com.vng.webapp.framework.NativeModule;
import vn.com.vng.webapp.framework.Promise;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * Created by khattn on 2/20/17.
 * *
 */

public class ProcessNativeModule implements NativeModule {
    private IProcessMessageListener mProcessMessageListener;

    private static String MESSAGE_PAY_ORDER = "payOrder";
    private static String MESSAGE_SHOW_LOADING = "showLoading";
    private static String MESSAGE_HIDE_LOADING = "hideLoading";
    private static String MESSAGE_SHOW_DIALOG = "showDialog";
    private static String MESSAGE_WRITE_LOG = "writeLog";

    ProcessNativeModule(IProcessMessageListener processMessageListener) {
        this.mProcessMessageListener = processMessageListener;
    }

    @Override
    public void processMessage(String messageName, String messageType, final JSONObject data, final Promise promise) {
        if (MESSAGE_PAY_ORDER.equalsIgnoreCase(messageName)) {
            processPayOrder(data, promise);
        } else if (MESSAGE_SHOW_LOADING.equalsIgnoreCase(messageName)) {
            if (mProcessMessageListener != null) {
                mProcessMessageListener.showLoading();
            }

            promise.resolve(null);
        } else if (MESSAGE_HIDE_LOADING.equalsIgnoreCase(messageName)) {
            if (mProcessMessageListener != null) {
                mProcessMessageListener.hideLoading();
            }

            promise.resolve(null);
        } else if (MESSAGE_SHOW_DIALOG.equalsIgnoreCase(messageName)) {
            showDialog(data, promise);
        } else if (MESSAGE_WRITE_LOG.equalsIgnoreCase(messageName)) {
            writeLog(data, promise);
        }
    }

    @Override
    public String[] canProcessMessages() {
        return new String[] { MESSAGE_PAY_ORDER, MESSAGE_SHOW_LOADING, MESSAGE_HIDE_LOADING,
                MESSAGE_SHOW_DIALOG, MESSAGE_WRITE_LOG, };
    }

    private void processPayOrder(final JSONObject data, final Promise promise) {
        if (mProcessMessageListener == null) {
            promise.reject(0, "Missing webview listener.");
        } else {
            mProcessMessageListener.payOrder(data, new IPaymentListener() {
                @Override
                public void onPayError(String param) {
                    promise.reject(0, param);
                }

                @Override
                public void onPayError(int code, String message) {
                    promise.reject(code, message);
                }

                @Override
                public void onPaySuccess() {
                    promise.resolve(null);
                }
            });
        }
    }

    private void showDialog(final JSONObject data, Promise promise) {
        String title = data.optString("title");
        String content = data.optString("message");
        String buttonLabel = data.optString("button");

        if (mProcessMessageListener != null) {
            mProcessMessageListener.showDialog(SweetAlertDialog.NORMAL_TYPE, title, content, buttonLabel);
        }

        promise.resolve(null);
    }

    private void writeLog(final JSONObject data, Promise promise) {
        String type = data.optString("type");
        long time = data.optLong("time");
        String arg = data.optString("data");

        if (mProcessMessageListener != null) {
            mProcessMessageListener.writeLog(type, time, arg);
        }
    }
}
