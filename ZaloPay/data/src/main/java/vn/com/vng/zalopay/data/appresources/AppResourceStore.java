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

        void increaseStateDownload(long appId);

        void resetStateDownload(long appId);

        void sortApplication(List<Long> list);
    }

    interface RequestService {

        @GET(Constants.TPE_API.INSIDEAPPRESOURCE)
        Observable<AppResourceResponse> insideappresource(@Query(encoded = false, value = "appidlist") List<Long> appidlist,
                                                          @Query("checksumlist") List<String> checksumlist,
                                                          @QueryMap HashMap<String, String> params,
                                                          @Query("appversion") String appVersion);

        @GET(Constants.TPE_API.GETINSIDEAPPRESOURCE)
        Observable<AppResourceResponse> insideappresource(@Query(value = "appidlist", encoded = false) String appidlist,
                                                          @Query(value = "checksumlist", encoded = true) String checksumlist,
                                                          @QueryMap HashMap<String, String> params,
                                                          @Query("appversion") String appVersion);
    }

    interface Repository {
        Observable<Boolean> initialize();

        Observable<List<AppResource>> listInsideAppResource();

        Observable<Boolean> existResource(long appId);
    }

}
