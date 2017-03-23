package vn.com.zalopay.wallet.datasource;


import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.Retrofit;

public class InjectionWrapper {
    @Inject
    protected Retrofit mRetrofit;

    @Inject
    @Named("HttpDownloadResource")
    protected Retrofit mRetrofitDownloadResource;

    public Retrofit getRetrofit() {
        return mRetrofit;
    }

    public Retrofit getRetrofitDownloadResource() {
        return mRetrofitDownloadResource;
    }
}
