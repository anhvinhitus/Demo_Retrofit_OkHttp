package vn.com.vng.zalopay.ui.view;

import android.app.Activity;

import vn.com.zalopay.wallet.listener.ZPWOnEventDialogListener;

/**
 * Created by longlv on 09/05/2016.
 * *
 */
public interface IQRScanView extends ILoadDataView {
    Activity getActivity();
    void onTokenInvalid();
    void resumeScanner();
    void showWarning(String message, ZPWOnEventDialogListener cancelListener);
}
