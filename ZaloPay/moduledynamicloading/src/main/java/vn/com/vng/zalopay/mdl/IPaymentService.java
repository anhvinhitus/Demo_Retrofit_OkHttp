package vn.com.vng.zalopay.mdl;

import android.app.Activity;

import com.facebook.react.bridge.Promise;

/**
 * Created by longlv on 02/06/2016.
 */
public interface IPaymentService {

    void getUserInfo(Promise promise, long appId);
    void verifyAccessToken(String mUid, String mAccessToken, Promise promise);
    void pay(Activity activity, Promise promise, long appID, String appTransID, String appUser, long appTime, long amount, String itemName, String description, String embedData, String mac);
    void destroyVariable();
}
