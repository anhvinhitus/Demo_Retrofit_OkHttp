package vn.com.vng.zalopay.react;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;

import java.util.HashMap;
import java.util.Map;

import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.data.appresources.AppResourceStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.navigation.INavigator;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * Created by huuhoa on 4/25/16.
 * Internal API
 */
public class ReactInternalNativeModule extends ReactContextBaseJavaModule {

    private INavigator navigator;
    private AppResourceStore.Repository mResourceRepository;
    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    public ReactInternalNativeModule(ReactApplicationContext reactContext,
                                     INavigator navigator, AppResourceStore.Repository resourceRepository) {
        super(reactContext);
        this.navigator = navigator;
        this.mResourceRepository = resourceRepository;
    }

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
        AndroidUtils.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (getCurrentActivity() != null) {
                    navigator.startLinkCardActivity(getCurrentActivity());
                }
            }
        });
    }

    @ReactMethod
    public void navigateProfile() {
        Timber.d("navigateProfile");
        AndroidUtils.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (getCurrentActivity() != null) {
                    navigator.startProfileInfoActivity(getCurrentActivity());
                }
            }
        });
    }

    @ReactMethod
    public void trackEvent(int eventId) {

        Timber.d("trackEvent eventId %s", eventId);

        ZPAnalytics.trackEvent(eventId);
    }

    @ReactMethod
    public void showDetail(final int appid, final String transid) {
        Timber.d("show Detail appid %s transid %s", appid, transid);
        Subscription subscription = mResourceRepository.existResource(appid)
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                            startPaymentApp(appid, transid);
                        }
                    }
                });
        mCompositeSubscription.add(subscription);
    }

    private void startPaymentApp(int appid, String transid) {
        Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }

        Map<String, String> options = new HashMap<>();
        options.put("view", "history");
        options.put("transid", transid);

        Intent intent = navigator.intentPaymentApp(activity, new AppResource(appid), options);
        if (intent != null) {
            activity.startActivity(intent);
        }
    }

    @Override
    public Map<String, Object> getConstants() {
        Map<String, Object> map = new HashMap<>();
        map.put("termsOfUseUrl", BuildConfig.TERMS_HOST);
        map.put("faqUrl", BuildConfig.FAQ_HOST);
        map.put("storeUrl", AndroidUtils.getPlayStoreUrl("React Native", "Internal"));
        return map;
    }

    @ReactMethod
    public void promptPIN(final int channel, final Promise promise) {
        Timber.d("promptPIN: channel %s", channel);
        AndroidUtils.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (getCurrentActivity() == null) {
                    return;
                }
                boolean pinSuccess = navigator.promptPIN(getCurrentActivity(), channel, promise);
                Timber.d("pinSuccess %s", pinSuccess);
            }
        });
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
    public void showDialogWithMessage(String message, String lblCancel, String lblConfirm, final Promise promise) {
        Timber.d("showDialogWithMessage %s", message);
        WritableNativeArray btnArray = new WritableNativeArray();
        btnArray.pushString(lblCancel);
        btnArray.pushString(lblConfirm);
        showDialog(SweetAlertDialog.NORMAL_TYPE, null, message, btnArray, promise);
    }

    @ReactMethod
    public void showDialogErrorWithMessage(String message, String lblCancel, final Promise promise) {
        Timber.d("showDialogErrorWithMessage %s", message);
        WritableNativeArray btnArray = new WritableNativeArray();
        btnArray.pushString(lblCancel);
        showDialog(SweetAlertDialog.ERROR_TYPE, null, message, btnArray, promise);
    }
}
