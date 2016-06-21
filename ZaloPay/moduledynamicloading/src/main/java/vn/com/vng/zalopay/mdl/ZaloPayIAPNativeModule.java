package vn.com.vng.zalopay.mdl;

import android.content.Intent;
import android.text.TextUtils;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import timber.log.Timber;
import vn.com.vng.zalopay.domain.Constants;
import vn.com.vng.zalopay.domain.repository.ZaloPayIAPRepository;
import vn.com.vng.zalopay.mdl.error.PaymentError;

/**
 * Created by huuhoa on 5/16/16.
 * API for PaymentApp integration
 */


public class ZaloPayIAPNativeModule extends ReactContextBaseJavaModule implements ActivityEventListener, LifecycleEventListener {
//    final ZaloPayIAPRepository zaloPayIAPRepository;
    final IPaymentService paymentService;
    final long appId; // AppId này là appid js cắm vào

    public ZaloPayIAPNativeModule(ReactApplicationContext reactContext,
                                  ZaloPayIAPRepository zaloPayIAPRepository,
                                  IPaymentService paymentService, long appId) {
        super(reactContext);
//        this.zaloPayIAPRepository = zaloPayIAPRepository;
        this.paymentService = paymentService;
        this.appId = appId;

        getReactApplicationContext().addActivityEventListener(this);
        getReactApplicationContext().addLifecycleEventListener(this);

    }

    @Override
    public String getName() {
        return "ZaloPay";
    }

    /**
     * Tham khảo tài liệu: https://docs.google.com/a/vng.com.vn/document/d/1dYKPBXLF9JRwExXkc5XlQJiQKRxp19Gf8x8cbXvGSvA/edit?usp=sharing
     *
     * @param params  Chứa danh sách các thuộc tính cần thiết để gọi hàm thanh toán của SDK
     * @param promise Trả về kết quả thanh toán
     */
    @ReactMethod
    public void payOrder(ReadableMap params, Promise promise) {
        Timber.d("payOrder start");
        // verify params parameters
        try {
            long appID = (long) params.getDouble(Constants.APPID);
            String appTransID = params.getString(Constants.APPTRANSID);
            String appUser = params.getString(Constants.APPUSER);
            long appTime = (long) params.getDouble(Constants.APPTIME);
            long amount = (long) params.getDouble(Constants.AMOUNT);
            String itemName = params.getString(Constants.ITEM);
            String description = params.getString(Constants.DESCRIPTION);
            String embedData = params.getString(Constants.EMBEDDATA);
            String mac = params.getString(Constants.MAC);

            paymentService.pay(getCurrentActivity(), promise, appID, appTransID, appUser, appTime, amount, itemName, description, embedData, mac);
        } catch (Exception e) {
            errorCallback(promise, PaymentError.ERR_CODE_INPUT);
            //e.printStackTrace();
        }
    }

    private void errorCallback(Promise promise, int errorCode) {
        errorCallback(promise, errorCode, null);
    }

    private void errorCallback(Promise promise, int errorCode, String message) {
        if (promise == null) {
            return;
        }
        WritableMap item = Arguments.createMap();
        item.putInt("code", errorCode);
        if (!TextUtils.isEmpty(message)) {
            item.putString("message", message);
        }
        promise.resolve(item);
    }

    @ReactMethod
    public void getUserInfo(Promise promise) {
        paymentService.getUserInfo(promise, appId);
    }

    @ReactMethod
    public void closeModule() {
        Timber.d("close Module");
        if (getCurrentActivity() != null) {
            getCurrentActivity().finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("requestCode %s resultCode %s ", requestCode, resultCode);
    }

    @Override
    public void onHostResume() {
        Timber.d(" Actvity `onResume`");
    }

    @Override
    public void onHostPause() {
        Timber.d(" Actvity `onPause`");
    }

    @Override
    public void onHostDestroy() {
        Timber.d("Actvity `onDestroy");
        paymentService.destroyVariable();
    }

}
