package vn.com.vng.zalopay.ui.presenter;

import java.util.List;

import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.domain.model.Order;
import vn.com.zalopay.game.businnesslogic.entity.pay.AppGamePayInfo;

/**
 * Created by AnhHieu on 5/9/16.
 *
 */
public interface ZaloPayPresenter<IZaloPayView> extends IPresenter<IZaloPayView> {
    void initialize();

    void listAppResource();

    void getBalance();

    void getBanners();

    void startGamePayWebActivity(int appId);

    void payOrder(Order order, AppGamePayInfo appGamePayInfo);

    List<AppResource> getListAppResourceFromDB();
}
