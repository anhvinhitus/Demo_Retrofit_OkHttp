package vn.com.zalopay.wallet.datasource;

import retrofit2.Call;

/**
 * this class keep 2 request callable used for retry platform info and download resource
 */
public class RequestKeeper {
    public static Call requestPlatformInfo;

    public static boolean isCanRetryFlatformInfo() {
        return requestPlatformInfo != null;
    }
}
