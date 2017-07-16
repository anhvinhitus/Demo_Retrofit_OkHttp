package vn.com.zalopay.wallet.repository.platforminfo;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;
import vn.com.vng.zalopay.network.API_NAME;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PlatformInfoResponse;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.interactor.PlatformInfoCallback;
import vn.com.zalopay.wallet.merchant.entities.Maintenance;
import vn.com.zalopay.wallet.repository.AbstractLocalStorage;

/**
 * Created by chucvv on 6/7/17.
 */

public class PlatformInfoStore {
    public interface LocalStorage extends AbstractLocalStorage.LocalStorage {
        void put(String userId, PlatformInfoResponse platformInfoResponse);

        long getExpireTime();

        void setExpireTime(long expireTime);

        long getExpireTimeDuration();

        String getPlatformInfoCheckSum();

        String getAppVersion();

        void setAppVersion(String pAppVersion);

        String getResourceVersion();

        void setResourceVersion(String resourceVersion);

        String getCardInfoCheckSum();

        void setCardInfoCheckSum(String checkSum);

        String getBankAccountCheckSum();

        void setBankAccountCheckSum(String checkSum);

        void setResourcePath(String pUnzipPath);

        void setResourceDownloadUrl(String resourceDownloadUrl);

        String getUserId();

        void setCheckSum(String checkSum);

        boolean enableTopup();

        String getResourcePath();

        Maintenance withdrawMaintain();
    }

    public interface Interactor {
        boolean isNewVersion(String appVersion);

        boolean isNewUser(String userId);

        long getExpireTime();

        String getAppVersion();

        String getPlatformInfoCheckSum();

        String getUserId();

        String getResourcePath();

        String getResourceVersion();

        boolean validFileConfig();

        boolean enableTopup();

        Maintenance withdrawMaintain();

        Observable<PlatformInfoCallback> loadSDKPlatform(String userId, String accessToken, long currentTime);

        Observable<PlatformInfoCallback> loadSDKPlatformFromCloud(String userId, String accessToken, boolean forceReloadApi, boolean forceDownloadResource);

        /***
         * Platform info expire time,unix time to exprired time (in milisecond)
         * After this expire time, client need hit to server again
         * @return
         */
        long getPlatformInfoDurationExpire();

    }

    public interface PlatformInfoService {
        @GET(Constants.URL_PLATFORM_INFO)
        @API_NAME(https = ZPEvents.API_V001_TPE_V001GETPLATFORMINFO, connector = ZPEvents.CONNECTOR_V001_TPE_V001GETPLATFORMINFO)
        Observable<PlatformInfoResponse> fetch(@QueryMap Map<String, String> params);
    }
}
