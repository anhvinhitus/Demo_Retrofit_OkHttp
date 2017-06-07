package vn.com.zalopay.wallet.repository.appinfo;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.vng.zalopay.network.API_NAME;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfoResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DAppInfo;

/**
 * Created by chucvv on 6/7/17.
 */

public class AppInfoStore {
    public interface LocalStorage {
        void putAppInfo(String pAppId, AppInfoResponse appInfo);

        Observable<DAppInfo> getAppInfo(String appid);

        long getExpireTime(String appid);

        String getCheckSum(String appid);
    }

    public interface Repository {
        Observable<DAppInfo> fetchAppInfoCloud(String appid, String userid, String accesstoken, String checksum, String appversion);

        AppInfoStore.LocalStorage getLocalStorage();
    }

    public interface AppInfoService {
        @GET(Constants.URL_APP_INFO)
        @API_NAME(ZPEvents.CONNECTOR_V001_TPE_GETAPPINFO)
        Observable<AppInfoResponse> fetchAppInfo(@Query("appid") String appid, @Query("userid") String userid, @Query("accesstoken") String accesstoken, @Query("checksum") String checksum, @Query("appversion") String appversion);
    }
}
