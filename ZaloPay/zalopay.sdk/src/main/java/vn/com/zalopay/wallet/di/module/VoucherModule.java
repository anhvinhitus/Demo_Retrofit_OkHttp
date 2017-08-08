package vn.com.zalopay.wallet.di.module;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import vn.com.zalopay.wallet.di.qualifier.Voucher;
import vn.com.zalopay.wallet.repository.SharedPreferencesManager;
import vn.com.zalopay.wallet.repository.voucher.VoucherLocalStorage;
import vn.com.zalopay.wallet.repository.voucher.VoucherStore;

/**
 * Created by chucvv on 8/1/17.
 */

@Module
public class VoucherModule {
    @Provides
    @Singleton
    public VoucherStore.VoucherService provideVoucherService(@Voucher Retrofit retrofit) {
        return retrofit.create(VoucherStore.VoucherService.class);
    }

    @Provides
    @Singleton
    public VoucherStore.LocalStorage provideLocalStorage(SharedPreferencesManager sharedPreferencesManager) {
        return new VoucherLocalStorage(sharedPreferencesManager);
    }
}
