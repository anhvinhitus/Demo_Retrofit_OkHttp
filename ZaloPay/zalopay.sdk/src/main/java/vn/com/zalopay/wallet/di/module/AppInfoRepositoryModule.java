package vn.com.zalopay.wallet.di.module;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.repository.appinfo.AppInfoLocalStorage;
import vn.com.zalopay.wallet.repository.appinfo.AppInfoRepository;
import vn.com.zalopay.wallet.repository.appinfo.AppInfoStore;

/**
 * Created by chucvv on 6/7/17.
 */
@Module
public class AppInfoRepositoryModule {
    @Provides
    @Singleton
    public AppInfoStore.AppInfoService provideAppInfoService(Retrofit retrofit) {
        return retrofit.create(AppInfoStore.AppInfoService.class);
    }

    @Provides
    @Singleton
    public AppInfoStore.LocalStorage provideAppInfoLocalStorage(SharedPreferencesManager sharedPreferencesManager) {
        return new AppInfoLocalStorage(sharedPreferencesManager);
    }

    @Provides
    @Singleton
    public AppInfoStore.Repository provideAppInfoRepository(AppInfoStore.AppInfoService appInfoService, AppInfoStore.LocalStorage localStorage) {
        return new AppInfoRepository(appInfoService, localStorage);
    }
}
