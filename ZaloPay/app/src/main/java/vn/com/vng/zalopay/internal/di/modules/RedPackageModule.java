package vn.com.vng.zalopay.internal.di.modules;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.redpacket.RedPackageLocalStorage;
import vn.com.vng.zalopay.data.redpacket.RedPackageRepositoryImpl;
import vn.com.vng.zalopay.data.redpacket.RedPackageStore;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.internal.di.scope.UserScope;

/**
 * Created by longlv on 13/07/2016.
 */
@Module
public class RedPackageModule {

    @UserScope
    @Provides
    RedPackageStore.LocalStorage provideRedPackageStorage(@Named("daosession") DaoSession session) {
        return new RedPackageLocalStorage(session);
    }

    @Provides
    @UserScope
    RedPackageStore.RequestService providesRedPackageService(@Named("retrofit") Retrofit retrofit) {
        return retrofit.create(RedPackageStore.RequestService.class);
    }

    @UserScope
    @Provides
    RedPackageStore.Repository provideRedPackageRepository(RedPackageStore.RequestService requestService, RedPackageStore.LocalStorage localStorage, UserConfig userConfig, User user) {
        return new RedPackageRepositoryImpl(requestService, localStorage, userConfig, user);
    }
}
