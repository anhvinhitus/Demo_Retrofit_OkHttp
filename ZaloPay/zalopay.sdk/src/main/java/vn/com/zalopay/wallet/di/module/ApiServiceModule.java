package vn.com.zalopay.wallet.di.module;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import vn.com.zalopay.wallet.api.IDownloadService;
import vn.com.zalopay.wallet.api.ITransService;
import vn.com.zalopay.wallet.di.qualifier.Api;
import vn.com.zalopay.wallet.di.qualifier.Download;

/**
 * Created by chucvv on 6/16/17.
 */

@Module
public class ApiServiceModule {
    @Provides
    @Singleton
    public ITransService provideTransactionService(@Api Retrofit retrofit) {
        return retrofit.create(ITransService.class);
    }

    @Provides
    @Singleton
    public IDownloadService provideDownloadService(@Download Retrofit retrofit) {
        return retrofit.create(IDownloadService.class);
    }
}
