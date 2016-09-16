package vn.com.vng.zalopay.react.iap;

import android.content.Intent;
import android.text.TextUtils;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;

import java.util.Locale;

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.vng.zalopay.domain.Constants;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.vng.zalopay.domain.repository.ApplicationSession;
import vn.com.vng.zalopay.react.Helpers;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.listener.ZPWOnEventDialogListener;
import vn.com.zalopay.wallet.listener.ZPWOnEventUpdateListener;
import vn.com.zalopay.wallet.view.dialog.DialogManager;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * Created by huuhoa on 5/16/16.
 * API for PaymentApp integration
 */
public class ZaloPayNativeModule extends ReactContextBaseJavaModule
        implements ActivityEventListener, LifecycleEventListener {
    final IPaymentService mPaymentService;
    final long mAppId; // AppId này là appid js cắm vào

    public ZaloPayNativeModule(ReactApplicationContext reactContext,
                               IPaymentService paymentService,
                               long appId) {
        super(reactContext);
        this.mPaymentService = paymentService;
        this.mAppId = appId;

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
            Order order = new Order();
            order.setAppid((long) params.getDouble(Constants.APPID));
            order.setApptransid(params.getString(Constants.APPTRANSID));
            order.setAppuser(params.getString(Constants.APPUSER));
            order.setApptime((long) params.getDouble(Constants.APPTIME));
            order.setAmount((long) params.getDouble(Constants.AMOUNT));
            order.setItem(params.getString(Constants.ITEM));
            order.setDescription(params.getString(Constants.DESCRIPTION));
            order.setEmbeddata(params.getString(Constants.EMBEDDATA));
            order.setMac(params.getString(Constants.MAC));

            if (order.getAppid() < 0) {
                reportInvalidParameter(promise, Constants.APPID);
                return;
            }
            if (TextUtils.isEmpty(order.getApptransid())) {
                reportInvalidParameter(promise, Constants.APPTRANSID);
                return;
            }
            if (TextUtils.isEmpty(order.getAppuser())) {
                reportInvalidParameter(promise, Constants.APPUSER);
                return;
            }
            if (order.getApptime() <= 0) {
                reportInvalidParameter(promise, Constants.APPTIME);
                return;
            }
            if (order.getAmount() <= 0) {
                reportInvalidParameter(promise, Constants.AMOUNT);
                return;
            }
//            if (TextUtils.isEmpty(order.getItem())) {
//                reportInvalidParameter(promise, Constants.ITEM);
//                return;
//            }
            if (TextUtils.isEmpty(order.getDescription())) {
                reportInvalidParameter(promise, Constants.DESCRIPTION);
                return;
            }
            if (TextUtils.isEmpty(order.getMac())) {
                reportInvalidParameter(promise, Constants.MAC);
                return;
            }

            mPaymentService.pay(getCurrentActivity(), promise, order);
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
    public void showLoading() {
        DialogManager.showProcessDialog(getCurrentActivity(), null);
    }

    @ReactMethod
    public void hideLoading() {
        DialogManager.closeProcessDialog();
    }

    @ReactMethod
    public void showDialog(int dialogType, String title, String message, ReadableArray btnNames, final Promise promise) {
        if (dialogType == SweetAlertDialog.NORMAL_TYPE) {
            if (btnNames == null || btnNames.size() <= 1) {
                DialogManager.showSweetDialogCustom(getCurrentActivity(),
                        message,
                        getCurrentActivity().getString(R.string.txt_close),
                        SweetAlertDialog.NORMAL_TYPE,
                        new ZPWOnEventDialogListener() {
                            @Override
                            public void onOKevent() {
                                Helpers.promiseResolveDialog(promise, 1);
                            }
                        });
            } else {
                DialogManager.showSweetDialogConfirm(getCurrentActivity(),
                        message,
                        btnNames.getString(0),
                        btnNames.getString(1),
                        new ZPWOnEventConfirmDialogListener() {
                            @Override
                            public void onCancelEvent() {
                                Helpers.promiseResolveDialog(promise, 1);
                            }

                            @Override
                            public void onOKevent() {
                                Helpers.promiseResolveDialog(promise, 0);
                            }
                        }
                );
            }
        } else if (dialogType == SweetAlertDialog.ERROR_TYPE) {
            DialogManager.showSweetDialogCustom(getCurrentActivity(),
                    message,
                    getCurrentActivity().getString(R.string.txt_close),
                    SweetAlertDialog.ERROR_TYPE,
                    new ZPWOnEventDialogListener() {
                        @Override
                        public void onOKevent() {
                            Helpers.promiseResolveDialog(promise, 1);
                        }
                    });
        } else if (dialogType == SweetAlertDialog.SUCCESS_TYPE) {
            DialogManager.showSweetDialogCustom(getCurrentActivity(),
                    message,
                    getCurrentActivity().getString(R.string.txt_close),
                    SweetAlertDialog.SUCCESS_TYPE,
                    new ZPWOnEventDialogListener() {
                        @Override
                        public void onOKevent() {
                            Helpers.promiseResolveDialog(promise, 1);
                        }
                    });
        } else if (dialogType == SweetAlertDialog.WARNING_TYPE) {
            DialogManager.showSweetDialogCustom(getCurrentActivity(),
                    message,
                    getCurrentActivity().getString(R.string.txt_close),
                    SweetAlertDialog.WARNING_TYPE,
                    new ZPWOnEventDialogListener() {
                        @Override
                        public void onOKevent() {
                            Helpers.promiseResolveDialog(promise, 1);
                        }
                    });
        } else if (dialogType == SweetAlertDialog.CUSTOM_IMAGE_TYPE) {
            if (btnNames == null || btnNames.size() <= 0) {
                return;
            }
            DialogManager.showSweetDialogUpdate(getCurrentActivity(),
                    message,
                    null,
                    btnNames.getString(0),
                    new ZPWOnEventUpdateListener() {
                        @Override
                        public void onUpdateListenner() {
                            Helpers.promiseResolveDialog(promise, 1);
                        }
                    });
        }
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
    }

    private void reportInvalidParameter(Promise promise, String parameterName) {
        if (promise == null) {
            return;
        }

        String message = String.format(Locale.getDefault(), "invalid %s", parameterName);
        Timber.d("Invalid parameter [%s]", parameterName);
        Helpers.promiseResolveError(promise, PaymentError.ERR_CODE_INPUT.value(), message);
    }
}
