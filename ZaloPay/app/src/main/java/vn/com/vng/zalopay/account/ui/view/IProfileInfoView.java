package vn.com.vng.zalopay.account.ui.view;

import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by longlv on 19/05/2016.
 */
public interface IProfileInfoView {
    void updateUserInfo(User user);

    void setZaloPayName(String zaloPayName);

    void showError(String message);
}

