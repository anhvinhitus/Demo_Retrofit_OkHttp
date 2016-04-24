package vn.com.vng.zalopay.internal.di.modules;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import vn.com.vng.zalopay.data.api.PassportService;

/**
 * Created by AnhHieu on 4/2/16.
 */

@Module
public class AppControllerModule {

    @Singleton
    @Provides
    PassportService providePassportService(@Named("Retrofit") Retrofit retrofit) {
        return retrofit.create(PassportService.class);
    }
}
