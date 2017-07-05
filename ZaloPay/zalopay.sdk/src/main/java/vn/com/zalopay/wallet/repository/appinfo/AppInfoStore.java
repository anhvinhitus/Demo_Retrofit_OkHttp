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

    public interface Interactor {
        void setExpireTime(long appId, long expireTime);

        MiniPmcTransType getPmcTranstype(long pAppId, @TransactionType int transtype, boolean isBankAcount, String bankCode);

        AppInfo get(long appid);

        List<String> getPmcTranstypeKeyList(long pAppID, @TransactionType int pTransType);

        Observable<AppInfo> loadAppInfo(long appid, @TransactionType int[] transtypes, String userid, String accesstoken, String appversion, long currentTime);
    }

    public interface RequestService {
        @GET(Constants.URL_APP_INFO)
        @API_NAME(https = ZPEvents.API_V001_TPE_GETAPPINFO, connector = ZPEvents.CONNECTOR_V001_TPE_GETAPPINFO)
        Observable<AppInfoResponse> fetch(@Query("appid") String appid, @Query("userid") String userid,
                                          @Query("accesstoken") String accesstoken, @Query("appinfochecksum") String appinfochecksum,
                                          @Query("transtypes") String transtypes, @Query("transtypechecksums") String transtypechecksums,
                                          @Query("appversion") String appversion);
    }
}
