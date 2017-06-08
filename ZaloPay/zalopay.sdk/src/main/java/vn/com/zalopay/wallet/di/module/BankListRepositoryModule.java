package vn.com.zalopay.wallet.di.module;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.di.qualifier.Api;
import vn.com.zalopay.wallet.repository.banklist.BankListLocalStorage;
import vn.com.zalopay.wallet.repository.banklist.BankListRepository;
import vn.com.zalopay.wallet.repository.banklist.BankListStore;

/**
 * Created by chucvv on 6/7/17.
 */
@Module
public class BankListRepositoryModule {
    @Provides
    @Singleton
    public BankListStore.BankListService provideBankListService(@Api Retrofit retrofit) {
        return retrofit.create(BankListStore.BankListService.class);
    }

    @Provides
    @Singleton
    public BankListStore.LocalStorage provideBankListLocalStorage(SharedPreferencesManager sharedPreferencesManager) {
        return new BankListLocalStorage(sharedPreferencesManager);
    }

    @Provides
    @Singleton
    public BankListStore.Repository provideBankListRepository(BankListStore.BankListService bankListService, BankListStore.LocalStorage localStorage) {
        return new BankListRepository(bankListService, localStorage);
    }
}
