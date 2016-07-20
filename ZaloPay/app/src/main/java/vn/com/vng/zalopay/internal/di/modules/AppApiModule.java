package vn.com.vng.zalopay.internal.di.modules;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import vn.com.vng.zalopay.data.api.PassportService;

@Module
public class AppApiModule {

    @Provides
    @Singleton
    PassportService providePassportService(@Named("retrofitApi") Retrofit retrofit) {
        return retrofit.create(PassportService.class);
    }

   /* @Provides
    @Singleton
    @Named("loginUseCase")
    UseCase provideLoginUseCase(LoginUseCase loginUseCase) {
        return loginUseCase;
    }
*/
}