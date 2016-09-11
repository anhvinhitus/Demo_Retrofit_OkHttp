package vn.com.vng.zalopay.ui.presenter;

import android.view.MotionEvent;
import android.view.View;

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

    void startGamePayWebActivity(AppResource appResource);

    List<AppResource> getListAppResourceFromDB();

    void startBannerCountDownTimer();

    void stopBannerCountDownTimer();

    void onTouchBanner(View v, MotionEvent event);
}
