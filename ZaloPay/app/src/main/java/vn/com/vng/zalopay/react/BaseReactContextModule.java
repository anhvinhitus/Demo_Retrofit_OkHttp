package vn.com.vng.zalopay.react;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;

import vn.com.vng.zalopay.react.listener.OnEventClickListener;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.view.dialog.DialogManager;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * Created by longlv on 16/09/2016.
 * Contain base methods that react native module usually use
 */
public abstract class BaseReactContextModule extends ReactContextBaseJavaModule {

    public BaseReactContextModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    /// The purpose of this method is to return the string name of the NativeModule
    /// which represents this class in JavaScript. So here we will call this ZaloPayInternal
    /// so that we can access it through React.NativeModules.ZaloPayInternal in JavaScript.
    public abstract String getReactNativeName();

    @Override
    public String getName() {
        return getReactNativeName();
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
        if (btnNames == null || btnNames.size() <= 0) {
            return;
        }
        switch (dialogType) {
            case SweetAlertDialog.NORMAL_TYPE:
                if (btnNames.size() > 1) {
                    DialogManager.showSweetDialogConfirm(getCurrentActivity(),
                            message,
                            btnNames.getString(0),
                            btnNames.getString(1),
                            new ZPWOnEventConfirmDialogListener() {
                                @Override
                                public void onCancelEvent() {
                                    Helpers.promiseResolve(promise, 1);
                                }

                                @Override
                                public void onOKevent() {
                                    Helpers.promiseResolve(promise, 0);
                                }
                            }
                    );
                } else {
                    DialogManager.showSweetDialogCustom(getCurrentActivity(),
                            message,
                            btnNames.getString(0),
                            SweetAlertDialog.NORMAL_TYPE,
                            new OnEventClickListener(promise, 1));
                }
                break;

            case SweetAlertDialog.ERROR_TYPE:
                DialogManager.showSweetDialogCustom(getCurrentActivity(),
                        message,
                        btnNames.getString(0),
                        SweetAlertDialog.ERROR_TYPE,
                        new OnEventClickListener(promise, 1));
                break;

            case SweetAlertDialog.SUCCESS_TYPE:
                DialogManager.showSweetDialogCustom(getCurrentActivity(),
                        message,
                        btnNames.getString(0),
                        SweetAlertDialog.SUCCESS_TYPE,
                        new OnEventClickListener(promise, 1));
                break;

            case SweetAlertDialog.WARNING_TYPE:
                DialogManager.showSweetDialogCustom(getCurrentActivity(),
                        message,
                        btnNames.getString(0),
                        SweetAlertDialog.WARNING_TYPE,
                        new OnEventClickListener(promise, 1));
                break;

            default:
                DialogManager.showSweetDialogCustom(getCurrentActivity(),
                        message,
                        btnNames.getString(0),
                        SweetAlertDialog.NORMAL_TYPE,
                        new OnEventClickListener(promise, 1));
                break;
        }
    }
}
