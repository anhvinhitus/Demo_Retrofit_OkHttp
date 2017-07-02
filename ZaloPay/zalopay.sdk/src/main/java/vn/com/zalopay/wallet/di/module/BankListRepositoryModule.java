package vn.com.zalopay.wallet.di.module;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.di.qualifier.Api;
import vn.com.zalopay.wallet.repository.bank.BankLocalStorage;
import vn.com.zalopay.wallet.repository.bank.BankStore;

/**
 * Created by chucvv on 6/7/17.
 */
@Module
public class BankListRepositoryModule {
    @Provides
    @Singleton
    public BankStore.BankListService provideBankListService(@Api Retrofit retrofit) {
        return retrofit.create(BankStore.BankListService.class);
    }

    @Provides
    @Singleton
    public BankStore.LocalStorage provideBankListLocalStorage(SharedPreferencesManager sharedPreferencesManager) {
        return new BankLocalStorage(sharedPreferencesManager);
    }
}
