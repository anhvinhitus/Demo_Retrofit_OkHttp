package vn.com.vng.zalopay.ui.view;

import android.app.Activity;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import vn.com.vng.zalopay.domain.model.User;


/**
 * Created by longlv on 13/06/2016.
 * *
 */
public interface IBalanceManagementView extends ILoadDataView {
    Activity getActivity();
    void updateBalance(long balance);
    void updateUserInfo(User user);
    void showConfirmDialog(String message, String btnConfirm, String btnCancel, ZPWOnEventConfirmDialogListener listener);
}
