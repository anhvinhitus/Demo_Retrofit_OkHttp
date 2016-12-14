package vn.com.vng.zalopay.internal.di.modules;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.account.ui.presenter.ChangePinPresenter;
import vn.com.vng.zalopay.account.ui.presenter.IChangePinPresenter;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.internal.di.scope.UserScope;

@Module
public class UserPresenterModule {

    @UserScope
    @Provides
    IChangePinPresenter providesChangePinPresenter(Context context, AccountStore.Repository accountRepository) {
        return new ChangePinPresenter(context, accountRepository);
    }
}
