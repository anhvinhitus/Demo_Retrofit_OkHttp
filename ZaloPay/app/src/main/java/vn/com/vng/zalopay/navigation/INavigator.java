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

    void startUpdateProfile3Activity(Context context, boolean focusIdentity);

    void startUpdateProfile2ForResult(Activity activity);

    void startUpdateProfile2ForResult(Fragment fragment);

    void startDepositForResultActivity(Activity activity);

    void startDepositForResultActivity(Fragment fragment);

    void startLinkCardActivityForResult(Activity activity, String bankCode);

    void startLinkCardActivityForResult(Fragment fragment, String bankCode);

    void startLinkAccountActivityForResult(Activity activity, String bankCode);

    void startLinkAccountActivityForResult(Fragment fragment, String bankCode);

    void startUpdateProfileLevelBeforeLinkAcc(Activity activity);

    void startUpdateProfileLevelBeforeLinkAcc(Fragment fragment);

    void startProfileInfoActivity(Context context);

    void startLinkCardActivity(Context context);

    void startLinkAccountActivity(Context context);

    Intent intentMiniAppActivity(Context context, String moduleName, Map<String, String> launchOptions);

    Intent intentProfile(Context context);

    Intent intentPaymentApp(Context context, AppResource appResource, Map<String, String> launchOptions);

    boolean promptPIN(Context context, int channel, Promise promise);

    void startWebAppActivity(Context context, String url);

    void startWebPromotionDetailActivity(Context context, String url);

}
