/*
package vn.com.vng.zalopay.data.api;

import java.util.HashMap;
import java.util.List;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import rx.Observable;
import vn.com.vng.zalopay.data.api.response.AppResourceResponse;
import vn.com.vng.zalopay.data.api.response.PlatformInfoResponse;

*/
/**
 * Created by AnhHieu on 4/28/16.
 *//*

public interface AppConfigService {
    */
/*userid
            accesstoken
    platformcode
            dscreentype
    platforminfochecksum
            resourceversion
    appversion
            mno
    devicemodel*//*


    @FormUrlEncoded
    @POST("tpe/getplatforminfo")
    Observable<PlatformInfoResponse> platforminfo(@Field("userid") String userid,
                                                  @Field("accesstoken") String accesstoken,
                                                  @Field("platformcode") String platformcode,
                                                  @Field("dscreentype") String dscreentype,
                                                  @Field("platforminfochecksum") String platforminfochecksum,
                                                  @Field("resourceversion") String resourceversion,
                                                  @Field("appversion") String appversion,
                                                  @Field("mno") String mno,
                                                  @Field("devicemodel") String devicemodel
    );
}
*/
