package vn.com.vng.zalopay.account.ui.view;

/**
 * Created by AnhHieu on 8/25/16.
 * *
 */
public interface IChangePinView {
    void showLoading();

    void hideLoading();

    void showError(String message);

    void requestFocusOldPin();

    void onPinValid(boolean isValid);
}
