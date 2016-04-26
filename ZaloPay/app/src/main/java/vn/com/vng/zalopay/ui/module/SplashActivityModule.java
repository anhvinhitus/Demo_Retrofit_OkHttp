package vn.com.vng.zalopay.ui.module;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.internal.di.scope.ActivityScope;
import vn.com.vng.zalopay.ui.activity.SplashScreenActivity;
import vn.com.vng.zalopay.ui.presenter.SplashActivityPresenter;

/**
 * Created by Miroslaw Stanek on 23.04.15.
 */
@Module
public class SplashActivityModule {
    private SplashScreenActivity splashActivity;

    public SplashActivityModule(SplashScreenActivity splashActivity) {
        this.splashActivity = splashActivity;
    }

    @Provides
    @ActivityScope
    SplashScreenActivity provideSplashActivity() {
        return splashActivity;
    }

    @Provides
    @ActivityScope
    SplashActivityPresenter provideSplashActivityPresenter() {
        return new SplashActivityPresenter(splashActivity);
    }
}