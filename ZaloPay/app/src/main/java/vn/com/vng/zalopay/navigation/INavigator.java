package vn.com.vng.zalopay.navigation;

import android.content.Context;
import android.content.Intent;

import com.facebook.react.bridge.Promise;

import java.util.Map;

import vn.com.vng.zalopay.domain.model.AppResource;

/**
 * Created by AnhHieu on 6/21/16.
 */
public interface INavigator {

    void startProfileInfoActivity(Context context);

    void startLinkCardActivity(Context context);

    Intent intentProfile(Context context);

    Intent intentLinkCard(Context context);

    Intent intentPaymentApp(Context context, AppResource appResource, Map<String, String> launchOptions);

    boolean promptPIN(Context context, int channel, Promise promise);
}
