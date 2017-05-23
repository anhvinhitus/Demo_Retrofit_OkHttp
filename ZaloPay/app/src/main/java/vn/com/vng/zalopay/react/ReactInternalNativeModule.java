package vn.com.vng.zalopay.react;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
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
import com.facebook.react.views.text.ReactFontManager;
import com.google.gson.JsonObject;
import com.zalopay.apploader.network.NetworkService;
import com.zalopay.ui.widget.dialog.SweetAlertDialog;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import rx.Subscription;
import rx.functions.Action0;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.data.appresources.AppResourceStore;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.exception.PaymentWrapperException;
import vn.com.vng.zalopay.navigation.INavigator;
import vn.com.vng.zalopay.paymentapps.PaymentAppConfig;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.react.iap.RequestSubscriber;
import vn.com.vng.zalopay.pw.AbsPWResponseListener;
import vn.com.vng.zalopay.pw.DefaultPaymentRedirectListener;
import vn.com.vng.zalopay.pw.PaymentWrapper;
import vn.com.vng.zalopay.pw.PaymentWrapperBuilder;
import vn.com.vng.zalopay.ui.view.ILoadDataView;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.vng.zalopay.utils.FileDownloader;
import vn.com.zalopay.analytics.ZPPaymentSteps;
import vn.com.zalopay.analytics.ZPAnalytics;

/**
 * Created by huuhoa on 4/25/16.
 * Internal API
 */
final class ReactInternalNativeModule extends ReactContextBaseJavaModule {

    private INavigator navigator;
    private AppResourceStore.Repository mResourceRepository;

    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();
    private NotificationStore.Repository mNotificationRepository;

    private final NetworkService mNetworkServiceWithRetry;
    private final User mUser;

