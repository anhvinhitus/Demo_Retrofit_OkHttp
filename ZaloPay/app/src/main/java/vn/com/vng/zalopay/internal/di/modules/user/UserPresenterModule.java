package vn.com.vng.zalopay.internal.di.modules.user;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.account.ui.presenter.PreProfilePresenter;
import vn.com.vng.zalopay.account.ui.presenter.ProfileInfoPresenter;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.internal.di.scope.UserScope;
import vn.com.vng.zalopay.ui.presenter.BalanceTopupPresenter;
import vn.com.vng.zalopay.ui.presenter.HomePresenter;
import vn.com.vng.zalopay.ui.presenter.LeftMenuPresenter;
import vn.com.vng.zalopay.ui.presenter.LinkCardPresenter;
import vn.com.vng.zalopay.ui.presenter.LinkCardProdurePresenter;
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
    LeftMenuPresenter provideLeftMenuPresenter(User user) {
        return new LeftMenuPresenter(user);
    }

    @UserScope
    @Provides
    BalanceTopupPresenter provideBalanceTopupPresenter(User user) {
        return new BalanceTopupPresenter(user);
    }

    @UserScope
    @Provides
    LinkCardPresenter provideLinkCardPresenter(User user) {
        return new LinkCardPresenter(user);
    }

    @UserScope
    @Provides
    LinkCardProdurePresenter provideLinkCardProcedurePresenter(User user) {
        return new LinkCardProdurePresenter(user);
    }
    @UserScope
    @Provides
    PreProfilePresenter providePreProfilePresenter(UserConfig userConfig) {
        return new PreProfilePresenter(userConfig);
    }

    @UserScope
    @Provides
    ProfileInfoPresenter provideProfileInfoPresenter(UserConfig userConfig) {
        return new ProfileInfoPresenter(userConfig);
    }
}
