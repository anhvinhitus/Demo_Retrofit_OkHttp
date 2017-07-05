package vn.com.zalopay.wallet.di.module;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.data.cache.MemoryCache;
import vn.com.vng.zalopay.monitors.ZPMonitorEventTiming;
import vn.com.zalopay.wallet.interactor.AppInfoInteractor;
import vn.com.zalopay.wallet.interactor.BankInteractor;
import vn.com.zalopay.wallet.interactor.ChannelListInteractor;
import vn.com.zalopay.wallet.interactor.IBankInteractor;
import vn.com.zalopay.wallet.interactor.ILinkSourceInteractor;
import vn.com.zalopay.wallet.interactor.IPlatformInfo;
import vn.com.zalopay.wallet.interactor.LinkSourceInteractor;
import vn.com.zalopay.wallet.interactor.PlatformInfoInteractor;
import vn.com.zalopay.wallet.repository.appinfo.AppInfoStore;
import vn.com.zalopay.wallet.repository.bank.BankStore;
import vn.com.zalopay.wallet.repository.bankaccount.BankAccountStore;
import vn.com.zalopay.wallet.repository.cardmap.CardStore;
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
    IBankInteractor provideBankListInteractor(BankStore.LocalStorage localStorage, BankStore.BankListService bankListService, MemoryCache memoryCache) {
        return new BankInteractor(localStorage, bankListService, memoryCache);
    }

    @Provides
    @Singleton
    AppInfoStore.Interactor provideAppInfoInteractor(AppInfoStore.RequestService requestService, AppInfoStore.LocalStorage localStorage) {
        return new AppInfoInteractor(requestService, localStorage);
    }

    @Provides
    @Singleton
    ILinkSourceInteractor provideLinkInteactor(CardStore.Repository cardRepository,
                                               BankAccountStore.Repository bankAccountRepository) {
        return new LinkSourceInteractor(cardRepository, bankAccountRepository);
    }

    @Provides
    @Singleton
    ChannelListInteractor provideChannelListInteractor(Application application,
                                                       IPlatformInfo platformInteractor,
                                                       AppInfoStore.Interactor appInfoInteractor,
                                                       IBankInteractor bankInteractor,
                                                       ZPMonitorEventTiming eventTiming) {
        return new ChannelListInteractor(application,
                platformInteractor, appInfoInteractor, bankInteractor,
                eventTiming);
    }
}
