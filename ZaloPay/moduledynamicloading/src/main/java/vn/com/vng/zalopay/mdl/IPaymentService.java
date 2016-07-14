package vn.com.vng.zalopay.mdl;

import android.app.Activity;

import com.facebook.react.bridge.Promise;

/**
 * Created by longlv on 02/06/2016.
 *
 */
public interface IPaymentService {
    class PaymentInfo {
        public long appID;
        public String appTransID;
        public String appUser;
        public long appTime;
        public long amount;
        public String itemName;
        public String description;
        public String embedData;
        public String mac;
    }

    void getUserInfo(Promise promise, long appId);
    void pay(Activity activity, Promise promise, PaymentInfo paymentInfo);
    void destroyVariable();
}
