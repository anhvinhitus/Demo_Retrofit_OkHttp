package vn.com.vng.zalopay.internal.di.modules;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import vn.com.vng.zalopay.data.api.entity.mapper.UserEntityDataMapper;
import vn.com.vng.zalopay.data.cache.AccountLocalStorage;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.repository.AccountRepositoryImpl;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.internal.di.scope.UserScope;

/**
 * Created by AnhHieu on 7/3/16.
 */

@Module
public class UserAccountModule {

    @Provides
    @UserScope
    AccountStore.RequestService providesAccountService(@Named("retrofitApi") Retrofit retrofit) {
        return retrofit.create(AccountStore.RequestService.class);
    }

    @Provides
    @UserScope
    AccountStore.UploadPhotoService providesAccountPhotoService(@Named("retrofitPhoto") Retrofit retrofit) {
        return retrofit.create(AccountStore.UploadPhotoService.class);
    }

    @UserScope
    @Provides
    AccountStore.Repository providesAccountRepository(AccountStore.RequestService service,
                                                      AccountStore.UploadPhotoService photoService,
                                                      UserConfig userConfig, User user,
                                                      UserEntityDataMapper mapper) {
        return new AccountRepositoryImpl(new AccountLocalStorage(), service, photoService, userConfig, user, mapper);
    }
}
