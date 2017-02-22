package vn.com.vng.zalopay.account.ui.view;

/**
 * Created by AnhHieu on 7/1/16.
 */
public interface IUpdateProfile3View {
    void showLoading();

    void hideLoading();

    void updateSuccess();

    void finish();

    void showError(String message);

    void setProfileInfo(String email, String identity, String foregroundImg, String backgroundImg, String avatarImg);
}
