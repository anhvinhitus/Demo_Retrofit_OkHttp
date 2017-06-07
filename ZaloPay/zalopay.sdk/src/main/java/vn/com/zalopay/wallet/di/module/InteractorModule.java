package vn.com.zalopay.wallet.di.module;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import vn.com.zalopay.wallet.interactor.PlatformInteractor;
import vn.com.zalopay.wallet.repository.appinfo.AppInfoStore;
import vn.com.zalopay.wallet.repository.banklist.BankListStore;

/**
 * Created by chucvv on 6/7/17.
 */
@Module
public class InteractorModule {
    @Provides
    @Singleton
    PlatformInteractor providePlatformIneractor(AppInfoStore.Repository appinfoRepository, BankListStore.Repository bankListRepository) {
        return new PlatformInteractor(appinfoRepository, bankListRepository);
    }
}
