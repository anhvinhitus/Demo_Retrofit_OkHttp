package vn.com.vng.zalopay.ui.view;

import android.app.Activity;
import android.support.v4.app.Fragment;

import vn.com.zalopay.wallet.listener.ZPWOnEventDialogListener;

/**
 * Created by longlv on 09/05/2016.
 * *
 */
public interface IQRScanView extends ILoadDataView {
    Activity getActivity();
    Fragment getFragment();

    void resumeScanner();
    void showWarningDialog(String message, ZPWOnEventDialogListener cancelListener);
}
