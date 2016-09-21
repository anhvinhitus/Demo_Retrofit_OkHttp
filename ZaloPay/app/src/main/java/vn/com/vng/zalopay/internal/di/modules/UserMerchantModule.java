package vn.com.vng.zalopay.internal.di.modules;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.merchant.MerchantLocalStorage;
import vn.com.vng.zalopay.data.merchant.MerchantRepository;
import vn.com.vng.zalopay.data.merchant.MerchantStore;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.internal.di.scope.UserScope;

/**
 * Created by AnhHieu on 9/21/16.
 * *
 */
@Module
public class UserMerchantModule {

    @UserScope
    @Provides
    MerchantStore.Repository providesRepository(
            MerchantStore.LocalStorage localStorage,
            MerchantStore.RequestService requestService,
            User user) {
        return new MerchantRepository(localStorage, requestService, user);
    }

    @UserScope
    @Provides
    MerchantStore.LocalStorage providesLocalStorage(@Named("daosession") DaoSession session) {
        return new MerchantLocalStorage(session);
    }

    @Provides
    @UserScope
    MerchantStore.RequestService providesRequestService(@Named("retrofitApi") Retrofit retrofit) {
        return retrofit.create(MerchantStore.RequestService.class);
    }
}
