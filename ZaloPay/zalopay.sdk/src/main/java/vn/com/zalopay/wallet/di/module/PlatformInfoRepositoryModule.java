package vn.com.zalopay.wallet.di.module;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.di.qualifier.Api;
import vn.com.zalopay.wallet.repository.platforminfo.PlatformInfoRepository;
import vn.com.zalopay.wallet.repository.platforminfo.PlatformInfoStorage;
import vn.com.zalopay.wallet.repository.platforminfo.PlatformInfoStore;

/**
 * Created by chucvv on 6/7/17.
 */
@Module
public class PlatformInfoRepositoryModule {
    @Provides
    @Singleton
    public PlatformInfoStore.PlatformInfoService providePlatformInfoService(@Api Retrofit retrofit) {
        return retrofit.create(PlatformInfoStore.PlatformInfoService.class);
    }

    @Provides
    @Singleton
    public PlatformInfoStore.LocalStorage providePlatformInfoLocalStorage(SharedPreferencesManager sharedPreferencesManager) {
        return new PlatformInfoStorage(sharedPreferencesManager);
    }

    @Provides
    @Singleton
    public PlatformInfoStore.Repository providePlatformInfoRepository(PlatformInfoStore.PlatformInfoService platformInfoService,
                                                                      PlatformInfoStore.LocalStorage localStorage) {
        return new PlatformInfoRepository(platformInfoService, localStorage);
    }
}
