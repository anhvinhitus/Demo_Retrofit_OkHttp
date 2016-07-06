package vn.com.vng.zalopay.ui.presenter;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;
import vn.com.vng.zalopay.ui.view.ISplashScreenView;

/**
 * Created by AnhHieu on 5/13/16.
 * Splash startup
 */
@Singleton
public class SplashScreenPresenter extends BaseAppPresenter implements IPresenter<ISplashScreenView> {
    private ISplashScreenView mView;

    @Inject
    public SplashScreenPresenter() {
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
        if (userConfig.hasCurrentUser()) {
            Timber.i("go to Home Screen");
            getZaloProfileInfo();
            mView.gotoHomeScreen();
        } else {
            Timber.d("gotoLoginScreen");
            mView.gotoLoginScreen();
        }
    }
}
