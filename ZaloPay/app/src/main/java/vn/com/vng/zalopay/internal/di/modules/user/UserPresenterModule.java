package vn.com.vng.zalopay.internal.di.modules.user;

import org.greenrobot.eventbus.EventBus;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.data.cache.SqlZaloPayScope;
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
    LeftMenuPresenter provideLeftMenuPresenter(EventBus eventBus, User user, SqlZaloPayScope sqlZaloPayScope) {
        return new LeftMenuPresenter(user, sqlZaloPayScope);
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
}
