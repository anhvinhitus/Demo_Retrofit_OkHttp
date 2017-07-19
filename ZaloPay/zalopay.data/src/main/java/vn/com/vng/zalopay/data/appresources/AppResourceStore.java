package vn.com.vng.zalopay.data.appresources;

import java.util.HashMap;
import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import rx.Observable;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.entity.AppResourceEntity;
import vn.com.vng.zalopay.data.api.response.AppResourceResponse;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.network.API_NAME;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by AnhHieu on 8/10/16.
 * *
 */
public interface AppResourceStore {

    interface LocalStorage {

        List<AppResourceEntity> getAllAppResource();

        AppResourceEntity get(long appId);

        void put(AppResourceEntity appResourceEntity);

        void put(List<AppResourceEntity> resourceEntities);

        void updateAppList(List<Long> listAppId);

        void increaseRetryDownload(long appId);

        void increaseDownloadState(long appId);

        void resetDownloadState(long appId);

        void resetResourceState(long appId);

        void sortApplication(List<Long> list);
    }

    interface RequestService {

        @API_NAME(https = ZPEvents.API_V001_TPE_GETINSIDEAPPRESOURCE, connector = ZPEvents.CONNECTOR_V001_TPE_GETINSIDEAPPRESOURCE)
        @GET(Constants.TPE_API.GETINSIDEAPPRESOURCE)
        Observable<AppResourceResponse> getinsideappresource(@Query(value = "appidlist", encoded = false) String appidlist,
                                                             @Query(value = "checksumlist", encoded = true) String checksumlist,
                                                             @QueryMap HashMap<String, String> params,
                                                             @Query("appversion") String appVersion);
    }

    interface Repository {

        Observable<Boolean> ensureAppResourceAvailable();

        Observable<List<AppResource>> listInsideAppResource();

        Observable<Boolean> isAppResourceAvailable(long appId);

        Observable<Boolean> isAppResourceAvailable(long appId, boolean downloadIfNeed);

        Observable<List<AppResource>> fetchAppResource();

        Observable<List<AppResource>> getAppResourceLocal();

        Observable<List<AppResource>> getListAppHome();

        Observable<List<AppResource>> getListAppHomeLocal();

        Observable<List<AppResource>> fetchListAppHome();

        Observable<Void> resetStateResource(long appId);

        Observable<Boolean> existAppResource(long appid);

        Exception exception(Exception pException);

    }

}
