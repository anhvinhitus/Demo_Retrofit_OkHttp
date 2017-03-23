package vn.com.zalopay.wallet.di.module;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.com.zalopay.wallet.configure.SDKConfiguration;

@Singleton
@Module
public class ConfigurationModule {

    protected SDKConfiguration mConfig;

    public ConfigurationModule(SDKConfiguration pConfig) {
        this.mConfig = pConfig;
    }

    @Provides
    @Singleton
    public SDKConfiguration provideSDKConfiguration()
    {
        return mConfig;
    }

    @Provides
    @Singleton
    public Retrofit provideRetrofit() {
        return this.mConfig.getRetrofit();
    }

    @Provides
    @Singleton
    @Named("HttpDownloadResource")
    public Retrofit provideRetrofitDownloadResource() {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(mConfig.getBaseHostUrl())
                .addConverterFactory(GsonConverterFactory.create());
        Retrofit retrofit = builder.client(mConfig.getHttpClientTimeoutLonger()).build();
        return retrofit;
    }
}