    ReactInternalNativeModule(ReactApplicationContext reactContext, User user,
                              INavigator navigator, AppResourceStore.Repository resourceRepository,
                              NotificationStore.Repository mNotificationRepository,
                              NetworkService networkServiceWithRetry
    ) {
        super(reactContext);
        this.navigator = navigator;
        this.mResourceRepository = resourceRepository;
        this.mNotificationRepository = mNotificationRepository;

        this.mNetworkServiceWithRetry = networkServiceWithRetry;
        this.mUser = user;
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

    @ReactMethod
    public void closeModule(String moduleId) {
        Timber.d("close Module");
        if (getCurrentActivity() != null) {
            getCurrentActivity().finish();
        }
    }

    @ReactMethod
    public void navigateProfile() {
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
    public void showDetail(final int appid, final String transid) {
        Timber.d("Show detail : appid [%s] transid [%s]", appid, transid);
        Subscription subscription = mResourceRepository.existResource(appid)
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
        map.put("supportCenterUrl", BuildConfig.SUPPORT_CENTER_HOST);
        map.put("storeUrl", AndroidUtils.getUrlPlayStore("React Native", "Internal"));
        return map;
    }

    @ReactMethod
    public void promptPIN(final int channel, final Promise promise) {
        Timber.d("promptPIN : channel %s", channel);
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
        Timber.d("showDialogWithMessage : [%s]", message);
        WritableNativeArray btnArray = new WritableNativeArray();
        btnArray.pushString(lblCancel);
        btnArray.pushString(lblConfirm);
        showDialog(SweetAlertDialog.NORMAL_TYPE, null, message, btnArray, promise);
    }

    @ReactMethod
    public void showDialogErrorWithMessage(String message, String lblCancel, final Promise promise) {
        Timber.d("showDialogErrorWithMessage : [%s]", message);
        WritableNativeArray btnArray = new WritableNativeArray();
        btnArray.pushString(lblCancel);
        showDialog(SweetAlertDialog.ERROR_TYPE, null, message, btnArray, promise);
    }

    @ReactMethod
    public void payOrder(String notificationId, Promise promise) {
        Timber.d("pay order notificationId [%s]", notificationId);
        final long notifyId;
        try {
            notifyId = Long.valueOf(notificationId);
        } catch (NumberFormatException e) {
            Helpers.promiseResolveError(promise, -1, "Arguments invalid");
            return;
        }

        Subscription subscription = mNotificationRepository.getNotify(notifyId)
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        removeNotify(notifyId);
                    }
                })
                .subscribe(new PayOrderSubscriber(promise));
        mCompositeSubscription.add(subscription);
    }

    @ReactMethod
    public void loadFontAsync(final String fontFamilyName, final String url, final Promise promise) {
        if (fontFamilyName.equalsIgnoreCase("") || url.equalsIgnoreCase("")) {
            promise.reject(new Exception("Do not leave param null"));
            return;
        }

        if (!BuildConfig.DEBUG && PaymentAppConfig.EXCEPT_LOAD_FONTS.contains(fontFamilyName.toLowerCase())) {
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
//                        ReactFontManager.getInstance().setTypeface("MerchantFont-" + fontFamilyName, Typeface.NORMAL, typeface);
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
    public void navigateBankAccount() {
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            return;
        }

        navigator.startLinkAccountActivity(currentActivity);
    }

    @ReactMethod
    public void navigateCardList() {
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            return;
        }

        navigator.startLinkCardActivity(currentActivity);
    }

    private void removeNotify(long notifyId) {
        Timber.d("Remove notify : notifyId [%s]", notifyId);
        Subscription subscription = mNotificationRepository.removeNotify(notifyId)
                .subscribe(new DefaultSubscriber<Boolean>());
        mCompositeSubscription.add(subscription);
    }

    private void payOrder(NotificationData notify) {
        JsonObject embeddata = notify.getEmbeddata();
        Timber.d("Pay order : notificationId [%s] embeddata [%s]", notify.notificationId, embeddata);
        if (embeddata == null) {
            return;
        }

        if (embeddata.has("zptranstoken") && embeddata.has("appid")) {

            String zptranstoken = embeddata.get("zptranstoken").getAsString();
            long appId = embeddata.get("appid").getAsLong();
            pay(appId, zptranstoken);
        }
    }


    private PaymentWrapper paymentWrapper;

    private void pay(final long appId, final String zptranstoken) {
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            return;
        }

        if (paymentWrapper == null) {
            paymentWrapper = getPaymentWrapper();
        }

        showLoading();
        Timber.d("Pay with token call");
        paymentWrapper.payWithToken(currentActivity, appId, zptranstoken, ZPPaymentSteps.OrderSource_MerchantApp);
    }

    private void showErrorDialog(final String message) {
        AndroidUtils.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (getCurrentActivity() != null) {
                    DialogHelper.showNotificationDialog(getCurrentActivity(), message);
                }
            }
        });
    }

    private void showNetworkErrorDialog() {
        AndroidUtils.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (getCurrentActivity() != null) {
                    DialogHelper.showNetworkErrorDialog(getCurrentActivity(), null);
                }
            }
        });
    }

    private PaymentWrapper getPaymentWrapper() {
        PaymentWrapper wrapper = new PaymentWrapperBuilder()
                .setRedirectListener(new DefaultPaymentRedirectListener(navigator) {
                    @Override
                    public Object getContext() {
                        return getCurrentActivity();
                    }
                })
                .setResponseListener(new AbsPWResponseListener(getCurrentActivity()) {
                    @Override
                    protected ILoadDataView getView() {
                        return null;
                    }

                    @Override
                    public void onError(PaymentWrapperException exception) {
                        hideLoading();
                        if (exception != null) {
                            if (exception.getErrorCode() == PaymentError.ERR_CODE_INTERNET.value()) {
                                showNetworkErrorDialog();
                            } else {
                                showErrorDialog(exception.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onCompleted() {
                        hideLoading();
                    }
                }).build();
        wrapper.initializeComponents();
        return wrapper;
    }


    private class PayOrderSubscriber extends DefaultSubscriber<NotificationData> {
        private WeakReference<Promise> mPromise;

        PayOrderSubscriber(Promise promise) {
            mPromise = new WeakReference<>(promise);
        }

        @Override
        public void onError(Throwable e) {
            Timber.d(e, "Error pay order");
            if (mPromise.get() != null) {
                Helpers.promiseResolveError(mPromise.get(), PaymentError.ERR_CODE_UNKNOWN.value(), e.getMessage());
            }
        }

        @Override
        public void onNext(NotificationData notify) {
            payOrder(notify);
            if (mPromise.get() != null) {
                Helpers.promiseResolveSuccess(mPromise.get(), null);
            }
        }
    }

    @ReactMethod
    public void request(String baseUrl, ReadableMap content, Promise promise) {
        Timber.d("requestWithoutRetry: baseUrl [%s] String content [%s]", baseUrl, content);
        Subscription subscription = mNetworkServiceWithRetry.requestWithoutRetry(baseUrl, addQueriesUser(content))
                .subscribeOn(Schedulers.io())
                .subscribe(new RequestSubscriber(promise));
        mCompositeSubscription.add(subscription);
    }

    @ReactMethod
    public void requestWithRetry(String baseUrl, ReadableMap content, Promise promise) {
        Timber.d("requestWithRetry: baseUrl [%s] String content [%s]", baseUrl, content);
        Subscription subscription = mNetworkServiceWithRetry.request(baseUrl, addQueriesUser(content))
                .subscribeOn(Schedulers.io())
                .subscribe(new RequestSubscriber(promise));
        mCompositeSubscription.add(subscription);
    }

    private ReadableMap addQueriesUser(ReadableMap content) {
        WritableMap writableMap = Arguments.createMap();
        writableMap.merge(content);

        WritableMap queries = Arguments.createMap();
        if (content.hasKey("query")) {
            ReadableMap map = content.getMap("query");
            queries.merge(map);
        }

        queries.putString("accesstoken", mUser.accesstoken);
        queries.putString("zalopayid", mUser.zaloPayId);

        writableMap.putMap("query", queries);

        return writableMap;
    }


    @ReactMethod
    public void navigateIntro(String title) {
        Timber.d("navigateIntro : %s", title);

        if (getCurrentActivity() == null) {
            return;
        }

        navigator.startIntroAppActivity(getCurrentActivity(), false, title);
    }

}
