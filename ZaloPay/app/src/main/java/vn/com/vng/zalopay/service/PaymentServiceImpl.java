package vn.com.vng.zalopay.service;

import android.app.Activity;
import android.text.TextUtils;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;

import java.util.Locale;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.MerChantUserInfo;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.BalanceRepository;
import vn.com.vng.zalopay.domain.repository.ZaloPayIAPRepository;
import vn.com.vng.zalopay.mdl.IPaymentService;
import vn.com.vng.zalopay.mdl.error.PaymentError;
import vn.com.zalopay.wallet.entity.base.ZPPaymentResult;

/**
 * Created by longlv on 02/06/2016.
 *
 */
public class PaymentServiceImpl implements IPaymentService {

    final ZaloPayIAPRepository zaloPayIAPRepository;
    final BalanceRepository mBalanceRepository;
    final User user;
//    private PaymentListener paymentListener;
    private PaymentWrapper paymentWrapper;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    public PaymentServiceImpl(ZaloPayIAPRepository zaloPayIAPRepository, BalanceRepository balanceRepository, User user) {
        this.zaloPayIAPRepository = zaloPayIAPRepository;
        this.mBalanceRepository = balanceRepository;
        this.user = user;
    }

    @Override
    public void pay(final Activity activity, final Promise promise, long appID, String appTransID, String appUser, long appTime, long amount, String itemName, String description, String embedData, String mac) {
        this.paymentWrapper = new PaymentWrapper(null, new PaymentWrapper.IViewListener() {
            @Override
            public Activity getActivity() {
                return activity;
            }
        }, new PaymentWrapper.IResponseListener() {
            @Override
            public void onParameterError(String param) {
                reportInvalidParameter(promise, param);
            }

            @Override
            public void onResponseError(int status) {
                errorCallback(promise, status);
            }

            @Override
            public void onResponseSuccess(ZPPaymentResult zpPaymentResult) {
                successCallback(promise, null);
            }

            @Override
            public void onResponseTokenInvalid() {

            }

            @Override
            public void onResponseCancel() {
                errorCallback(promise, PaymentError.ERR_CODE_USER_CANCEL);
                destroyVariable();
            }
        });

        this.paymentWrapper.payWithDetail(appID, appTransID, appUser, appTime, amount, itemName, description, embedData, mac);
//
//        if (appID < 0) {
//            reportInvalidParameter(promise, Constants.APPID);
//            return;
//        }
//        if (TextUtils.isEmpty(appTransID)) {
//            reportInvalidParameter(promise, Constants.APPTRANSID);
//            return;
//        }
//        if (TextUtils.isEmpty(appUser)) {
//            reportInvalidParameter(promise, Constants.APPUSER);
//            return;
//        }
//        if (appTime <= 0) {
//            reportInvalidParameter(promise, Constants.APPTIME);
//            return;
//        }
//        if (amount <= 0) {
//            reportInvalidParameter(promise, Constants.AMOUNT);
//            return;
//        }
//        if (TextUtils.isEmpty(itemName)) {
//            reportInvalidParameter(promise, Constants.ITEM);
//            return;
//        }
//        if (TextUtils.isEmpty(embedData)) {
//            reportInvalidParameter(promise, Constants.DESCRIPTION);
//            return;
//        }
//        if (TextUtils.isEmpty(mac)) {
//            reportInvalidParameter(promise, Constants.MAC);
//            return;
//        }
//
//        if (user == null || user.uid <= 0 || TextUtils.isEmpty(user.accesstoken)) {
//            errorCallback(promise, PaymentError.ERR_CODE_USER_INFO);
//            return;
//        }
//
//        ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();
//        EPaymentChannel forcedPaymentChannel = null;
//        paymentInfo.appID = appID;
//        paymentInfo.zaloUserID = String.valueOf(user.uid);
//        paymentInfo.zaloPayAccessToken = user.accesstoken;
//        paymentInfo.appTime = appTime;
//        paymentInfo.appTransID = appTransID;
//        paymentInfo.itemName = itemName;
//        paymentInfo.amount = amount;
//        paymentInfo.description = description;
//        paymentInfo.embedData = embedData;
//        //lap vao ví appId = appUser = 1
//        paymentInfo.appUser = appUser;
//        paymentInfo.mac = mac;
//
//        Timber.tag("@@@@@@@@@@@@@@@@@@@@@").d("pay.................3");
//        paymentListener = new PaymentListener(promise);
//        ZingMobilePayService.pay(activity, forcedPaymentChannel, paymentInfo, paymentListener);
    }

//    class PaymentListener implements ZPPaymentListener {
//
//        private Promise promise;
//
//        public PaymentListener(Promise promise) {
//            this.promise = promise;
//        }
//
//        @Override
//        public void onComplete(ZPPaymentResult zpPaymentResult) {
//            if (zpPaymentResult == null) {
//                if (!isNetworkAvailable(AndroidApplication.instance().getApplicationContext())) {
//                    errorCallback(promise, PaymentError.ERR_CODE_INTERNET);
//                    return;
//                }
//
//                errorCallback(promise, PaymentError.ERR_CODE_SYSTEM);
//                return;
//            }
//
//            EPaymentStatus paymentStatus = zpPaymentResult.paymentStatus;
//            if (paymentStatus == null) {
//                errorCallback(promise, PaymentError.ERR_CODE_SYSTEM, PaymentError.getErrorMessage(PaymentError.ERR_CODE_SYSTEM));
//            } else if (paymentStatus.getNum() == EPaymentStatus.ZPC_TRANXSTATUS_SUCCESS.getNum()) {
//                successCallback(promise, null);
//            } else {
//                errorCallback(promise, paymentStatus.getNum(), paymentStatus.toString());
//            }
//        }
//
//        @Override
//        public void onCancel() {
//            errorCallback(promise, PaymentError.ERR_CODE_USER_CANCEL);
//            destroyVariable();
//        }
//
//        @Override
//        public void onSMSCallBack(String s) {
//            //not use
//        }
//
//        private boolean isNetworkAvailable(Context context) {
//            ConnectivityManager connectivityManager
//                    = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
//            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
//        }
//    }

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
        Timber.d("Invalid parameter %s", parameterName);
        errorCallback(promise, PaymentError.ERR_CODE_INPUT, message);
    }

    private void successCallback(Promise promise, WritableMap object) {
        transactionUpdate();
        balanceUpdate();
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

    @Override
    public void getUserInfo(Promise promise, long appId) {

        Timber.d("get user info appId %s", appId);

        Subscription subscription = zaloPayIAPRepository.getMerchantUserInfo(appId)
                .subscribe(new UserInfoSubscriber(promise));
        compositeSubscription.add(subscription);
    }

    @Override
    public void verifyAccessToken(String mUid, String mAccessToken, Promise promise) {

        Timber.d("verifyAccessToken %s %s", mUid, mAccessToken);

        Subscription subscription = zaloPayIAPRepository.verifyMerchantAccessToken(mUid, mAccessToken)
                .subscribe(new VerifyAccessToken(promise));
        compositeSubscription.add(subscription);
    }

    public void destroyVariable() {
//        paymentListener = null;
        paymentWrapper = null;
        unsubscribeIfNotNull(compositeSubscription);
    }

    private void transactionUpdate() {
        zaloPayIAPRepository.transactionUpdate()
                .subscribe(new DefaultSubscriber<Boolean>());

    }

    private void balanceUpdate() {
        // update balance
        mBalanceRepository.updateBalance().subscribe(new DefaultSubscriber<>());
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
