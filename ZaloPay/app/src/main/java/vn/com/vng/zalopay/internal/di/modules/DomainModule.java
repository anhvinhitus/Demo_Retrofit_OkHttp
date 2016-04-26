package vn.com.vng.zalopay.internal.di.modules;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import vn.com.vng.zalopay.data.api.PassportService;
import vn.com.vng.zalopay.domain.interactor.LoginUseCase;
import vn.com.vng.zalopay.domain.interactor.UseCase;

@Module
public class DomainModule {

    @Provides
    @Singleton
    PassportService providePassportService(@Named("retrofit") Retrofit retrofit) {
        return retrofit.create(PassportService.class);
    }

    @Provides
    @Singleton
    @Named("loginUseCase")
    UseCase provideLoginUseCase(LoginUseCase loginUseCase) {
        return loginUseCase;
    }

}