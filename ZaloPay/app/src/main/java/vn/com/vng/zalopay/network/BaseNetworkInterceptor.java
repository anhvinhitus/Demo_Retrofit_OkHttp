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

    public static final String PLATFORM_PARAMETER = "platform";
    public static final String DEVICE_ID_PARAMETER = "deviceID";
    public static final String DEVICE_MODEL_PARAMETER = "deviceModel";
    public static final String OS_VERSION_PARAMETER = "osVer";
    public static final String APPLICATION_VERSION_PARAMETER = "appversion";
    public static final String PAYMENT_SDK_VERSION_PARAMETER = "sdkVer";
    public static final String DISTSRC_PARAMETER = "distSrc";
    public static final String MNO_PARAMETER = "mno";
    public static final String CONNECTION_TYPE_PARAMETER = "connType";

    private final String DEVICE_ID = AndroidUtils.getUUID();
    private final String DEVICE_MODEL = AndroidUtils.getDeviceManufacturer();
    private final String DEVICE_VERSION = AndroidUtils.getAndroidVersion();
    public static String CONNECTION_TYPE = AndroidUtils.getNetworkClass();
    private final String CARRIER_NAME = AndroidUtils.getCarrierName();

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        HttpUrl url = request.url().newBuilder()
                .addQueryParameter(PLATFORM_PARAMETER, "android")
                .addQueryParameter(DEVICE_ID_PARAMETER, DEVICE_ID)
                .addQueryParameter(DEVICE_MODEL_PARAMETER, DEVICE_MODEL)
                .addQueryParameter(OS_VERSION_PARAMETER, DEVICE_VERSION)
                .addQueryParameter(APPLICATION_VERSION_PARAMETER, BuildConfig.VERSION_NAME)
                .addQueryParameter(PAYMENT_SDK_VERSION_PARAMETER, vn.com.zalopay.wallet.BuildConfig.VERSION_NAME)
                .addQueryParameter(DISTSRC_PARAMETER, "")
                .addQueryParameter(MNO_PARAMETER, CARRIER_NAME)
                .addQueryParameter(CONNECTION_TYPE_PARAMETER, CONNECTION_TYPE)
                .build();
        request = request.newBuilder().url(url).build();
        return chain.proceed(request);
    }
}
