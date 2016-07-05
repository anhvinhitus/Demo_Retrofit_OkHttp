package vn.com.vng.zalopay.internal.di.modules.user;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.repository.AccountRepositoryImpl;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.internal.di.scope.UserScope;

/**
 * Created by AnhHieu on 7/3/16.
 */

@Module
public class AccountModule {

    @Provides
    @UserScope
    AccountStore.RequestService provideBalanceRequestService(@Named("retrofit") Retrofit retrofit) {
        return retrofit.create(AccountStore.RequestService.class);
    }

    @UserScope
    @Provides
    AccountStore.Repository provideBalanceRepository(AccountStore.RequestService service, UserConfig userConfig, User user) {
        return new AccountRepositoryImpl(service, userConfig, user, BuildConfig.UPLOAD_PHOTO_HOST);
    }

    @UserScope
    @Provides
    AccountStore.LocalStorage provideBalanceLocalStorage(@Named("daosession") DaoSession session) {
        return null;
    }
}
