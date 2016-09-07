package vn.com.zalopay.game.businnesslogic.interfaces.payment;

import vn.com.zalopay.game.businnesslogic.entity.pay.AppGamePayInfo;

/**
 * Created by longlv on 07/09/2016.
 *
 */
public interface IPaymentCallback {
    void onResponseSuccess(AppGamePayInfo appGamePayInfo);
}
