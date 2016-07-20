package vn.com.vng.zalopay.internal.di.modules;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import vn.com.vng.zalopay.data.api.AppConfigService;
import vn.com.vng.zalopay.data.api.ZaloPayIAPService;
import vn.com.vng.zalopay.data.api.ZaloPayService;
import vn.com.vng.zalopay.internal.di.scope.UserScope;

/**
 * Created by AnhHieu on 4/26/16.
 */

@Module
public class UserApiModule {

    @Provides
    @UserScope
    AppConfigService provideAppConfigService(@Named("retrofitApi") Retrofit retrofit) {
        return retrofit.create(AppConfigService.class);
    }

    @Provides
    @UserScope
    ZaloPayService provideZaloPayService(@Named("retrofitApi") Retrofit retrofit) {
        return retrofit.create(ZaloPayService.class);
    }


    @Provides
    @UserScope
    ZaloPayIAPService provideZaloPayIAPService(@Named("retrofitApi") Retrofit retrofit) {
        return retrofit.create(ZaloPayIAPService.class);
    }

}
