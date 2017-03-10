package vn.com.vng.zalopay.account.ui.view;

import com.zalopay.ui.widget.dialog.listener.ZPWOnSweetDialogListener;
import android.app.Activity;

import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.view.ILoadDataView;


/**
 * Created by longlv on 19/05/2016.
 * *
 */
public interface IProfileView extends ILoadDataView {

    Activity getActivity();

    void updateUserInfo(User user);

    void showHideChangePinView(boolean isShow);

    void setZaloPayName(String zaloPayName);

    void showNotificationDialog(String content);

    void showUpdateProfileDialog(String message, ZPWOnSweetDialogListener listener);
}

