package vn.com.vng.zalopay.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.facebook.react.bridge.Promise;

import java.util.Map;

import vn.com.vng.zalopay.domain.model.AppResource;

/**
 * Created by AnhHieu on 6/21/16.
 * *
 */
public interface INavigator {

    void startUpdateProfile2ForResult(Activity activity, String walletTransID);

    void startUpdateProfile2ForResult(Fragment fragment, String walletTransID);

    void startLinkAccountActivityForResult(Activity activity);

    void startLinkAccountActivityForResult(Fragment fragment);

    void startUpdateProfileLevelBeforeLinkAcc(Activity activity);

    void startUpdateProfileLevelBeforeLinkAcc(Fragment fragment);

    void startProfileInfoActivity(Context context);

    void startLinkCardActivity(Context context);

    Intent intentProfile(Context context);

    Intent intentLinkCard(Context context);

    Intent intentPaymentApp(Context context, AppResource appResource, Map<String, String> launchOptions);

    boolean promptPIN(Context context, int channel, Promise promise);

    void startWebAppActivity(Context context, String url);
}
