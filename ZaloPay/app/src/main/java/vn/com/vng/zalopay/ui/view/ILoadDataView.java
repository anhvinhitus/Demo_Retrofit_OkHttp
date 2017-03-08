
package vn.com.vng.zalopay.ui.view;

import android.content.Context;

import com.zalopay.ui.widget.dialog.listener.ZPWOnSweetDialogListener;


public interface ILoadDataView {

    void showLoading();

    void hideLoading();

    void showError(String message);

    void showNetworkErrorDialog();

    void showNetworkErrorDialog(ZPWOnSweetDialogListener listener);

    Context getContext();

}
