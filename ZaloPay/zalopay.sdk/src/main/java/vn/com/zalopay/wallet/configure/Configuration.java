package vn.com.zalopay.wallet.configure;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import vn.com.zalopay.wallet.business.data.Constants;

public class Configuration {
    protected Builder mConfigBuilder;

    public Configuration(Builder pConfigBuilder) {
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

    public Constants.HostType getHostType() {
        return mConfigBuilder.getEnumEnvironment();
    }

    public static class Builder {
        protected boolean mReleaseBuild;
        protected Constants.HostType mEnumEnvironment;
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

        public Builder setHttpClientLongTimeout(OkHttpClient pOkHttpClient) {
            mHttpClientTimeoutLonger = pOkHttpClient;
            return this;
        }

        public Builder setHostType(Constants.HostType pHostType) {
            mEnumEnvironment = pHostType;
            return this;
        }

        public Configuration build() {
            return new Configuration(this);
        }
    }
}
