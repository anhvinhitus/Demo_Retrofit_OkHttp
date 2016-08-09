package vn.com.vng.zalopay.internal.di.modules;

import android.content.Context;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.data.net.adapter.RxJavaCallAdapterFactory;
import vn.com.vng.zalopay.domain.executor.PostExecutionThread;
import vn.com.vng.zalopay.domain.executor.ThreadExecutor;
import vn.com.vng.zalopay.utils.HttpLoggingInterceptor;

/**
 * Created by AnhHieu on 3/25/16.
 */
@Module
public class NetworkModule {

    private static final HttpUrl API_HTTP_URL = HttpUrl.parse(BuildConfig.HOST);

    @Provides
    @Singleton
    HttpUrl provideBaseUrl() {
        return API_HTTP_URL;
    }

    public NetworkModule() {
    }

    @Provides
    @Singleton
    Cache provideOkHttpCache(Context application) {
        int cacheSize = 50 * 1024 * 1024; // 10 MiB
        return new Cache(application.getCacheDir(), cacheSize);
    }

    @Provides
    @Singleton
    Gson provideGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        return gsonBuilder.create();
    }

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient(Cache cache) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(String message) {
                    Timber.i(message);
                }
            });
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(interceptor);
        }
        builder.cache(cache);
        builder.connectionPool(new ConnectionPool(Constants.CONNECTION_POOL_COUNT, Constants.CONNECTION_KEEP_ALIVE_DURATION, TimeUnit.MINUTES));
        builder.connectTimeout(10, TimeUnit.SECONDS);
        builder.readTimeout(5, TimeUnit.SECONDS);
        return builder.build();
    }

    @Provides
    @Singleton
    @Named("OkHttpClientTimeoutLonger")
    OkHttpClient provideOkHttpClientTimeoutLonger(Cache cache) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(String message) {
                    Timber.i(message);
                }
            });
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(interceptor);
        }
        builder.cache(cache);
        builder.connectionPool(new ConnectionPool(Constants.CONNECTION_POOL_DOWNLOAD_COUNT,
                Constants.CONNECTION_KEEP_ALIVE_DOWNLOAD_DURATION, TimeUnit.MINUTES));
        builder.connectTimeout(30, TimeUnit.SECONDS);
        builder.readTimeout(30, TimeUnit.SECONDS);
        return builder.build();
    }

    @Provides
    @Singleton
    CallAdapter.Factory provideCallAdapter(ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread, Context context) {
        return RxJavaCallAdapterFactory.create(context, RxJavaCallAdapterFactory.AdapterType.ZaloPay);
    }

    @Provides
    @Singleton
    @Named("retrofitApi")
    Retrofit provideRetrofit(HttpUrl baseUrl, Gson gson, OkHttpClient okHttpClient, CallAdapter.Factory callAdapter) {
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(callAdapter)
                .baseUrl(baseUrl)
                .validateEagerly(BuildConfig.DEBUG)
                .client(okHttpClient)
                .build();
    }

    @Provides
    @Singleton
    @Named("retrofitRedPacketApi")
    Retrofit provideRetrofitRedPacketApi(HttpUrl baseUrl, Gson gson, OkHttpClient okHttpClient, Context context) {
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create(context, RxJavaCallAdapterFactory.AdapterType.RedPacket))
                .baseUrl(baseUrl)
                .validateEagerly(BuildConfig.DEBUG)
                .client(okHttpClient)
                .build();
    }

    @Provides
    @Singleton
    @Named("retrofitPhoto")
    Retrofit provideRetrofitUploadPhoto(Gson gson, OkHttpClient okHttpClient, CallAdapter.Factory callAdapter) {
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(callAdapter)
                .baseUrl(BuildConfig.UPLOAD_PHOTO_HOST)
                .validateEagerly(BuildConfig.DEBUG)
                .client(okHttpClient)
                .build();
    }

}
