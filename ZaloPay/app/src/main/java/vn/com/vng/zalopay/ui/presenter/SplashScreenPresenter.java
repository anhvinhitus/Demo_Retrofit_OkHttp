package vn.com.vng.zalopay.ui.presenter;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;
import vn.com.vng.zalopay.app.ApplicationState;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.ui.view.ISplashScreenView;
import vn.com.vng.zalopay.utils.IntroAppUtils;
import vn.com.vng.zalopay.utils.ZaloHelper;

/**
 * Created by AnhHieu on 5/13/16.
 * Splash startup
 */
@Singleton
public class SplashScreenPresenter extends AbstractPresenter<ISplashScreenView> {

    private final UserConfig mUserConfig;
    private final ApplicationState mApplicationState;


    @Inject
    SplashScreenPresenter(UserConfig userConfig, ApplicationState applicationState) {
        mUserConfig = userConfig;
        mApplicationState = applicationState;
    }

    public void verifyUser() {
        Timber.d("ApplicationState object [%s]", mApplicationState);
        if (mUserConfig.hasCurrentUser()) {
            Timber.i("go to Home Screen");
            ZaloHelper.getZaloProfileInfo(mView.getContext(), mUserConfig);
            mView.gotoHomeScreen();
        } else if (IntroAppUtils.isShowedIntro()) {
            Timber.d("gotoLoginScreen");
            mView.gotoLoginScreen();
        } else {
            Timber.d("gotoOnBoardingScreen");
            mView.gotoOnBoardingScreen();
        }
    }
}
