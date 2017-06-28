package vn.com.zalopay.wallet.repository.appinfo;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.vng.zalopay.network.API_NAME;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfo;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfoResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.constants.TransactionType;

/**
 * Created by chucvv on 6/7/17.
 */

public class AppInfoStore {
    public interface LocalStorage {
        void put(long pAppId, AppInfoResponse appInfo);

        Observable<AppInfo> get(long appid);

        AppInfo getSync(long appId);

        long getExpireTime(long appid);

        void setExpireTime(long appId, long expireTime);

        String getAppInfoCheckSum(long appid);

        String getTranstypeCheckSum(String key);

        String getTranstypeCheckSumKey(long pAppId, @TransactionType int transtype);

        MiniPmcTransType getPmcTranstype(long pAppId, @TransactionType int transtype, boolean isBankAcount, String bankCode);

        List<String> getPmcTranstypeKeyList(long pAppID, @TransactionType int pTransType);
    }

    public interface Repository {
        Observable<AppInfoResponse> fetchCloud(long appid, String userid, String accesstoken, String appinfochecksum, String transtypes, String transtypechecksums, String appversion);

        AppInfoStore.LocalStorage getLocalStorage();
    }

    public interface AppInfoService {
        @GET(Constants.URL_APP_INFO)
        @API_NAME(value = {ZPEvents.API_V001_TPE_GETAPPINFO, ZPEvents.CONNECTOR_V001_TPE_GETAPPINFO})
        Observable<AppInfoResponse> fetch(@Query("appid") String appid, @Query("userid") String userid,
                                          @Query("accesstoken") String accesstoken, @Query("appinfochecksum") String appinfochecksum,
                                          @Query("transtypes") String transtypes, @Query("transtypechecksums") String transtypechecksums,
                                          @Query("appversion") String appversion);
    }
}
