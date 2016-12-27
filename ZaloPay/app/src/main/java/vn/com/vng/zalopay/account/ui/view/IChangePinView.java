package vn.com.vng.zalopay.account.ui.view;

import vn.com.vng.zalopay.ui.view.ILoadDataView;
import vn.com.zalopay.wallet.listener.ZPWOnEventDialogListener;

/**
 * Created by AnhHieu on 8/25/16.
 * *
 */
public interface IChangePinView extends ILoadDataView{
    void requestFocusOldPin();

    void showError(String message, ZPWOnEventDialogListener listener);
}
