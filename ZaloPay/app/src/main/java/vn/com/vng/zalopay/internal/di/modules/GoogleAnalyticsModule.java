package vn.com.vng.zalopay.internal.di.modules;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.data.cache.global.DaoSession;
import vn.com.vng.zalopay.data.ga.AnalyticsLocalStorage;
import vn.com.vng.zalopay.data.ga.AnalyticsRepository;
import vn.com.vng.zalopay.data.ga.AnalyticsStore;
import vn.com.vng.zalopay.network.ToStringConverterFactory;
import vn.com.vng.zalopay.tracker.GoogleReporter;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.HttpLoggingInterceptor;

/**
 * Created by hieuvm on 6/6/17.
 * *
 */
@Module
public class GoogleAnalyticsModule {

    @Singleton
    @Provides
    AnalyticsStore.LocalStorage provideAnalyticsStorage(@Named("globaldaosession") DaoSession session) {
        return new AnalyticsLocalStorage(session);
    }

    @Singleton
    @Provides
    AnalyticsStore.RequestService providesAnalyticsService(@Named("retrofitGoogleAnalytics") Retrofit retrofit) {
        return retrofit.create(AnalyticsStore.RequestService.class);
    }

    @Singleton
    @Provides
    AnalyticsStore.Repository provideAnalyticsRepository(Context context, AnalyticsStore.LocalStorage localStorage, AnalyticsStore.RequestService requestService) {
        return new AnalyticsRepository(localStorage, requestService, AndroidUtils.getUserAgent(context));
    }

    @Provides
    @Singleton
    @Named("okHttpClientGA")
    OkHttpClient provideOkHttpClientGA() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (BuildConfig.DEBUG && BuildConfig.GA_DEBUG) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(Timber::i);
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(interceptor);
        }
        builder.connectionPool(new ConnectionPool(Constants.CONNECTION_POOL_COUNT, Constants.CONNECTION_KEEP_ALIVE_DURATION, TimeUnit.MINUTES));
        builder.connectTimeout(10, TimeUnit.SECONDS);
        builder.readTimeout(5, TimeUnit.SECONDS);
        return builder.build();
    }

    @Provides
    @Singleton
    @Named("retrofitGoogleAnalytics")
    Retrofit provideRetrofitGA(@Named("okHttpClientGA") OkHttpClient okHttpClient, CallAdapter.Factory callAdapter) {
        return new Retrofit.Builder()
                .addConverterFactory(new ToStringConverterFactory())
                .addCallAdapterFactory(callAdapter)
                .baseUrl(GoogleReporter.BASE_URL)
                .validateEagerly(BuildConfig.DEBUG)
                .client(okHttpClient)
                .build();
    }

}
