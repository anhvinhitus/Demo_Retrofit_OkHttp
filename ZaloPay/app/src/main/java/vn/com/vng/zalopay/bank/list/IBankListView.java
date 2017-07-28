package vn.com.vng.zalopay.bank.list;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import java.util.List;

import vn.com.vng.zalopay.ui.view.ILoadDataView;

/**
 * Created by hieuvm on 7/10/17.
 * *
 */

interface IBankListView extends ILoadDataView {
    void setData(List<BankData> val);

    List<BankData> getData();

    void remove(BankData val);

    void insert(int position, BankData val);

    void close(BankData val);

    void closeAll();

    void showNotificationDialog(String msg);

    void showConfirmDialogAfterLinkBank(String message);

    void showConfirmDialog(String msg, String btnConfirm, String btnCancel, ZPWOnEventConfirmDialogListener listener);
}
