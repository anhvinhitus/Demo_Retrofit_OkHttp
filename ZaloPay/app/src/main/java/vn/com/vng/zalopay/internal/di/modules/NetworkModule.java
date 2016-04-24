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
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.data.net.adapter.CustomRxJavaCallAdapterFactory;

/**
 * Created by AnhHieu on 3/25/16.
 */
@Module
public class NetworkModule {

    public static final HttpUrl PRODUCTION_API_URL = HttpUrl.parse("http://dev.lizks.com/");

    @Provides
    @Singleton
    HttpUrl provideBaseUrl() {
        return PRODUCTION_API_URL;
    }

    public NetworkModule() {
    }


    @Provides
    @Singleton
    Cache provideOkHttpCache(Context application) {
        int cacheSize = 50 * 1024 * 1024; // 10 MiB
        Cache cache = new Cache(application.getCacheDir(), cacheSize);
        return cache;
    }

  /*  @Provides @Named("cached")
    @Singleton
    OkHttpClient provideOkHttpClient(Cache cache) {
        OkHttpClient client = new OkHttpClient();
        client.setCache(cache);
    }

    @Provides @Named("non_cached") @Singleton
    OkHttpClient provideOkHttpClient() {
        OkHttpClient client = new OkHttpClient();
        return client;
    }*/

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
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(interceptor);
        }
        builder.cache(cache);
        builder.connectionPool(new ConnectionPool(Constants.CONNECTION_POOL_COUNT, Constants.KEEP_ALIVE_DURATION_MS, TimeUnit.MILLISECONDS));
        return builder.build();
    }

    @Provides
    @Singleton
    @Named("Retrofit")
    Retrofit provideRetrofit(HttpUrl baseUrl, Gson gson, OkHttpClient okHttpClient) {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(CustomRxJavaCallAdapterFactory.create())
                .baseUrl(baseUrl)
                .validateEagerly(BuildConfig.DEBUG)
                .client(okHttpClient)
                .build();
        return retrofit;
    }


}
