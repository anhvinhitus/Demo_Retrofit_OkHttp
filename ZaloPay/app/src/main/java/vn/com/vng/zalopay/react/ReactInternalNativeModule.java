package vn.com.vng.zalopay.react;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.navigation.INavigator;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.view.dialog.DialogManager;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * Created by huuhoa on 4/25/16.
 * Internal API
 */
public class ReactInternalNativeModule extends ReactContextBaseJavaModule {

    INavigator navigator;

    public ReactInternalNativeModule(ReactApplicationContext reactContext,
                                     INavigator navigator) {
        super(reactContext);
        this.navigator = navigator;
    }

    /// The purpose of this method is to return the string name of the NativeModule
    /// which represents this class in JavaScript. So here we will call this ZaloPayInternal
    /// so that we can access it through React.NativeModules.ZaloPayInternal in JavaScript.
    @Override
    public String getName() {
        return "ZaloPayApi";
    }

    /// To expose a method to JavaScript a Java method must be annotated using @ReactMethod.
    /// The return type of bridge methods is always void.
    /// React Native bridge is asynchronous, so the only way to pass a result to JavaScript is
    /// by using callbacks or emitting events.
    @ReactMethod
    public void show(String message, int duration) {
        Toast.makeText(getReactApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    /// Request ZaloPayInternal API
    @ReactMethod
    public void request(String methodName, ReadableMap parameters, Promise promise) {
        WritableMap result = Arguments.createMap();
        result.merge(parameters);
        result.putString("method", methodName);
        promise.resolve(result);
    }

    @ReactMethod
    public void closeModule(String moduleId) {
        Timber.d("close Module");
        if (getCurrentActivity() != null) {
            getCurrentActivity().finish();
        }
    }

    @ReactMethod
    public void navigateLinkCard() {
        Timber.d("navigateLinkCard");
        if (getCurrentActivity() != null) {
            navigator.startLinkCardActivity(getCurrentActivity());
        }
    }

    @ReactMethod
    public void navigateProfile() {
        Timber.d("navigateProfile");
        if (getCurrentActivity() != null) {
            navigator.startProfileInfoActivity(getCurrentActivity());
        }
    }

    @ReactMethod
    public void trackEvent(int eventId) {

        Timber.d("trackEvent eventId %s", eventId);

        ZPAnalytics.trackEvent(eventId);
    }

    @ReactMethod
    public void showDetail(int appid, String transid) {
        Timber.d("show Detail appid %s transid %s", appid, transid);
        Map<String, String> options = new HashMap<>();
        options.put("view", "history");
        options.put("transid", transid);

        Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }

        Intent intent = navigator.intentPaymentApp(activity, new AppResource(appid), options);
        if (intent != null) {
            activity.startActivity(intent);
        }
    }

    @Nullable
    @Override
    public Map<String, Object> getConstants() {
        Map<String, Object> map = new HashMap<>();
        map.put("termsOfUseUrl", BuildConfig.DEBUG ? "https://sandbox.zalopay.com.vn/terms" : "https://zalopay.com.vn/terms");
        map.put("faqUrl", BuildConfig.DEBUG ? "https://sandbox.zalopay.com.vn/faq" : "https://zalopay.com.vn/faq");
        map.put("storeUrl", AndroidUtils.getPlayStoreUrl("React Native", "Internal"));
        return map;
    }

    @ReactMethod
    public void showDialogWithMessage(String message, String lblCancel, String lblConfirm, final Promise promise) {
        Timber.d("showDialogWithMessage %s", message);
        DialogManager.showSweetDialogConfirm(getCurrentActivity(), message, lblConfirm, lblCancel, new ZPWOnEventConfirmDialogListener() {
            @Override
            public void onCancelEvent() {
                if (promise != null) {
                    promise.resolve(0);
                }
            }

            @Override
            public void onOKevent() {
                if (promise != null) {
                    promise.resolve(1);
                }
            }
        });
    }

    @ReactMethod
    public void showDialogErrorWithMessage(String message, String lblCancel, final Promise promise) {
        Timber.d("showDialogErrorWithMessage %s", message);
        DialogManager.showSweetDialogCustom(getCurrentActivity(), message, lblCancel, SweetAlertDialog.ERROR_TYPE, new ZPWOnEventConfirmDialogListener() {
            @Override
            public void onCancelEvent() {
            }

            @Override
            public void onOKevent() {
                if (promise != null) {
                    promise.resolve(0);
                }
            }
        });
    }
}

