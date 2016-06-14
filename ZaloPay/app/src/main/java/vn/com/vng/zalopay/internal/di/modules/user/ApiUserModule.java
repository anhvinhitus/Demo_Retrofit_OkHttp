package vn.com.vng.zalopay.internal.di.modules.user;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import vn.com.vng.zalopay.data.api.AccountService;
import vn.com.vng.zalopay.data.api.AppConfigService;
import vn.com.vng.zalopay.data.api.ZaloPayIAPService;
import vn.com.vng.zalopay.data.api.ZaloPayService;
import vn.com.vng.zalopay.data.cache.BalanceStore;
import vn.com.vng.zalopay.internal.di.scope.UserScope;

/**
 * Created by AnhHieu on 4/26/16.
 */

@Module
public class ApiUserModule {

    @Provides
    @UserScope
    AppConfigService provideAppConfigService(@Named("retrofit") Retrofit retrofit) {
        return retrofit.create(AppConfigService.class);
    }

    @Provides
    @UserScope
    ZaloPayService provideZaloPayService(@Named("retrofit") Retrofit retrofit) {
        return retrofit.create(ZaloPayService.class);
    }

    @Provides
    @UserScope
    BalanceStore.RequestService provideBalanceRequestService(@Named("retrofit") Retrofit retrofit) {
        return retrofit.create(BalanceStore.RequestService.class);
    }

    @Provides
    @UserScope
    AccountService provideAccountService(@Named("retrofit") Retrofit retrofit) {
        return retrofit.create(AccountService.class);
    }

    @Provides
    @UserScope
    ZaloPayIAPService provideZaloPayIAPService(@Named("retrofit") Retrofit retrofit) {
        return retrofit.create(ZaloPayIAPService.class);
    }

}
