package vn.com.vng.zalopay.data.api;

import java.util.HashMap;
import java.util.List;

import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;
import rx.Observable;
import vn.com.vng.zalopay.data.api.response.AppResourceResponse;
import vn.com.vng.zalopay.data.api.response.PlatformInfoResponse;

/**
 * Created by AnhHieu on 4/28/16.
 */
public interface AppConfigService {
    /*userid
            accesstoken
    platformcode
            dscreentype
    platforminfochecksum
            resourceversion
    appversion
            mno
    devicemodel*/

    @FormUrlEncoded
    @POST("tpe/getplatforminfo")
    Observable<PlatformInfoResponse> platforminfo(@Field("userid") long userid,
                                                  @Field("accesstoken") String accesstoken,
                                                  @Field("platformcode") String platformcode,
                                                  @Field("dscreentype") String dscreentype,
                                                  @Field("platforminfochecksum") String platforminfochecksum,
                                                  @Field("resourceversion") String resourceversion,
                                                  @Field("appversion") String appversion,
                                                  @Field("mno") String mno,
                                                  @Field("devicemodel") String devicemodel
    );

/*    platformcode
            dscreentype
    appidlist
            checksumlist*/

   /* android/ios
    "ios : iphone1x,iphone2x,iphone3x,ipad1x,ipad2x
    android : ldpi, dpi, mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi"
    json array
    json array*/

    @GET("tpe/insideappresource")
    Observable<AppResourceResponse> insideappresource(@Query(encoded = false, value = "appidlist") List<Long> appidlist,
                                                      @Query("checksumlist") List<String> checksumlist,
                                                      @QueryMap HashMap<String, String> params);

    @GET("tpe/insideappresource")
    Observable<AppResourceResponse> insideappresource(@Query(value = "appidlist", encoded = false) String appidlist,
                                                      @Query(value = "checksumlist", encoded = true) String checksumlist,
                                                      @QueryMap HashMap<String, String> params);
}
