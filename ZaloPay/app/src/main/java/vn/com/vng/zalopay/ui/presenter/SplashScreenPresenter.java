package vn.com.vng.zalopay.ui.presenter;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.event.PaymentDataEvent;
import vn.com.vng.zalopay.ui.view.ISplashScreenView;
import vn.com.vng.zalopay.utils.IntroAppUtils;

/**
 * Created by AnhHieu on 5/13/16.
 * Splash startup
 */
@Singleton
public class SplashScreenPresenter extends BaseAppPresenter implements IPresenter<ISplashScreenView> {
    private final UserConfig mUserConfig;
    private ISplashScreenView mView;
    private final EventBus mEventBus;

    @Inject
    SplashScreenPresenter(EventBus eventBus, UserConfig userConfig) {
        mEventBus = eventBus;
        mUserConfig = userConfig;
    }

    @Override
    public void setView(ISplashScreenView iSplashScreenView) {
        mView = iSplashScreenView;
    }

    @Override
    public void destroyView() {
        mView = null;
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {

    }

    public void verifyUser() {
        if (mUserConfig.hasCurrentUser()) {
            Timber.i("go to Home Screen");
            getZaloProfileInfo(mView.getContext(), mUserConfig);
            mView.gotoHomeScreen();
        } else if (IntroAppUtils.isShowedIntro()) {
            Timber.d("gotoLoginScreen");
            mView.gotoLoginScreen();
        } else {
            Timber.d("gotoOnBoardingScreen");
            mView.gotoOnBoardingScreen();
        }
    }

    public void handleDeepLinks(Intent intent) {
        // Test : adb shell 'am start -d "zalopay-1://post?appid={}&zptranstoken={}"'
        if (intent != null && intent.getData() != null) {
            Uri data = intent.getData();
            String link = String.valueOf(data);
            Timber.d("handle deep links [%s]", link);

            String scheme = data.getScheme();
            String host = data.getHost();

            if (scheme.equalsIgnoreCase("zalopay-1") && host.equalsIgnoreCase("post")) {
                pay(data, false);
            } else if (scheme.equalsIgnoreCase("zalopay") && host.equalsIgnoreCase("pay")) {
                pay(data, true);
            }
        }
    }

    private void pay(Uri data, boolean isAppToApp) {
        String appid = data.getQueryParameter(vn.com.vng.zalopay.data.Constants.APPID);
        String zptranstoken = data.getQueryParameter(vn.com.vng.zalopay.data.Constants.ZPTRANSTOKEN);

        if (TextUtils.isEmpty(appid)) {
            return;
        }

        if (!TextUtils.isDigitsOnly(appid)) {
            return;
        }

        if (TextUtils.isEmpty(zptranstoken)) {
            return;
        }

        mEventBus.postSticky(new PaymentDataEvent(Long.parseLong(appid), zptranstoken, isAppToApp));
        Timber.d("post sticky payment");
    }
}
