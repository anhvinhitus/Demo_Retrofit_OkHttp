/*
package vn.com.vng.zalopay.ui.module;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.internal.di.scope.ActivityScope;
import vn.com.vng.zalopay.ui.presenter.LoginPresenter;
import vn.com.vng.zalopay.ui.view.ILoginView;

*/
/**
 * Created by AnhHieu on 4/26/16.
 *//*

@Module
public class LoginActivityModule {

    private ILoginView loginView;

    public LoginActivityModule(ILoginView loginView) {
        this.loginView = loginView;
    }

    @Provides
    @ActivityScope
    ILoginView provideSplashActivity() {
        return loginView;
    }

    @Provides
    @ActivityScope
    LoginPresenter provideSplashActivityPresenter(ILoginView loginView) {
        return new LoginPresenter(loginView);
    }
}
*/
