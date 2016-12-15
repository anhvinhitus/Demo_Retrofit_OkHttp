package vn.com.vng.zalopay.ui.presenter;

import vn.com.vng.zalopay.domain.model.AppResource;

/**
 * Created by AnhHieu on 5/9/16.
 * *
 */
public interface IZaloPayPresenter<IZaloPayView> extends IPresenter<IZaloPayView> {
    void initialize();

    void getBalance();

    void startServiceWebViewActivity(long appId, String webViewUrl);

    void startPaymentApp(AppResource app);

    void handleLaunchApp(AppResource app);
}
