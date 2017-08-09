package vn.com.vng.zalopay.passport;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventDialogListener;

import vn.com.vng.zalopay.domain.model.zalosdk.ZaloProfile;


/**
 * Created by AnhHieu on 3/26/16.
 * *
 */
interface ILoginView extends AbstractLoginView {

    void gotoOnboarding(ZaloProfile zaloProfile, String oauthcode);

    void showCustomDialog(String message,
                          String cancelBtnText,
                          int dialogType,
                          ZPWOnEventDialogListener listener);
}
