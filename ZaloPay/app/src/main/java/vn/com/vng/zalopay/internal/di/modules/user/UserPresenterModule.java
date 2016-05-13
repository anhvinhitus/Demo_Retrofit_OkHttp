package vn.com.vng.zalopay.internal.di.modules.user;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.internal.di.scope.UserScope;
import vn.com.vng.zalopay.ui.presenter.BalanceTopupPresenter;
import vn.com.vng.zalopay.ui.presenter.HomePresenter;
import vn.com.vng.zalopay.ui.presenter.LinkCardPresenter;
import vn.com.vng.zalopay.ui.presenter.LinkCardProdurePresenter;
import vn.com.vng.zalopay.ui.presenter.QRCodePresenter;
import vn.com.vng.zalopay.ui.presenter.LeftMenuPresenter;

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
    LeftMenuPresenter provideLeftMenuPresenter() {
        return new LeftMenuPresenter();
    }

    @UserScope
    @Provides
    BalanceTopupPresenter provideBalanceTopupPresenter(User user) {
        return new BalanceTopupPresenter(user);
    }

    @UserScope
    @Provides
    LinkCardPresenter provideLinkCardPresenter() {
        return new LinkCardPresenter();
    }

    @UserScope
    @Provides
    LinkCardProdurePresenter provideLinkCardProcedurePresenter() {
        return new LinkCardProdurePresenter();
    }
}
