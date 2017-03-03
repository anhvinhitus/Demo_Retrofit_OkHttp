package vn.com.vng.zalopay;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.com.zalopay.wallet.business.data.Constants;

public class RetrofitSetup {
    public static final String API_BASE_URL = MainActivity.mUrl;

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create());

    public static <S> S createService(Class<S> serviceClass) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        httpClient.addInterceptor(logging);

        //set timeout
        httpClient.readTimeout(Constants.API_READ_REQUEST_TIMEOUT, TimeUnit.SECONDS).connectTimeout(Constants.API_CONNECTING_REQUEST_TIMEOUT, TimeUnit.SECONDS);

        Retrofit retrofit = builder.client(httpClient.build()).build();
        return retrofit.create(serviceClass);
    }

}
