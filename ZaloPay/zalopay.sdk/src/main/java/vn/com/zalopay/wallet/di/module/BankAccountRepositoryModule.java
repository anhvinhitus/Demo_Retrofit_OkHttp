package vn.com.zalopay.wallet.di.module;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.di.qualifier.Api;
import vn.com.zalopay.wallet.repository.bankaccount.BankAccountLocalStorage;
import vn.com.zalopay.wallet.repository.bankaccount.BankAccountStore;

/**
 * Created by chucvv on 6/7/17.
 */
@Module
public class BankAccountRepositoryModule {
    @Provides
    @Singleton
    public BankAccountStore.BankAccountService provideBankAccountService(@Api Retrofit retrofit) {
        return retrofit.create(BankAccountStore.BankAccountService.class);
    }

    @Provides
    @Singleton
    public BankAccountStore.LocalStorage provideBankAccountLocalStorage(SharedPreferencesManager sharedPreferencesManager) {
        return new BankAccountLocalStorage(sharedPreferencesManager);
    }
}
