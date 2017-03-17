package vn.com.zalopay.wallet.configure;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import vn.com.zalopay.wallet.business.data.Constants;

public class SDKConfiguration {
    protected Builder mConfigBuilder;

    public SDKConfiguration(Builder pConfigBuilder) {
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

    public boolean isReleaseBuild() {
        return mConfigBuilder.isReleaseBuild();
    }

    public Constants.HostType getHostType() {
        return mConfigBuilder.getEnumEnvironment();
    }

    public static class Builder {
        protected boolean mReleaseBuild;
        protected Constants.HostType mEnumEnvironment;
        /***
         * this http client for get platform info and download resource
         * connect timeout : read timeout = 30ms : 30ms.
         */
        protected OkHttpClient mHttpClientTimeoutLonger;
        /***
         * retrofit initialized from app for payment
         * connector
         */
        protected Retrofit mRetrofit;

        public OkHttpClient getHttpClientTimeoutLonger() {
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

        public boolean isReleaseBuild() {
            return mReleaseBuild;
        }

        public Builder setReleaseBuild(boolean pReleaseBuild) {
            mReleaseBuild = pReleaseBuild;
            return this;
        }

        public Constants.HostType getEnumEnvironment() {
            return mEnumEnvironment;
        }

        public Builder setHostType(Constants.HostType pHostType) {
            mEnumEnvironment = pHostType;
            return this;
        }

        public SDKConfiguration build() {
            return new SDKConfiguration(this);
        }
    }
}
