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

        getReactApplicationContext().addActivityEventListener(this);
        getReactApplicationContext().addLifecycleEventListener(this);

        this.appId = appId;

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
                if (promise != null) {
                    handleResultError(promise, PaymentError.ERR_CODE_INPUT);
                }
                return;
            }
            if (TextUtils.isEmpty(appTransID)) {
                if (promise != null) {
                    handleResultError(promise, PaymentError.ERR_CODE_INPUT);
                }
                return;
            }
            if (TextUtils.isEmpty(appUser)) {
                if (promise != null) {
                    handleResultError(promise, PaymentError.ERR_CODE_INPUT);
                }
                return;
            }
            if (appTime <= 0) {
                if (promise != null) {
                    handleResultError(promise, PaymentError.ERR_CODE_INPUT);
                }
                return;
            }
            if (amount <= 0) {
                if (promise != null) {
                    handleResultError(promise, PaymentError.ERR_CODE_INPUT);
                }
                return;
            }
            if (TextUtils.isEmpty(itemName)) {
                if (promise != null) {
                    handleResultError(promise, PaymentError.ERR_CODE_INPUT);
                }
                return;
            }
            if (TextUtils.isEmpty(embedData)) {
                handleResultError(promise, PaymentError.ERR_CODE_INPUT);
                return;
            }
            if (TextUtils.isEmpty(mac)) {
                handleResultError(promise, PaymentError.ERR_CODE_INPUT);
                return;
            }

            if (user == null || user.uid <= 0) {
                if (promise != null) {
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
        } catch (Exception e) {
            if (promise != null) {
                handleResultError(promise, PaymentError.ERR_CODE_INPUT);
            }
        }
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
                if (paymentStatus == null) {
                    handleResultError(promise, String.valueOf(PaymentError.ERR_CODE_SYSTEM), PaymentError.getErrorMessage(PaymentError.ERR_CODE_SYSTEM));
                } else if (paymentStatus.getNum() == EPaymentStatus.ZPC_TRANXSTATUS_SUCCESS.getNum()) {
                    handleResultSucess(promise, null);
                } else {
                    handleResultError(promise, String.valueOf(paymentStatus.getNum()), paymentStatus.toString());
                }
            }
        }

        @Override
        public void onCancel() {
            handleResultError(promise, PaymentError.ERR_CODE_USER_CANCEL);
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
        transactionUpdate();
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

    private void transactionUpdate() {
        zaloPayIAPRepository.transactionUpdate()
                .subscribe(new DefaultSubscriber<Boolean>());
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

    public void unsubscribeIfNotNull(CompositeSubscription subscription) {
        if (subscription != null) {
            subscription.clear();
        }
    }

    @ReactMethod
    public void getUserInfo(Promise promise) {

        Timber.d("get user info appId %s", appId);

        Subscription subscription = zaloPayIAPRepository.getMerchantUserInfo(2)
                .subscribe(new UserInfoSubscriber(promise));
        compositeSubscription.add(subscription);
    }

    private final class UserInfoSubscriber extends DefaultSubscriber<MerChantUserInfo> {

        private Promise promise;

        public UserInfoSubscriber(Promise promise) {
            this.promise = promise;
        }

        @Override
        public void onError(Throwable e) {

            Timber.e(e, "on error ", e);

            promise.resolve(handleResultError(e));
        }

        @Override
        public void onNext(MerChantUserInfo merChantUserInfo) {

            Timber.d("get user info %s %s ", merChantUserInfo, merChantUserInfo.muid);

            promise.resolve(transform(merChantUserInfo));
        }
    }

    private WritableMap transform(MerChantUserInfo merChantUserInfo) {
        if (merChantUserInfo == null) return null;
        WritableMap item = Arguments.createMap();
        item.putInt("code", 1);

        WritableMap data = Arguments.createMap();
        data.putString("mUid", merChantUserInfo.muid);
        data.putString("displayName", merChantUserInfo.displayname);
        data.putString("dateOfBirth", merChantUserInfo.birthdate);
        data.putString("gender", String.valueOf(merChantUserInfo.usergender));
        data.putString("mAccessToken", merChantUserInfo.maccesstoken);
        item.putMap("data", data);
        return item;
    }

    @ReactMethod
    public void verifyAccessToken(String mUid, String mAccessToken, Promise promise) {

        Timber.d("verifyAccessToken %s %s", mUid, mAccessToken);

        Subscription subscription = zaloPayIAPRepository.verifyMerchantAccessToken(mUid, mAccessToken)
                .subscribe(new VerifyAccessToken(promise));
        compositeSubscription.add(subscription);
    }

    private final class VerifyAccessToken extends DefaultSubscriber<Boolean> {
        private Promise promise;

        public VerifyAccessToken(Promise promise) {
            this.promise = promise;
        }

        @Override
        public void onNext(Boolean aBoolean) {

            Timber.d("verifyAccessToken onNext");

            WritableMap item = Arguments.createMap();
            item.putInt("code", 1);
            promise.resolve(item);
        }

        @Override
        public void onError(Throwable e) {

            Timber.e("on Error %s", e);

            promise.resolve(handleResultError(e));
        }
    }

    private WritableMap handleResultError(Throwable e) {
        WritableMap item = Arguments.createMap();
        if (e instanceof BodyException) {
            item.putInt("code", ((BodyException) e).errorCode);
        } else {
            item.putInt("code", -1);
        }
        return item;
    }
}
