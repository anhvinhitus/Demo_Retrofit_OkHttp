package vn.com.vng.zalopay.mdl;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import java.util.Locale;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.domain.Constants;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.MerChantUserInfo;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayIAPRepository;
import vn.com.vng.zalopay.mdl.error.PaymentError;
import vn.com.zalopay.wallet.ZingMobilePayService;
import vn.com.zalopay.wallet.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.entity.enumeration.EPaymentChannel;
import vn.com.zalopay.wallet.entity.enumeration.EPaymentStatus;
import vn.com.zalopay.wallet.listener.ZPPaymentListener;

/**
 * Created by huuhoa on 5/16/16.
 * API for PaymentApp integration
 */


public class ZaloPayIAPNativeModule extends ReactContextBaseJavaModule implements ActivityEventListener, LifecycleEventListener {

    final ZaloPayIAPRepository zaloPayIAPRepository;

    final User user;

    private PaymentListener paymentListener;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    final long appId; // AppId này là appid js cắm vào

    public ZaloPayIAPNativeModule(ReactApplicationContext reactContext,
                                  ZaloPayIAPRepository zaloPayIAPRepository,
                                  User user, long appId) {
        super(reactContext);
        this.user = user;
        this.zaloPayIAPRepository = zaloPayIAPRepository;
        this.appId = appId;

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

            if (appID < 0) {
                reportInvalidParameter(promise, Constants.APPID);
                return;
            }
            if (TextUtils.isEmpty(appTransID)) {
                reportInvalidParameter(promise, Constants.APPTRANSID);
                return;
            }
            if (TextUtils.isEmpty(appUser)) {
                reportInvalidParameter(promise, Constants.APPUSER);
                return;
            }
            if (appTime <= 0) {
                reportInvalidParameter(promise, Constants.APPTIME);
                return;
            }
            if (amount <= 0) {
                reportInvalidParameter(promise, Constants.AMOUNT);
                return;
            }
            if (TextUtils.isEmpty(itemName)) {
                reportInvalidParameter(promise, Constants.ITEM);
                return;
            }
            if (TextUtils.isEmpty(embedData)) {
                reportInvalidParameter(promise, Constants.DESCRIPTION);
                return;
            }
            if (TextUtils.isEmpty(mac)) {
                reportInvalidParameter(promise, Constants.MAC);
                return;
            }

            if (user == null || user.uid <= 0) {
                errorCallback(promise, PaymentError.ERR_CODE_USER_INFO);
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
        } catch (Exception e) {
            errorCallback(promise, PaymentError.ERR_CODE_INPUT);
        }
    }

    @ReactMethod
    public void getUserInfo(Promise promise) {

        Timber.d("get user info appId %s", appId);

        Subscription subscription = zaloPayIAPRepository.getMerchantUserInfo(appId)
                .subscribe(new UserInfoSubscriber(promise));
        compositeSubscription.add(subscription);
    }

    @ReactMethod
    public void verifyAccessToken(String mUid, String mAccessToken, Promise promise) {

        Timber.d("verifyAccessToken %s %s", mUid, mAccessToken);

        Subscription subscription = zaloPayIAPRepository.verifyMerchantAccessToken(mUid, mAccessToken)
                .subscribe(new VerifyAccessToken(promise));
        compositeSubscription.add(subscription);
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

        unsubscribeIfNotNull(compositeSubscription);
        destroyVariable();
    }

    private void unsubscribeIfNotNull(CompositeSubscription subscription) {
        if (subscription != null) {
            subscription.clear();
        }
    }

    private void reportInvalidParameter(Promise promise, String parameterName) {
        if (promise == null) {
            return;
        }

        String message = String.format(Locale.getDefault(), "invalid %s", parameterName);
        Timber.w("Invalid parameter %s", parameterName);
        errorCallback(promise, PaymentError.ERR_CODE_INPUT, message);
    }

    private void successCallback(Promise promise, WritableMap object) {
        transactionUpdate();
        if (promise == null) {
            return;
        }
        WritableMap item = Arguments.createMap();
        item.putInt("code", PaymentError.ERR_CODE_SUCCESS);
        if (object != null) {
            item.putMap("data", object);
        }
        promise.resolve(item);
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

    private void transactionUpdate() {
        zaloPayIAPRepository.transactionUpdate()
                .subscribe(new DefaultSubscriber<Boolean>());
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
                    errorCallback(promise, PaymentError.ERR_CODE_INTERNET);
                    return;
                }

                errorCallback(promise, PaymentError.ERR_CODE_SYSTEM);
                return;
            }

            EPaymentStatus paymentStatus = zpPaymentResult.paymentStatus;
            if (paymentStatus == null) {
                errorCallback(promise, PaymentError.ERR_CODE_SYSTEM, PaymentError.getErrorMessage(PaymentError.ERR_CODE_SYSTEM));
            } else if (paymentStatus.getNum() == EPaymentStatus.ZPC_TRANXSTATUS_SUCCESS.getNum()) {
                successCallback(promise, null);
            } else {
                errorCallback(promise, paymentStatus.getNum(), paymentStatus.toString());
            }
        }

        @Override
        public void onCancel() {
            errorCallback(promise, PaymentError.ERR_CODE_USER_CANCEL);
            destroyVariable();
        }

        @Override
        public void onSMSCallBack(String s) {
            //not use
        }

        private boolean isNetworkAvailable(Context context) {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
    }

    private final class UserInfoSubscriber extends DefaultSubscriber<MerChantUserInfo> {

        private Promise promise;

        public UserInfoSubscriber(Promise promise) {
            this.promise = promise;
        }

        @Override
        public void onError(Throwable e) {

            Timber.e(e, "on error ", e);

            errorCallback(promise, getErrorCode(e));
        }

        @Override
        public void onNext(MerChantUserInfo merChantUserInfo) {

            Timber.d("get user info %s %s ", merChantUserInfo, merChantUserInfo.muid);

            successCallback(promise, transform(merChantUserInfo));
        }

        private WritableMap transform(MerChantUserInfo merChantUserInfo) {
            if (merChantUserInfo == null) {
                return null;
            }

            WritableMap data = Arguments.createMap();
            data.putString("mUid", merChantUserInfo.muid);
            data.putString("mAccessToken", merChantUserInfo.maccesstoken);
            data.putString("displayName", merChantUserInfo.displayname);
            data.putString("dateOfBirth", merChantUserInfo.birthdate);
            data.putString("gender", String.valueOf(merChantUserInfo.usergender));
            return data;
        }

        private int getErrorCode(Throwable e) {
            if (e instanceof BodyException) {
                return ((BodyException) e).errorCode;
            } else {
                return PaymentError.ERR_CODE_UNKNOWN;
            }
        }
    }

    private final class VerifyAccessToken extends DefaultSubscriber<Boolean> {
        private Promise promise;

        public VerifyAccessToken(Promise promise) {
            this.promise = promise;
        }

        @Override
        public void onNext(Boolean aBoolean) {

            Timber.d("verifyAccessToken onNext");
            successCallback(promise, null);
        }

        @Override
        public void onError(Throwable e) {

            Timber.e("on Error %s", e);

            errorCallback(promise, getErrorCode(e));
        }

        private int getErrorCode(Throwable e) {
            if (e instanceof BodyException) {
                return ((BodyException) e).errorCode;
            } else {
                return PaymentError.ERR_CODE_UNKNOWN;
            }
        }
    }
}
