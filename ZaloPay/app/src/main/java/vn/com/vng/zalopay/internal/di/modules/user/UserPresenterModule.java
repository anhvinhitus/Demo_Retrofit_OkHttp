package vn.com.vng.zalopay.internal.di.modules.user;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.internal.di.scope.UserScope;
import vn.com.vng.zalopay.ui.presenter.HomePresenter;

@Module
public class UserPresenterModule {


    @UserScope
    @Provides
    HomePresenter provideHomePresenter() {
        return new HomePresenter();
    }
}
