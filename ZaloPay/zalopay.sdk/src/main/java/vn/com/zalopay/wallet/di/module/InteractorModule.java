package vn.com.zalopay.wallet.di.module;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import vn.com.zalopay.wallet.interactor.AppInfoInteractor;
import vn.com.zalopay.wallet.interactor.BankListInteractor;
import vn.com.zalopay.wallet.interactor.IAppInfo;
import vn.com.zalopay.wallet.interactor.IBankList;
import vn.com.zalopay.wallet.interactor.IPlatformInfo;
import vn.com.zalopay.wallet.interactor.PlatformInfoInteractor;
import vn.com.zalopay.wallet.repository.appinfo.AppInfoStore;
import vn.com.zalopay.wallet.repository.banklist.BankListStore;
import vn.com.zalopay.wallet.repository.platforminfo.PlatformInfoStore;

/**
 * Created by chucvv on 6/7/17.
 */
@Module
public class InteractorModule {
    @Provides
    @Singleton
    IPlatformInfo providePlatformInteractor(PlatformInfoStore.Repository platformInfoRepository) {
        return new PlatformInfoInteractor(platformInfoRepository);
    }

    @Provides
    @Singleton
    IBankList provideBankListInteractor(BankListStore.Repository bankListRepository) {
        return new BankListInteractor(bankListRepository);
    }

    @Provides
    @Singleton
    IAppInfo provideAppInfoInteractor(AppInfoStore.Repository appinfoRepository) {
        return new AppInfoInteractor(appinfoRepository);
    }
}
