package vn.com.vng.zalopay.network;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.utils.AndroidUtils;

/**
 * Created by longlv on 10/6/16.
 * Base params that add to all request.
 */

public class BaseNetworkInterceptor implements Interceptor {

    private final String DEVICE_ID = AndroidUtils.getUUID();
    private final String DEVICE_MODEL = AndroidUtils.getDeviceManufacturer();
    private final String DEVICE_VERSION = AndroidUtils.getAndroidVersion();
    public static String CONNECTION_TYPE = AndroidUtils.getNetworkClass();
    private final String CARRIER_NAME = AndroidUtils.getCarrierName();

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        HttpUrl url = request.url().newBuilder()
                .addQueryParameter("platform", "android")
                .addQueryParameter("deviceID", DEVICE_ID)
                .addQueryParameter("deviceModel", DEVICE_MODEL)
                .addQueryParameter("osVer", DEVICE_VERSION)
                .addQueryParameter("appVer", BuildConfig.VERSION_NAME)
                .addQueryParameter("appversion", BuildConfig.VERSION_NAME)
                .addQueryParameter("sdkVer", vn.com.zalopay.wallet.BuildConfig.VERSION_NAME)
                .addQueryParameter("distSrc", "")
                .addQueryParameter("mno", CARRIER_NAME)
                .addQueryParameter("connType", CONNECTION_TYPE)
                .build();
        request = request.newBuilder().url(url).build();
        return chain.proceed(request);
    }
}
