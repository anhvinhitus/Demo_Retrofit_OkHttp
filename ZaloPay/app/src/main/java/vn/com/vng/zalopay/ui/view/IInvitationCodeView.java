package vn.com.vng.zalopay.ui.view;

/**
 * Created by AnhHieu on 6/27/16.
 */
public interface IInvitationCodeView {
    void showLoading();

    void hideLoading();

    void gotoMainActivity();

    void showError(String m);
}
