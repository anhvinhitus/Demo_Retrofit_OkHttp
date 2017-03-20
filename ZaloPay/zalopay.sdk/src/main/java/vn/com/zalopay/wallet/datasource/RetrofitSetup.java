package vn.com.zalopay.wallet.datasource;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.utils.Log;

public class RetrofitSetup {
    private static Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl(SDKApplication.getBaseHostUrl())
            .addConverterFactory(GsonConverterFactory.create());

    public static <S> S createServiceFromRetrofit(Retrofit pRetrofit, Class<S> serviceClass) {
        return pRetrofit.create(serviceClass);
    }

    public static <S> S createService(OkHttpClient httpClient, Class<S> serviceClass) {

        if (httpClient == null) {
            //re create okhttpclient if null
            OkHttpClient.Builder newHttpClient = new OkHttpClient().newBuilder();
            newHttpClient.readTimeout(Constants.API_READ_REQUEST_TIMEOUT, TimeUnit.MILLISECONDS)
                    .writeTimeout(Constants.API_READ_REQUEST_TIMEOUT, TimeUnit.MILLISECONDS)
                    .connectTimeout(Constants.API_CONNECTING_REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
            if (!SDKApplication.isReleaseBuild()) {
                Log.d("createService", "=====creating new httpClient======");
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                newHttpClient.addInterceptor(logging);
            }
            httpClient = newHttpClient.build();
        }
        Log.d("createService", "connect timeout " + httpClient.connectTimeoutMillis() + " readtimeout " + httpClient.readTimeoutMillis() + " writetimeout " + httpClient.writeTimeoutMillis());

        Retrofit retrofit = builder.client(httpClient).build();
        return retrofit.create(serviceClass);
    }

}
