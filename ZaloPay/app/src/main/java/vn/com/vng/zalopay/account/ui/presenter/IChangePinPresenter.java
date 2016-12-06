package vn.com.vng.zalopay.account.ui.presenter;

import vn.com.vng.zalopay.ui.presenter.IPresenter;

/**
 * Created by AnhHieu on 8/25/16.
 * *
 */
public interface IChangePinPresenter<ContainerView, ChangePassView, VerifyView> extends IPresenter<ContainerView> {

    void setChangePassView(ChangePassView view);

    void destroyChangePassView();

    void setVerifyView(VerifyView view);

    void destroyVerifyView();

    void changePin(String oldPin, String newPin);

    void verify(String otp);
}
