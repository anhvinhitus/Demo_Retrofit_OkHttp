package vn.com.vng.zalopay.ui.view;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import vn.com.vng.zalopay.domain.model.User;


/**
 * Created by longlv on 13/06/2016.
 * *
 */
public interface IBalanceManagementView extends ILoadDataView {

    void setBalance(long balance);

    void setUser(User user);

    void showConfirmDialog(String message, String btnConfirm, String btnCancel, ZPWOnEventConfirmDialogListener listener);

    void showTopup(boolean isShow);
}
