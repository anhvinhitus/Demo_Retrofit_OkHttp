package vn.com.zalopay.wallet.api;


import javax.inject.Inject;

import retrofit2.Retrofit;
import vn.com.zalopay.wallet.di.qualifier.Api;
import vn.com.zalopay.wallet.di.qualifier.Download;

public class InjectionWrapper {
    @Inject
    @Api
    protected Retrofit mRetrofit;

    @Inject
    @Download
    protected Retrofit mRetrofitDownloadResource;

    public Retrofit getRetrofit() {
        return mRetrofit;
    }

    public Retrofit getRetrofitDownloadResource() {
        return mRetrofitDownloadResource;
    }
}
