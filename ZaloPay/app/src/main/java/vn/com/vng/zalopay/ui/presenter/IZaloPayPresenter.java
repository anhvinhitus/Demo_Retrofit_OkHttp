package vn.com.vng.zalopay.ui.presenter;

import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBanner;

/**
 * Created by AnhHieu on 5/9/16.
 * *
 */
public interface IZaloPayPresenter<IZaloPayView> extends IPresenter<IZaloPayView> {
    void initialize();

    void startServiceWebViewActivity(long appId, String webViewUrl);

    void launchApp(AppResource app, int position);

    void launchBanner(DBanner banner, int index);
}
