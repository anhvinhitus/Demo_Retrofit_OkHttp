package vn.com.vng.zalopay.account.ui.view;

import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.view.ILoadDataView;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;

/**
 * Created by longlv on 19/05/2016.
 * *
 */
public interface IProfileView extends ILoadDataView {

    void updateUserInfo(User user);

    void showHideChangePinView(boolean isShow);

    void setZaloPayName(String zaloPayName);


    void showDialogUpdateProfile2(String content);

    void showDialogInfo(String content);

    void showConfirmDialog(String message, ZPWOnEventConfirmDialogListener listener);
}

