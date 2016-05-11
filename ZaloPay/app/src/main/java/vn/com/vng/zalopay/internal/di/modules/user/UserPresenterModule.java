package vn.com.vng.zalopay.internal.di.modules.user;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.internal.di.scope.UserScope;
import vn.com.vng.zalopay.ui.presenter.BalanceTopupPresenter;
import vn.com.vng.zalopay.ui.presenter.HomePresenter;
import vn.com.vng.zalopay.ui.presenter.QRCodePresenter;

@Module
public class UserPresenterModule {

    @UserScope
    @Provides
    HomePresenter provideHomePresenter() {
        return new HomePresenter();
    }

    @UserScope
    @Provides
    QRCodePresenter provideProductPresenter(User user) {
        return new QRCodePresenter(user);
    }

    @UserScope
    @Provides
    BalanceTopupPresenter provideBalanceTopupPresenterr(User user) {
        return new BalanceTopupPresenter(user);
    }
}
