package vn.com.zalopay.game.businnesslogic.interfaces.payment;

import android.app.Activity;
import android.os.Bundle;

import vn.com.zalopay.game.businnesslogic.entity.pay.AppGamePayInfo;

/**
 * Created by longlv on 07/09/2016.
 *
 */
public interface IPaymentService {
    void pay(final Activity activity, Bundle bundle, IPaymentCallback paymentCallback);

    void destroyVariable();
}
