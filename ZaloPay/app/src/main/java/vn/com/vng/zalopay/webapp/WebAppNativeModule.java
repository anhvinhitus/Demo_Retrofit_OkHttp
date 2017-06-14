package vn.com.vng.zalopay.webapp;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.zalopay.ui.widget.dialog.SweetAlertDialog;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;
import vn.com.vng.webapp.framework.NativeModule;
import vn.com.vng.webapp.framework.Promise;

/**
 * Created by khattn on 2/20/17.
 * Native module to provide native-to-web integration for web app
 */

class WebAppNativeModule implements NativeModule {
    private final IProcessMessageListener mProcessMessageListener;

    private final static String MESSAGE_PAY_ORDER = "payOrder";
    private final static String MESSAGE_TRANSFER_MONEY = "transferMoney";
    private final static String MESSAGE_SHOW_LOADING = "showLoading";
    private final static String MESSAGE_HIDE_LOADING = "hideLoading";
    private final static String MESSAGE_SHOW_DIALOG = "showDialog";
    private final static String MESSAGE_WRITE_LOG = "writeLog";
    private final static String MESSAGE_LAUNCH_APP = "promotionEvent";

    private final static int FUNCTION_PAY_ORDER = 1;
    private final static int FUNCTION_TRANSFER_MONEY = 2;
    private final static int FUNCTION_SHOW_LOADING = 3;
    private final static int FUNCTION_HIDE_LOADING = 4;
    private final static int FUNCTION_SHOW_DIALOG = 5;
    private final static int FUNCTION_WRITE_LOG = 6;
    private final static int FUNCTION_LAUNCH_APP = 7;

    private static Map<String, Integer> sMessageToFunctionMap;

    WebAppNativeModule(@NonNull IProcessMessageListener processMessageListener) {
        this.mProcessMessageListener = processMessageListener;
    }

    static {
        // static initialization
        sMessageToFunctionMap = new HashMap<>();
        sMessageToFunctionMap.put(MESSAGE_PAY_ORDER, FUNCTION_PAY_ORDER);
        sMessageToFunctionMap.put(MESSAGE_TRANSFER_MONEY, FUNCTION_TRANSFER_MONEY);
        sMessageToFunctionMap.put(MESSAGE_SHOW_LOADING, FUNCTION_SHOW_LOADING);
        sMessageToFunctionMap.put(MESSAGE_HIDE_LOADING, FUNCTION_HIDE_LOADING);
        sMessageToFunctionMap.put(MESSAGE_SHOW_DIALOG, FUNCTION_SHOW_DIALOG);
        sMessageToFunctionMap.put(MESSAGE_WRITE_LOG, FUNCTION_WRITE_LOG);
        sMessageToFunctionMap.put(MESSAGE_LAUNCH_APP, FUNCTION_LAUNCH_APP);
    }

    @Override
    public void processMessage(String messageName, String messageType, final JSONObject data, final Promise promise) {
        Timber.d("Invoke function: %s", messageName);
        if (TextUtils.isEmpty(messageName)) {
            return;
        }

        Integer function = sMessageToFunctionMap.get(messageName);
        if (function == null) {
            Timber.i("Unknown function name %s", messageName);
            return;
        }

        switch (function) {
            case FUNCTION_PAY_ORDER:
                processPayOrder(data, promise);
                break;
            case FUNCTION_TRANSFER_MONEY:
                transferMoney(data, promise);
                break;
            case FUNCTION_SHOW_LOADING:
                mProcessMessageListener.showLoading();
                promise.resolve(null);
                break;
            case FUNCTION_HIDE_LOADING:
                mProcessMessageListener.hideLoading();
                promise.resolve(null);
                break;
            case FUNCTION_SHOW_DIALOG:
                showDialog(data, promise);
                break;
            case FUNCTION_WRITE_LOG:
                writeLog(data, promise);
                break;
            case FUNCTION_LAUNCH_APP:
                launchApp(data, promise);
//                sendEmail(data, promise);
//                openDial(data, promise);
        }
    }

    @Override
    public String[] canProcessMessages() {
        return sMessageToFunctionMap.keySet().toArray(new String[0]);
    }

    private void processPayOrder(final JSONObject data, final Promise promise) {

        mProcessMessageListener.payOrder(data, new IPaymentListener() {
            @Override
            public void onPayError(String param) {
                promise.reject(WebAppConstants.RETURN_CODE_UNKNOWN_ERROR, param);
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

    private void transferMoney(final JSONObject data, final Promise promise) {

        mProcessMessageListener.transferMoney(data, new IPaymentListener() {
            @Override
            public void onPayError(String param) {
                promise.reject(WebAppConstants.RETURN_CODE_UNKNOWN_ERROR, param);
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

    private void showDialog(final JSONObject data, Promise promise) {
        String title = data.optString("title");
        String content = data.optString("message");
        String buttonLabel = data.optString("button");

        mProcessMessageListener.showDialog(SweetAlertDialog.NORMAL_TYPE, title, content, buttonLabel);

        promise.resolve(null);
    }

    private void writeLog(final JSONObject data, Promise promise) {
        String type = data.optString("type");
        long time = data.optLong("time");
        String arg = data.optString("data");

        mProcessMessageListener.writeLog(type, time, arg);
    }

    // message = {"func":"launchApp",
    // "param":{'url': 'zalo://launch?params=1', // use on iOS 'packageId': 'com.zing.zalo' // use on Android, if url is not available},
    // "msgType":"launch",
    // "clientId":"14865289272660.004411039873957634"}
    private void launchApp(final JSONObject data, Promise promise) {
        String strPackageID = data.optString("packageId");
        String strAlternateUrl = data.optString("alternateUrl");
        int nCampaignId = data.optInt("campaignId");
        int nInternalAppID = data.optInt("internalApp");

        if (!TextUtils.isEmpty(strPackageID)) {
            mProcessMessageListener.launchApp(strPackageID, strAlternateUrl);
        } else if (nInternalAppID > 0) {
            mProcessMessageListener.launchInternalApp(nInternalAppID);
        }

        promise.resolve(null);
    }

    // {"mailto":"hotro@zalopay.vn","cc":"","subject":"Hoàn tiền","content":"yêu cầu hoàn tiền thanh toán game"}
    private void sendEmail(final JSONObject data, Promise promise) {
        mProcessMessageListener.sendEmail();

        promise.resolve(null);
    }

    // {"phoneto":"0908133987"}
    private void openDial(final JSONObject data, Promise promise) {
        mProcessMessageListener.openDial();

        promise.resolve(null);
    }
}
