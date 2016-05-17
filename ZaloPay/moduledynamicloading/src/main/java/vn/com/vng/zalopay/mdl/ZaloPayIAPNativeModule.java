package vn.com.vng.zalopay.mdl;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.domain.Constants;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.mdl.error.PaymentError;
import vn.zing.pay.zmpsdk.ZingMobilePayService;
import vn.zing.pay.zmpsdk.entity.ZPPaymentResult;
import vn.zing.pay.zmpsdk.entity.ZPWPaymentInfo;
import vn.zing.pay.zmpsdk.entity.enumeration.EPaymentChannel;
import vn.zing.pay.zmpsdk.entity.enumeration.EPaymentStatus;
import vn.zing.pay.zmpsdk.listener.ZPPaymentListener;

/**
 * Created by huuhoa on 5/16/16.
 */



public class ZaloPayIAPNativeModule extends ReactContextBaseJavaModule implements ActivityEventListener, LifecycleEventListener {



    ZaloPayRepository zaloPayRepository;
    User user;
    private PaymentListener paymentListener;

    public ZaloPayIAPNativeModule(ReactApplicationContext reactContext, ZaloPayRepository zaloPayRepository, User user) {
        super(reactContext);
        this.zaloPayRepository = zaloPayRepository;
        this.user = user;

        getReactApplicationContext().addActivityEventListener(this);
        getReactApplicationContext().addLifecycleEventListener(this);

    }

    @Override
    public String getName() {
        return "ZaloPayIAP";
    }


    private void destroyVariable() {
        paymentListener = null;
    }

    /**
     * Tham khảo tài liệu: https://docs.google.com/a/vng.com.vn/document/d/1dYKPBXLF9JRwExXkc5XlQJiQKRxp19Gf8x8cbXvGSvA/edit?usp=sharing
     *
     * @param params  Chứa danh sách các thuộc tính cần thiết để gọi hàm thanh toán của SDK
     * @param promise Trả về kết quả thanh toán
     */
    @ReactMethod
    public void payOrder(ReadableMap params, Promise promise) {
        Timber.tag("@@@@@@@@@@@@@@@@@@@@@").d("pay.................1");
        // verify params parameters
        long appID = (long)params.getDouble(Constants.APPID);
        String appTransID = params.getString(Constants.APPTRANSID);
        String appUser = params.getString(Constants.APPUSER);
        long appTime = (long)params.getDouble(Constants.APPTIME);
        long amount = (long)params.getDouble(Constants.AMOUNT);
        String itemName = params.getString(Constants.ITEM);
        String description = params.getString(Constants.DESCRIPTION);
        String embedData = params.getString(Constants.EMBEDDATA);
        String mac = params.getString(Constants.MAC);
        String chargeInfo = params.getString(Constants.CHARGEINFO);

        if (user == null || user.uid <= 0) {
            if (promise!=null) {
                handleResultError(promise, PaymentError.ERR_CODE_USER_INFO);
            }
            return;
        }

        ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();
        EPaymentChannel forcedPaymentChannel = null;
        paymentInfo.appID = appID;
        paymentInfo.zaloUserID = String.valueOf(user.uid);
        paymentInfo.zaloPayAccessToken = user.accesstoken;
        paymentInfo.appTime = appTime;
        paymentInfo.appTransID = appTransID;
        paymentInfo.itemName = itemName;
        paymentInfo.amount = amount;
        paymentInfo.description = description;
        paymentInfo.embedData = embedData;
        //lap vao ví appId = appUser = 1
        paymentInfo.appUser = appUser;
        paymentInfo.mac = mac;

        Timber.tag("@@@@@@@@@@@@@@@@@@@@@").d("pay.................3");
        paymentListener = new PaymentListener(promise);
        ZingMobilePayService.pay(getCurrentActivity(), forcedPaymentChannel, paymentInfo, paymentListener);
    }

    class PaymentListener implements ZPPaymentListener {

        private Promise promise;

        public PaymentListener(Promise promise) {
            this.promise = promise;
        }

        @Override
        public void onComplete(ZPPaymentResult zpPaymentResult) {
            if (zpPaymentResult == null) {
                if (!isNetworkAvailable(getReactApplicationContext())) {
                    handleResultError(promise, PaymentError.ERR_CODE_INTERNET);
                } else {
                    handleResultError(promise, PaymentError.ERR_CODE_SYSTEM);
                }
            } else {
                EPaymentStatus paymentStatus = zpPaymentResult.paymentStatus;
                if (paymentStatus.getNum() == EPaymentStatus.ZPC_TRANXSTATUS_SUCCESS.getNum()) {
                    handleResultSucess(promise, zpPaymentResult.paymentInfo);
                } else {
                    handleResultError(promise, String.valueOf(EPaymentStatus.ZPC_TRANXSTATUS_TOKEN_INVALID.getNum()), EPaymentStatus.ZPC_TRANXSTATUS_TOKEN_INVALID.toString());
                }
            }
            destroyVariable();
        }

        @Override
        public void onCancel() {
            handleResultError(promise, 0);
            destroyVariable();
        }

        @Override
        public void onSMSCallBack(String s) {
            //not use
        }
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void handleResultSucess(Promise promise, Object object) {
        getBalance();
        if (promise == null) {
            return;
        }
        promise.resolve(object);
    }

    private void handleResultError(Promise promise, int errorCode) {
        if (promise == null) {
            return;
        }
        promise.reject(String.valueOf(errorCode), PaymentError.getErrorMessage(errorCode));
    }

    private void handleResultError(Promise promise, String error, String message) {
        if (promise == null) {
            return;
        }
        promise.reject(error, message);
    }

    private void getBalance() {
        zaloPayRepository.balance()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
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
    }
}
