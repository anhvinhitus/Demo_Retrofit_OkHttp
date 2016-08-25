package vn.com.vng.zalopay.account.ui.view;

/**
 * Created by AnhHieu on 8/25/16.
 */
public interface IChangePinVerifyView {
    void showLoading();

    void hideLoading();

    void showError(String message);

    void checkOtpValidAndSubmit();
}
