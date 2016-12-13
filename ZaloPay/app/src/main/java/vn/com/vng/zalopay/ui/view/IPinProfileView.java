package vn.com.vng.zalopay.ui.view;

/**
 * Created by AnhHieu on 9/10/16.
 * *
 */
public interface IPinProfileView {

    void setError(String message);

    void clearPin();

    void onPinSuccess();

    void showLoading();

    void hideLoading();

    void showKeyboard();
}
