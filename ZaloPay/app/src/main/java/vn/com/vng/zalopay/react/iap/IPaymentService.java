package vn.com.vng.zalopay.react.iap;

import android.app.Activity;

import com.facebook.react.bridge.Promise;

import vn.com.vng.zalopay.domain.model.Order;

/**
 * Created by longlv on 02/06/2016.
 *
 */
public interface IPaymentService {
    void getUserInfo(Promise promise, long appId);
    void pay(Activity activity, Promise promise, Order order);
    void destroyVariable();
}
