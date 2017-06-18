package vn.com.vng.zalopay.passport;

import android.app.Activity;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventDialogListener;

import vn.com.vng.zalopay.domain.model.zalosdk.ZaloProfile;
import vn.com.vng.zalopay.ui.view.ILoadDataView;


/**
 * Created by AnhHieu on 3/26/16.
 * *
 */
interface ILoginView extends ILoadDataView {

    void gotoMainActivity();

    Activity getActivity();

    void gotoInvitationCode();

    void gotoOnboarding(ZaloProfile zaloProfile, String oauthcode);

    void showNetworkError();

    void showCustomDialog(String message,
                          String cancelBtnText,
                          int dialogType,
                          ZPWOnEventDialogListener listener);
}
