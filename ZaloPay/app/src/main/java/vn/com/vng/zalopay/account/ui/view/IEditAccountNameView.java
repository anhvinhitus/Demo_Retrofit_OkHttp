package vn.com.vng.zalopay.account.ui.view;

/**
 * Created by AnhHieu on 8/12/16.
 */
public interface IEditAccountNameView {
    void showError(String msg);

    void accountNameValid(boolean exist);

    void editAccountNameSuccess();

    void showLoading();

    void hideLoading();
}
