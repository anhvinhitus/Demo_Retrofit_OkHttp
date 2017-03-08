package vn.com.vng.zalopay.account.ui.view;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventDialogListener;

import vn.com.vng.zalopay.ui.view.ILoadDataView;


/**
 * Created by AnhHieu on 8/25/16.
 * *
 */
public interface IChangePinView extends ILoadDataView{
    void requestFocusOldPin();

    void showError(String message, ZPWOnEventDialogListener listener);
}
