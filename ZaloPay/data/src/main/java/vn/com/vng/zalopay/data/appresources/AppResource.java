package vn.com.vng.zalopay.data.appresources;

import java.util.HashMap;
import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import rx.Observable;
import vn.com.vng.zalopay.data.api.entity.AppResourceEntity;
import vn.com.vng.zalopay.data.api.response.AppResourceResponse;

/**
 * Created by huuhoa on 6/17/16.
 * Declare app resources interface for local storage, request service, repository
 */
public interface AppResource {
    interface LocalStorage {
        List<AppResourceEntity> get();
        void put(List<AppResourceEntity> resourceEntities);
        void updateAppList(List<Integer> listAppId);

        void increaseRetryDownload(long appId);
        void increaseStateDownload(int appId);
    }

    interface RequestService {

        /* platformcode
                dscreentype
            appidlist
                checksumlist*/

       /* android/ios
        "ios : iphone1x,iphone2x,iphone3x,ipad1x,ipad2x
        android : ldpi, dpi, mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi"
        json array
        json array
        */

        @GET("tpe/insideappresource")
        Observable<AppResourceResponse> insideappresource(@Query(encoded = false, value = "appidlist") List<Long> appidlist,
                                                          @Query("checksumlist") List<String> checksumlist,
                                                          @QueryMap HashMap<String, String> params,
                                                          @Query("appversion") String appVersion);

        @GET("tpe/getinsideappresource")
        Observable<AppResourceResponse> insideappresource(@Query(value = "appidlist", encoded = false) String appidlist,
                                                          @Query(value = "checksumlist", encoded = true) String checksumlist,
                                                          @QueryMap HashMap<String, String> params,
                                                          @Query("appversion") String appVersion);
    }

    interface Repository {
        Observable<Boolean> initialize();
        Observable<List<vn.com.vng.zalopay.domain.model.AppResource>> listAppResource();
    }
}
