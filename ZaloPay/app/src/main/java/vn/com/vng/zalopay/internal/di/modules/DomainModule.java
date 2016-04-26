package vn.com.vng.zalopay.internal.di.modules;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import vn.com.vng.zalopay.domain.interactor.LoginUseCase;
import vn.com.vng.zalopay.domain.interactor.UseCase;

@Module
public class DomainModule {
    @Provides
    @Named("loginUseCase")
    UseCase provideLoginUseCase(LoginUseCase loginUseCase) {
        return loginUseCase;
    }

}