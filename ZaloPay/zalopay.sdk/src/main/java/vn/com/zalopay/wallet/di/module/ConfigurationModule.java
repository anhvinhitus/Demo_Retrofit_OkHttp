package vn.com.zalopay.wallet.di.module;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.com.zalopay.wallet.configure.SDKConfiguration;
import vn.com.zalopay.wallet.di.qualifier.Api;
import vn.com.zalopay.wallet.di.qualifier.Download;
import vn.com.zalopay.wallet.di.qualifier.Voucher;

@Singleton
@Module
public class ConfigurationModule {

    private SDKConfiguration mConfig;

    public ConfigurationModule(SDKConfiguration pConfig) {
        this.mConfig = pConfig;
    }

    @Provides
    @Singleton
    public SDKConfiguration provideSDKConfiguration() {
        return mConfig;
    }

    @Provides
    @Singleton
    @Api
    public Retrofit provideRetrofit() {
        return this.mConfig.getRetrofit();
    }

    @Provides
    @Singleton
    @Voucher
    public Retrofit provideVoucherRetrofit() {
        return this.mConfig.getVoucherRetrofit();
    }

    @Provides
    @Singleton
    @Download
    public Retrofit provideRetrofitDownloadResource() {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(mConfig.getBaseHostUrl())
                .addConverterFactory(GsonConverterFactory.create());
        return builder.client(mConfig.getHttpClientTimeoutLonger()).build();
    }
}
