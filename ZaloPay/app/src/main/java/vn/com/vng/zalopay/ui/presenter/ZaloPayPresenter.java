package vn.com.vng.zalopay.ui.presenter;

/**
 * Created by AnhHieu on 5/9/16.
 */
public interface ZaloPayPresenter<IZaloPayView> extends IPresenter<IZaloPayView> {
    void initialize();

    void listAppResource();

    void getBalance();

    void getBanners();
}
