package vn.com.vng.zalopay.ui.presenter;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;
import vn.com.vng.zalopay.app.ApplicationState;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.zalosdk.ZaloSdkApi;
import vn.com.vng.zalopay.ui.view.ISplashScreenView;
import vn.com.vng.zalopay.utils.IntroAppUtils;

/**
 * Created by AnhHieu on 5/13/16.
 * Splash startup
 */
@Singleton
public class SplashScreenPresenter extends AbstractPresenter<ISplashScreenView> {

    private final UserConfig mUserConfig;
    private final ApplicationState mApplicationState;

    private final ZaloSdkApi mZaloSdkApi;

    @Inject
    SplashScreenPresenter(UserConfig userConfig, ApplicationState applicationState, ZaloSdkApi zaloSdkApi) {
        this.mUserConfig = userConfig;
        this.mApplicationState = applicationState;
        this.mZaloSdkApi = zaloSdkApi;
    }

    public void verifyUser() {
        Timber.d("ApplicationState object [%s]", mApplicationState);
        if (mUserConfig.hasCurrentUser()) {
            Timber.i("go to Home Screen");
            mZaloSdkApi.getProfile();
            mView.gotoHomeScreen();
        } else  {
            Timber.d("gotoLoginScreen");
            mView.gotoLoginScreen();
        }
    }
}
