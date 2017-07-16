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

        String getUnzipPath();

        void setUnzipPath(String pUnzipPath);

        void setResourceDownloadUrl(String resourceDownloadUrl);

        String getUserId();

        void setCheckSum(String checkSum);
    }

    public interface Repository {
        Observable<PlatformInfoResponse> fetchCloud(Map<String, String> params);

        PlatformInfoStore.LocalStorage getLocalStorage();
    }

    public interface PlatformInfoService {
        @GET(Constants.URL_PLATFORM_INFO)
        @API_NAME(https = ZPEvents.API_V001_TPE_V001GETPLATFORMINFO, connector = ZPEvents.CONNECTOR_V001_TPE_V001GETPLATFORMINFO)
        Observable<PlatformInfoResponse> fetch(@QueryMap Map<String, String> params);
    }
}
