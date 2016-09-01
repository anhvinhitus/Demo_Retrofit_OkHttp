package vn.com.vng.zalopay.account.ui.view;

import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by AnhHieu on 7/1/16.
 */
public interface IUpdateProfile3View {
    void showLoading();

    void hideLoading();

    void updateSuccess();

    void waitingApproveProfileLevel3();

    void setProfile(User user);

    void showError(String message);
}
