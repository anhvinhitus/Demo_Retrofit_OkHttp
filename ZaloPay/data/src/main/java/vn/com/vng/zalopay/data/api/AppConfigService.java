package vn.com.vng.zalopay.data.api;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;
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

}
