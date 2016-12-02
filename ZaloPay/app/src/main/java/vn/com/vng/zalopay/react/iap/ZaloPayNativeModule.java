package vn.com.vng.zalopay.react.iap;

import android.content.Intent;
import android.text.TextUtils;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.zalopay.apploader.network.NetworkService;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.Constants;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ApplicationSession;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.paymentapps.PaymentAppConfig;
import vn.com.vng.zalopay.react.Helpers;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.zalopay.analytics.ZPAnalytics;

/**
 * Created by huuhoa on 5/16/16.
 * API for PaymentApp integration
 */
class ZaloPayNativeModule extends ReactContextBaseJavaModule
        implements ActivityEventListener, LifecycleEventListener {

    private final IPaymentService mPaymentService;
    private final long mAppId; // AppId này là appid js cắm vào
    private final NetworkService mNetworkServiceWithRetry;
    private final NetworkService mNetworkServiceWithoutRetry;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();
    private final User mUser;
    private Navigator mNavigator;

    ZaloPayNativeModule(ReactApplicationContext reactContext,
                        User user,
                        IPaymentService paymentService,
                        long appId,
                        NetworkService networkServiceWithRetry,
                        NetworkService networkServiceWithoutRetry,
                        Navigator navigator) {
        super(reactContext);
        this.mPaymentService = paymentService;
        this.mAppId = appId;
        this.mNetworkServiceWithRetry = networkServiceWithRetry;
        this.mNetworkServiceWithoutRetry = networkServiceWithoutRetry;
        this.mUser = user;
        this.mNavigator = navigator;

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
        Timber.d("payOrder start with params: %s", Helpers.readableMapToString(params));

        // verify params parameters
        try {
            Order order = new Order(
//            long appid, String zptranstoken, String apptransid, String appuser, long apptime,
//            String embeddata, String item, long amount, String description, String payoption, String mac

                (long) params.getDouble(Constants.APPID), // appid
                "", // zptranstoken
                params.getString(Constants.APPTRANSID), // apptransid
                params.getString(Constants.APPUSER), // appuser
                (long) params.getDouble(Constants.APPTIME), // apptime
                params.getString(Constants.EMBEDDATA), // embeddata
                params.getString(Constants.ITEM), // item
                (long) params.getDouble(Constants.AMOUNT), // amount
                params.getString(Constants.DESCRIPTION), // description
                "",
                params.getString(Constants.MAC)
            );

            if (order.appid < 0) {
                reportInvalidParameter(promise, Constants.APPID);
                return;
            }
            if (TextUtils.isEmpty(order.apptransid)) {
                reportInvalidParameter(promise, Constants.APPTRANSID);
                return;
            }
            if (TextUtils.isEmpty(order.appuser)) {
                reportInvalidParameter(promise, Constants.APPUSER);
                return;
            }
            if (order.apptime <= 0) {
                reportInvalidParameter(promise, Constants.APPTIME);
                return;
            }
            if (order.amount <= 0) {
                reportInvalidParameter(promise, Constants.AMOUNT);
                return;
            }
//            if (TextUtils.isEmpty(order.getItem())) {
//                reportInvalidParameter(promise, Constants.ITEM);
//                return;
//            }
            if (TextUtils.isEmpty(order.description)) {
                reportInvalidParameter(promise, Constants.DESCRIPTION);
                return;
            }
            if (TextUtils.isEmpty(order.mac)) {
                reportInvalidParameter(promise, Constants.MAC);
                return;
            }

            if (getCurrentActivity() != null) {
                mPaymentService.pay(getCurrentActivity(), promise, order);
            }

        } catch (Exception e) {
            Helpers.promiseResolveError(promise, PaymentError.ERR_CODE_INPUT.value(),
                    PaymentError.getErrorMessage(PaymentError.ERR_CODE_INPUT));
            //e.printStackTrace();
        }
    }

    @ReactMethod
    public void getUserInfo(Promise promise) {
        mPaymentService.getUserInfo(promise, mAppId);
    }

    @ReactMethod
    public void closeModule(String moduleId) {
        Timber.d("close Module");
        if (getCurrentActivity() != null) {
            getCurrentActivity().finish();
        }
    }

    @ReactMethod
    public void logError(String message) {
        Timber.w(message);
    }

    @ReactMethod
    public void logout() {
        Timber.d("Payment app %s request to logout", mAppId);
        ApplicationSession applicationSession = AndroidApplication.instance().getAppComponent().applicationSession();
        applicationSession.setMessageAtLogin(R.string.exception_token_expired_message);
        applicationSession.clearUserSession();
    }

    @ReactMethod
    public void trackEvent(Integer eventId, Integer eventValue) {
        Timber.d("trackEvent eventId %s", eventId);
        if (eventValue != null) {
            long value = eventValue;
            ZPAnalytics.trackEvent(eventId, value);
        } else {
            ZPAnalytics.trackEvent(eventId);
        }
    }


    @ReactMethod
    public void shareMessageToOtherApp(String message, Promise promise) {
        if (getCurrentActivity() != null) {
            mPaymentService.shareMessageToOtherApp(getCurrentActivity(), message);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("requestCode %s resultCode %s ", requestCode, resultCode);
    }

    /**
     * Called when a new intent is passed to the activity
     *
     * @param intent
     */
    @Override
    public void onNewIntent(Intent intent) {
        Timber.d("onNewIntent called from based");
    }

    @Override
    public void onHostResume() {
        Timber.d(" Activity `onResume`");
    }

    @Override
    public void onHostPause() {
        Timber.d(" Activity `onPause`");
    }

    @Override
    public void onHostDestroy() {
        Timber.d("Activity `onDestroy");
        mPaymentService.destroyVariable();

        if (compositeSubscription != null) {
            compositeSubscription.clear();
        }
    }

    private void reportInvalidParameter(Promise promise, String parameterName) {
        if (promise == null) {
            return;
        }

        String message = String.format(Locale.getDefault(), "invalid %s", parameterName);
        Timber.d("Invalid parameter [%s]", parameterName);
        Helpers.promiseResolveError(promise, PaymentError.ERR_CODE_INPUT.value(), message);
    }


    @ReactMethod
    public void showLoading() {
        Helpers.showLoading(getCurrentActivity());
    }

    @ReactMethod
    public void hideLoading() {
        Helpers.hideLoading();
    }

    @ReactMethod
    public void showDialog(int dialogType, String title, String message, ReadableArray btnNames, final Promise promise) {
        Helpers.showDialog(getCurrentActivity(), dialogType, title, message, btnNames, promise);
    }

    @ReactMethod
    public void request(String baseUrl, ReadableMap content, Promise promise) {
        Timber.d("request: baseUrl [%s] String content [%s]", baseUrl, content);


        ReadableMap readableMap = shouldPostAuthKey(content);

        Subscription subscription = mNetworkServiceWithoutRetry.request(baseUrl, readableMap)
                .subscribeOn(Schedulers.io())
                .subscribe(new RequestSubscriber(promise));
        compositeSubscription.add(subscription);
    }

    @ReactMethod
    public void requestWithRetry(String baseUrl, ReadableMap content, Promise promise) {
        Timber.d("request: baseUrl [%s] String content [%s]", baseUrl, content);


        ReadableMap readableMap = shouldPostAuthKey(content);

        Subscription subscription = mNetworkServiceWithRetry.request(baseUrl, readableMap)
                .subscribeOn(Schedulers.io())
                .subscribe(new RequestSubscriber(promise));
        compositeSubscription.add(subscription);
    }

    private ReadableMap shouldPostAuthKey(ReadableMap content) {

        if (mAppId == PaymentAppConfig.Constants.SHOW_SHOW) {
            WritableMap writableMap = Arguments.createMap();
            if (content != null) {
                writableMap.merge(content);
            }
            WritableMap queryMap = Arguments.createMap();
            if (content.hasKey("query")) {
                queryMap.merge(content.getMap("query"));
            }

            queryMap.putString("accesstoken", mUser.accesstoken);
            queryMap.putString("userid", mUser.zaloPayId);

            writableMap.putMap("query", queryMap);
            return writableMap;
        }

        return content;
    }

    @Override
    public Map<String, Object> getConstants() {
        Map<String, Object> map = new HashMap<>();
        map.put("dscreentype", AndroidUtils.getScreenType());
        map.put("platform", "android");
        return map;
    }

    @ReactMethod
    public void showNoInternetConnection() {
        if (getCurrentActivity() != null) {
            mNavigator.startTutorialConnectInternetActivity(getCurrentActivity());
        }
    }
}
