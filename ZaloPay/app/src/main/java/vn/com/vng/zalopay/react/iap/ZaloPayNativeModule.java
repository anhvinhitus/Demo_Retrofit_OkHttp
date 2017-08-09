package vn.com.vng.zalopay.react.iap;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.facebook.react.views.text.ReactFontManager;
import com.zalopay.apploader.internal.ModuleName;
import com.zalopay.apploader.network.NetworkService;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.BundleConstants;
import vn.com.vng.zalopay.domain.Constants;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.model.ZPProfile;
import vn.com.vng.zalopay.event.TokenPaymentExpiredEvent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.paymentapps.PaymentAppConfig;
import vn.com.vng.zalopay.react.Helpers;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.FileDownloader;
import vn.com.zalopay.analytics.ZPAnalytics;

/**
 * Created by huuhoa on 5/16/16.
 * API for PaymentApp integration
 */
class ZaloPayNativeModule extends ReactContextBaseJavaModule
        implements ActivityEventListener, LifecycleEventListener {

    private static final int TOPUP_REQUEST_CODE = 100;
    private final IPaymentService mPaymentService;
    private final long mAppId; // AppId này là appid js cắm vào
    private final NetworkService mNetworkServiceWithRetry;
    private final User mUser;
    private CompositeSubscription compositeSubscription = new CompositeSubscription();
    private Navigator mNavigator;
    private WeakReference<Promise> mPromiseTopup = null;

    ZaloPayNativeModule(ReactApplicationContext reactContext,
                        User user,
                        IPaymentService paymentService,
                        long appId,
                        NetworkService networkServiceWithRetry,
                        Navigator navigator) {
        super(reactContext);
        this.mPaymentService = paymentService;
        this.mAppId = appId;
        this.mNetworkServiceWithRetry = networkServiceWithRetry;
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
        if (params == null) {
            if (promise != null) {
                Helpers.promiseResolveError(promise, PaymentError.ERR_CODE_INPUT.value(),
                        PaymentError.getErrorMessage(PaymentError.ERR_CODE_INPUT));
            }
            Timber.d("pay order with invalid params");
            return;
        }
        // verify params parameters
        try {
            long appId = (long) params.getDouble(Constants.APPID); // appid
            String transactionToken = params.hasKey(Constants.TRANSTOKEN) ?
                    params.getString(Constants.TRANSTOKEN) : null;
            if (!TextUtils.isEmpty(transactionToken) && getCurrentActivity() != null) {
                //pay with trans token
                mPaymentService.pay(getCurrentActivity(), promise, appId, transactionToken);
                return;
            }
            //pay with order info
            Order order = new Order(
//            long appid, String zptranstoken, String apptransid, String appuser, long apptime,
//            String embeddata, String item, long amount, String description, String payoption, String mac

                    appId, // appid
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

            boolean validOrder = validateOrder(order, promise);
            if (getCurrentActivity() != null && validOrder) {
                mPaymentService.pay(getCurrentActivity(), promise, order);
            }

        } catch (Exception e) {
            Helpers.promiseResolveError(promise, PaymentError.ERR_CODE_INPUT.value(),
                    PaymentError.getErrorMessage(PaymentError.ERR_CODE_INPUT));
            Timber.d(e);
        }
    }

    private boolean validateOrder(Order order, Promise promise) {
        if (order.appid < 0) {
            reportInvalidParameter(promise, Constants.APPID);
            return false;
        }
        if (TextUtils.isEmpty(order.apptransid)) {
            reportInvalidParameter(promise, Constants.APPTRANSID);
            return false;
        }
        if (TextUtils.isEmpty(order.appuser)) {
            reportInvalidParameter(promise, Constants.APPUSER);
            return false;
        }
        if (order.apptime <= 0) {
            reportInvalidParameter(promise, Constants.APPTIME);
            return false;
        }
        if (order.amount <= 0) {
            reportInvalidParameter(promise, Constants.AMOUNT);
            return false;
        }
//            if (TextUtils.isEmpty(order.getItem())) {
//                reportInvalidParameter(promise, Constants.ITEM);
//                return;
//            }
        // allow empty description
        if (order.description == null) {
            reportInvalidParameter(promise, Constants.DESCRIPTION);
            return false;
        }
        if (TextUtils.isEmpty(order.mac)) {
            reportInvalidParameter(promise, Constants.MAC);
            return false;
        }
        return true;
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
        EventBus eventBus = AndroidApplication.instance().getAppComponent().eventBus();
        eventBus.post(new TokenPaymentExpiredEvent());
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
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        Timber.d("requestCode %s resultCode %s ", requestCode, resultCode);

        if (requestCode == TOPUP_REQUEST_CODE) {
            handleResultTopup(resultCode, data);
            return;
        }

        if (mPaymentService != null) {
            mPaymentService.onActivityResult(requestCode, resultCode, data);
        }
    }

    private WritableMap createTopupResponse(String phoneNumber, String phoneName, String avatar) {
        WritableMap resp = Arguments.createMap();
        resp.putString("phonenumber", phoneNumber);
        resp.putString("phonename", phoneName);
        resp.putString("avatar", avatar);
        return resp;
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
        Timber.d("requestWithoutRetry: baseUrl [%s] String content [%s]", baseUrl, content);

        ReadableMap queries = addQueriesUser(content);

        Subscription subscription = mNetworkServiceWithRetry.requestWithoutRetry(baseUrl, queries)
                .doOnError(Timber::d)
                .subscribeOn(Schedulers.io())
                .subscribe(new RequestSubscriber(promise));
        compositeSubscription.add(subscription);
    }

    private boolean shouldAddQueriesUser() {
        Timber.d("Should add QueriesUser: [appId:%s]",mAppId);
        return mAppId == BuildConfig.VOUCHER_APP_ID;
    }

    private ReadableMap addQueriesUser(@NonNull ReadableMap content) {
        if (!shouldAddQueriesUser()) {
            return content;
        }

        WritableMap writableMap = Arguments.createMap();
        writableMap.merge(content);

        WritableMap queries = Arguments.createMap();
        if (content.hasKey("query")) {
            ReadableMap map = content.getMap("query");
            queries.merge(map);
        }

        queries.putString("accesstoken", mUser.accesstoken);
        queries.putString("userid", mUser.zaloPayId);

        writableMap.putMap("query", queries);

        return writableMap;
    }

    @ReactMethod
    public void requestWithRetry(String baseUrl, ReadableMap content, Promise promise) {
        Timber.d("requestWithRetry: baseUrl [%s] String content [%s]", baseUrl, content);
        Subscription subscription = mNetworkServiceWithRetry.request(baseUrl, content)
                .doOnError(Timber::d)
                .subscribeOn(Schedulers.io())
                .subscribe(new RequestSubscriber(promise));
        compositeSubscription.add(subscription);
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

    @ReactMethod
    public void loadFontAsync(final String fontFamilyName, final String url, final Promise promise) {
        if (fontFamilyName.equalsIgnoreCase("") || url.equalsIgnoreCase("")) {
            promise.reject(new Exception("Do not leave param null"));
            return;
        }

        if (PaymentAppConfig.EXCEPT_LOAD_FONTS.contains(fontFamilyName.toLowerCase())) {
            promise.reject(new Exception("Can not load font " + fontFamilyName));
            return;
        }

        try {
            Timber.d("loadFontAsync fontPath %s", url);
            if (url.contains("file://")) {
                Typeface typeface = Typeface.createFromFile(new File(Uri.parse(url).getPath()));
                ReactFontManager.getInstance().setTypeface(fontFamilyName, Typeface.NORMAL, typeface);
                promise.resolve(Arguments.createMap());
            } else {
                FileDownloader.DownloadParam param = new FileDownloader.DownloadParam();
                param.fileName = fontFamilyName;
                param.url = url;
                param.dest = getReactApplicationContext().getFilesDir() + "/fonts/";
                param.callback = new FileDownloader.DownloadCallback() {
                    @Override
                    public void onSuccess(File dest) {
                        Typeface typeface = Typeface.createFromFile(dest);
//                        ReactFontManager.get().setTypeface("MerchantFont-" + fontFamilyName, Typeface.NORMAL, typeface);
                        ReactFontManager.getInstance().setTypeface(fontFamilyName, Typeface.NORMAL, typeface);
                        Timber.d("FileDownloader onSuccess");
                        promise.resolve(Arguments.createMap());
                    }

                    @Override
                    public void onFail(Exception e) {
                        promise.reject(e);
                    }
                };
                FileDownloader downloader = new FileDownloader();
                downloader.execute(param);
            }
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void navigateSupportCenter() {
        if (getCurrentActivity() != null) {
            mNavigator.startMiniAppActivity(getCurrentActivity(), ModuleName.SUPPORT_CENTER);
        }
    }

    @ReactMethod
    public void launchContactList(String phoneNumber, String viewMode, String navigationTitle, final Promise promise) {

        Activity activity = getCurrentActivity();
        if (activity == null) {
            Helpers.promiseResolveError(promise, PaymentError.ERR_CODE_FAIL.value(), "Current activity is null");
            return;
        }

        mPromiseTopup = new WeakReference<>(promise);

        // push data to bundle
        Bundle extras = new Bundle();
        extras.putString(BundleConstants.KEY_SEARCH, phoneNumber);
        extras.putString(BundleConstants.ZPC_VIEW_MODE, viewMode);
        extras.putString(BundleConstants.NAVIGATION_TITLE, navigationTitle);
        mNavigator.startZaloPayContactTopup(activity, extras, TOPUP_REQUEST_CODE);
    }

    private void handleResultTopup(int resultCode, Intent data) {
        if (mPromiseTopup == null || mPromiseTopup.get() == null) {
            return;
        }

        try {

            Promise promise = mPromiseTopup.get();
            if (resultCode != Activity.RESULT_OK || data == null) {
                Helpers.promiseResolveError(promise, PaymentError.ERR_CODE_USER_CANCEL.value(), "User cancel");
                return;
            }

            ZPProfile profile = data.getParcelableExtra("profile");

            if (profile == null || TextUtils.isEmpty(profile.phonenumber)) {
                Timber.d("Profile invalid");
                Helpers.promiseResolveError(promise, PaymentError.ERR_CODE_FAIL.value(), "Data invalid");
                return;
            }
            WritableMap resp = createTopupResponse(profile.phonenumber, profile.displayName, profile.avatar);
            Helpers.promiseResolveSuccess(promise, PaymentError.ERR_CODE_SUCCESS.value(), "", resp);
        } finally {
            if (mPromiseTopup != null) {
                mPromiseTopup.clear();
                mPromiseTopup = null;
            }
        }

    }

}
