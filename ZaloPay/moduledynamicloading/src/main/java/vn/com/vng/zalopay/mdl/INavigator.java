package vn.com.vng.zalopay.mdl;

import android.content.Context;
import android.content.Intent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by AnhHieu on 6/21/16.
 */
public interface INavigator {

    Intent intentProfile(Context context);

    Intent intentLinkCard(Context context);

    Intent intentPaymentApp(Context context, int appId, Map<String, String> launchOptions);
}
