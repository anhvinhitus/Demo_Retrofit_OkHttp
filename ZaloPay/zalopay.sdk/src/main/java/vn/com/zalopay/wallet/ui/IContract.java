package vn.com.zalopay.wallet.ui;

/**
 * Created by chucvv on 6/12/17.
 */

public interface IContract {
    void showLoading(String pTitle);

    void hideLoading();

    void showError(String pMessage);

    void showInfoDialog(String pMessage);

    void terminate();

    void callbackThenterminate();
}
