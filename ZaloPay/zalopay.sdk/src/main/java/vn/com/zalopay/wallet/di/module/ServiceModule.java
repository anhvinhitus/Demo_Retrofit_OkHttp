package vn.com.zalopay.wallet.di.module;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;

@Module
public class ServiceModule {
    protected Retrofit mRetrofit;

    public ServiceModule(Retrofit pRetrofit) {
        this.mRetrofit = pRetrofit;
    }

    @Provides
    @Singleton
    public Retrofit provideRetrofit() {
        return this.mRetrofit;
    }
}
