package vn.com.vng.zalopay.ui.view;

import vn.com.vng.zalopay.domain.model.Person;

/**
 * Created by AnhHieu on 9/11/16.
 * *
 */
public interface ITransferMoneyView {
    void showLoading();

    void hideLoading();

    void showError(String message);

    void onGetProfileSuccess(Person person, String zaloPayName);
}
