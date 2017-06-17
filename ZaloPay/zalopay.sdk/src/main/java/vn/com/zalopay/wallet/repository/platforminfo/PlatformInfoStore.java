package vn.com.zalopay.wallet.repository.platforminfo;

import java.util.Map;

import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import rx.Observable;
import vn.com.vng.zalopay.network.API_NAME;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.PlatformInfoResponse;
import vn.com.zalopay.wallet.interactor.PlatformInfoCallback;

/**
 * Created by chucvv on 6/7/17.
 */

public class PlatformInfoStore {
    public interface LocalStorage {
        void put(String userId, PlatformInfoResponse platformInfoResponse);

        long getExpireTime();

        long getExpireTimeDuration();

        String getPlatformInfoCheckSum();

        String getAppVersion();

        String getResourceVersion();

        void setResourceVersion(String resourceVersion);

        String getCardInfoCheckSum();

        void setCardInfoCheckSum(String checkSum);

        String getBankAccountCheckSum();

        void setBankAccountCheckSum(String checkSum);

        String getUnzipPath();

        void setUnzipPath(String pUnzipPath);

        void setAppVersion(String pAppVersion);

        String getResourceDownloadUrl();

        void setResourceDownloadUrl(String resourceDownloadUrl);

        String getUserId();

        void clearCardMapCheckSum();

        void clearBankAccountMapCheckSum();

        Observable<PlatformInfoCallback> get();//return expire time
    }

    public interface Repository {
        Observable<PlatformInfoResponse> fetchCloud(Map<String, String> params);

        PlatformInfoStore.LocalStorage getLocalStorage();
    }

    public interface PlatformInfoService {
        @GET(Constants.URL_PLATFORM_INFO)
        @API_NAME(ZPEvents.CONNECTOR_V001_TPE_V001GETPLATFORMINFO)
        Observable<PlatformInfoResponse> fetch(@QueryMap Map<String, String> params);
    }
}
