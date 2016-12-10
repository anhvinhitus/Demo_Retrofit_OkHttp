package vn.com.vng.zalopay.ui.view;

import android.app.Activity;

import vn.com.zalopay.wallet.listener.ZPWOnEventDialogListener;

/**
 * Created by AnhHieu on 3/26/16.
 * *
 */
public interface ILoginView extends ILoadDataView {

    void gotoMainActivity();

    Activity getActivity();

    void gotoInvitationCode();

    void showNetworkError();

    void showCustomDialog(String message,
                          String cancelBtnText,
                          int dialogType,
                          final ZPWOnEventDialogListener listener);
}
