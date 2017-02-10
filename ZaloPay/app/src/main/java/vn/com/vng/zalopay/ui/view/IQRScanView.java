package vn.com.vng.zalopay.ui.view;

import vn.com.vng.zalopay.ui.presenter.IPaymentDataView;
import vn.com.zalopay.wallet.listener.ZPWOnEventDialogListener;

/**
 * Created by longlv on 09/05/2016.
 * *
 */
public interface IQRScanView extends IPaymentDataView {

    void resumeScanner();

    void showWarningDialog(String message, ZPWOnEventDialogListener cancelListener);
}
