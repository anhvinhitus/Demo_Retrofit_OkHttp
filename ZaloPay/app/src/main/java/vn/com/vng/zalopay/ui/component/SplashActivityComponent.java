package vn.com.vng.zalopay.ui.component;

import dagger.Subcomponent;
import vn.com.vng.zalopay.internal.di.scope.ActivityScope;
import vn.com.vng.zalopay.ui.activity.SplashScreenActivity;
import vn.com.vng.zalopay.ui.module.SplashActivityModule;

/**
 * Created by AnhHieu on 4/26/16.
 */
@ActivityScope
@Subcomponent(
        modules = SplashActivityModule.class
)
public interface SplashActivityComponent {

    void inject(SplashScreenActivity splashActivity);

}