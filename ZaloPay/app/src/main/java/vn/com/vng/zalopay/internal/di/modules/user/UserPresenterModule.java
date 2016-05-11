package vn.com.vng.zalopay.internal.di.modules.user;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.internal.di.scope.UserScope;
import vn.com.vng.zalopay.ui.presenter.BalanceTopupPresenter;
import vn.com.vng.zalopay.ui.presenter.HomePresenter;
import vn.com.vng.zalopay.ui.presenter.LeftMenuPresenter;
import vn.com.vng.zalopay.ui.presenter.ProductPresenter;

@Module
public class UserPresenterModule {

    @UserScope
    @Provides
    HomePresenter provideHomePresenter() {
        return new HomePresenter();
    }

    @UserScope
    @Provides
    ProductPresenter provideProductPresenter(User user) {
        return new ProductPresenter(user);
    }

    @UserScope
    @Provides
    LeftMenuPresenter provideLeftMenuPresenter() {
        return new LeftMenuPresenter();
    }

    @UserScope
    @Provides
    BalanceTopupPresenter provideBalanceTopupPresenterr(User user) {
        return new BalanceTopupPresenter(user);

    }
}
