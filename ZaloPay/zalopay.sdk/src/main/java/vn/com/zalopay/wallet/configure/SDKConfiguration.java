package vn.com.zalopay.wallet.configure;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

public class SDKConfiguration {
    private Builder mConfigBuilder;

    SDKConfiguration(Builder pConfigBuilder) {
        this.mConfigBuilder = pConfigBuilder;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder getBuilder() {
        return mConfigBuilder;
    }

    public OkHttpClient getHttpClientTimeoutLonger() {
        return mConfigBuilder.getHttpClientTimeoutLonger();
    }

    public Retrofit getRetrofit() {
        return mConfigBuilder.getRetrofit();
    }

    public Retrofit getVoucherRetrofit(){
        return mConfigBuilder.getVoucherRetrofit();
    }

    public boolean isReleaseBuild() {
        return mConfigBuilder.isReleaseBuild();
    }

    public String getBaseHostUrl() {
        return mConfigBuilder.getBaseHostUrl();
    }

    public static class Builder {
        /*
         * retrofit for calling promotion apis
         */
        Retrofit mVoucherRetrofit;
        boolean mReleaseBuild;
        String mBaseHostUrl;
        /*
         * retrofit initialized from app for payment
         * connector
         */
        Retrofit mRetrofit;
        /*
         * this http client for get platform info and download resource
         * connect timeout : read timeout = 30ms : 30ms.
         */
        OkHttpClient mHttpClientTimeoutLonger;

        OkHttpClient getHttpClientTimeoutLonger() {
            return mHttpClientTimeoutLonger;
        }

        public Builder setHttpClientTimeoutLonger(OkHttpClient pOkHttpClient) {
            mHttpClientTimeoutLonger = pOkHttpClient;
            return this;
        }

        public Retrofit getRetrofit() {
            return mRetrofit;
        }

        public Builder setRetrofit(Retrofit pRetrofit) {
            mRetrofit = pRetrofit;
            return this;
        }

        Retrofit getVoucherRetrofit() {
            return mVoucherRetrofit;
        }

        public Builder setVoucherRetrofit(Retrofit pRetrofit) {
            mVoucherRetrofit = pRetrofit;
            return this;
        }

        boolean isReleaseBuild() {
            return mReleaseBuild;
        }

        public Builder setReleaseBuild(boolean pReleaseBuild) {
            mReleaseBuild = pReleaseBuild;
            return this;
        }

        String getBaseHostUrl() {
            return mBaseHostUrl;
        }

        public Builder setBaseHostUrl(String pBaseHostUrl) {
            mBaseHostUrl = pBaseHostUrl;
            return this;
        }

        public SDKConfiguration build() {
            return new SDKConfiguration(this);
        }
    }
}
