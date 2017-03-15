package vn.com.zalopay.wallet.configure;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import vn.com.zalopay.wallet.business.data.Constants;

public class SDKConfiguration {
    protected Builder mConfigBuilder;

    public SDKConfiguration(Builder pConfigBuilder) {
        this.mConfigBuilder = pConfigBuilder;
    }

    public static Builder builder() {
        return new Builder();
    }

    public OkHttpClient getHttpClient() {
        return mConfigBuilder.getHttpClient();
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

    public String getBaseHostUrl() {
        return mConfigBuilder.getBaseHostUrl();
    }

    public static class Builder {
        protected boolean mReleaseBuild;
        protected String mBaseHostUrl;
        /***
         * this http client get from app, with connect timeout : read timeout = 10ms:5ms
         */
        protected OkHttpClient mHttpClient;

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

        public OkHttpClient getHttpClient() {
            return mHttpClient;
        }

        public Builder setHttpClient(OkHttpClient pOkHttpClient) {
            mHttpClient = pOkHttpClient;
            return this;
        }

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

        public String getBaseHostUrl() {
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
