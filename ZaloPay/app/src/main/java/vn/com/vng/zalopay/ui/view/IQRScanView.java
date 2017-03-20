package vn.com.vng.zalopay.ui.view;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventDialogListener;
import android.support.annotation.StringRes;

import vn.com.vng.zalopay.ui.presenter.IPaymentDataView;


/**
 * Created by longlv on 09/05/2016.
 * *
 */
public interface IQRScanView extends IPaymentDataView {

    void resumeScanner();

    void showWarningDialogAndResumeScan(@StringRes int strResource);

}
