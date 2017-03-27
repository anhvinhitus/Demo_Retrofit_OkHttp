package vn.com.vng.zalopay.internal.di.modules;

import android.content.Context;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
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
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.data.net.adapter.RxJavaCallAdapterFactory;
import vn.com.vng.zalopay.data.net.adapter.ToStringConverterFactory;
import vn.com.vng.zalopay.data.ws.model.NotificationEmbedData;
import vn.com.vng.zalopay.data.ws.parser.NotificationMessageDeserializer;
import vn.com.vng.zalopay.domain.executor.PostExecutionThread;
import vn.com.vng.zalopay.domain.executor.ThreadExecutor;
import vn.com.vng.zalopay.network.BaseNetworkInterceptor;
import vn.com.vng.zalopay.utils.HttpLoggingInterceptor;

import static vn.com.vng.zalopay.data.net.adapter.RxJavaCallAdapterFactory.AdapterType.React;
import static vn.com.vng.zalopay.data.net.adapter.RxJavaCallAdapterFactory.AdapterType.ZaloPay;

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
        gsonBuilder.registerTypeAdapter(NotificationEmbedData.class, new NotificationMessageDeserializer());
        gsonBuilder.registerTypeAdapter(long.class, NumberTypeAdapter);
        gsonBuilder.registerTypeAdapter(Long.class, NumberTypeAdapter);
        gsonBuilder.registerTypeAdapter(double.class, NumberTypeAdapter);
        gsonBuilder.registerTypeAdapter(Double.class, NumberTypeAdapter);
        return gsonBuilder.create();
    }

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient(Cache cache) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(Timber::i);
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(interceptor);
        }
        builder.addInterceptor(new BaseNetworkInterceptor());
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
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(Timber::i);
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(interceptor);
        }
        builder.addInterceptor(new BaseNetworkInterceptor());
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
        return RxJavaCallAdapterFactory.create(context, ZaloPay);
    }

    @Provides
    @Singleton
    GsonConverterFactory providesGsonConverterFactory(Gson gson) {
        return GsonConverterFactory.create(gson);
    }

    @Provides
    @Singleton
    ToStringConverterFactory providesStringConverterFactory() {
        return new ToStringConverterFactory();
    }

    @Provides
    @Singleton
    Converter.Factory providesConvertFactory(Gson gson) {
        return GsonConverterFactory.create(gson);
    }

    @Provides
    @Singleton
    @Named("retrofitApi")
    Retrofit provideRetrofit(HttpUrl baseUrl, OkHttpClient okHttpClient,
                             CallAdapter.Factory callAdapter,
                             Converter.Factory convertFactory) {
        return new Retrofit.Builder()
                .addConverterFactory(convertFactory)
                .addCallAdapterFactory(callAdapter)
                .baseUrl(baseUrl)
                .validateEagerly(BuildConfig.DEBUG)
                .client(okHttpClient)
                .build();
    }

    @Provides
    @Singleton
    @Named("retrofitPhoto")
    Retrofit provideRetrofitUploadPhoto(OkHttpClient okHttpClient, CallAdapter.Factory callAdapter,
                                        Converter.Factory convertFactory) {
        return new Retrofit.Builder()
                .addConverterFactory(convertFactory)
                .addCallAdapterFactory(callAdapter)
                .baseUrl(BuildConfig.UPLOAD_PHOTO_HOST)
                .validateEagerly(BuildConfig.DEBUG)
                .client(okHttpClient)
                .build();
    }

    private static final TypeAdapter<Number> NumberTypeAdapter = new TypeAdapter<Number>() {

        /**
         * Writes one JSON value (an array, object, string, number, boolean or null)
         * for {@code value}.
         *
         * @param out
         * @param value the Java object to write. May be null.
         */
        @Override
        public void write(JsonWriter out, Number value) throws IOException {
            out.value(value);
        }

        /**
         * Reads one JSON value (an array, object, string, number, boolean or null)
         * and converts it to a Java object. Returns the converted object.
         *
         * @param in
         * @return the converted Java object. May be null.
         */
        @Override
        public Number read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            String result = in.nextString();
            if ("".equals(result)) {
                return null;
            }
            try {
                return Long.parseLong(result);
            } catch (NumberFormatException e) {
                // empty catch exception to try another parser Double
            }

            try {
                return Double.parseDouble(result);
            } catch (NumberFormatException e) {
                throw new JsonSyntaxException(e);
            }
        }
    };
}
