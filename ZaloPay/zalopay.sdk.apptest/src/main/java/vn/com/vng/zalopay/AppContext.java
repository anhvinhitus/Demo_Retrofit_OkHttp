package vn.com.vng.zalopay;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.configure.SDKConfiguration;
import vn.com.zalopay.wallet.controller.SDKApplication;

/**
 * Created by admin on 9/19/16.
 */
public class AppContext extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());

        SDKConfiguration sdkConfig = SDKConfiguration.builder()
                .setHttpClientTimeoutLonger(createOKHttpClient())
                .setRetrofit(createRetrofit())
                .setReleaseBuild(!BuildConfig.DEBUG)
                .setBaseHostUrl(BuildConfig.HOST)
                .build();

        SDKApplication.initialize(this,sdkConfig);
        Stetho.initializeWithDefaults(this);
    }

    protected OkHttpClient createOKHttpClient()
    {
        OkHttpClient.Builder httpClient = new OkHttpClient().newBuilder();

        long apiReadTimeout = Constants.API_READ_REQUEST_TIMEOUT;
        long apiConnectTimeout = Constants.API_CONNECTING_REQUEST_TIMEOUT;

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        httpClient.readTimeout(apiReadTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(apiReadTimeout, TimeUnit.MILLISECONDS)
                .connectTimeout(apiConnectTimeout, TimeUnit.MILLISECONDS);

        httpClient.addInterceptor(logging);

        httpClient.addNetworkInterceptor(new StethoInterceptor());

        //use for download resource.
        OkHttpClient.Builder httpClientLonger = new OkHttpClient().newBuilder();

        apiReadTimeout = Constants.API_PLATFORM_TIMEOUT;
        apiConnectTimeout = Constants.API_PLATFORM_TIMEOUT;

        httpClientLonger.readTimeout(apiReadTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(apiReadTimeout, TimeUnit.MILLISECONDS)
                .connectTimeout(apiConnectTimeout, TimeUnit.MILLISECONDS);

        httpClientLonger.addInterceptor(logging);

        httpClient.addNetworkInterceptor(new StethoInterceptor());

        SDKConfiguration sdkConfig = SDKConfiguration.builder()
                .setHttpClient(httpClient.build())
                .setHttpClientTimeoutLonger(httpClientLonger.build())
                .setReleaseBuild(!BuildConfig.DEBUG)
                .setBaseHostUrl(BuildConfig.HOST)
                .build();

        SDKApplication.initialize(this,sdkConfig);
        Stetho.initializeWithDefaults(this);
        return httpClient.build();
    }
    protected Retrofit createRetrofit()
    {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(BuildConfig.HOST)
                .addConverterFactory(GsonConverterFactory.create());
        return builder.client(createOKHttpClient()).build();
    }
}
