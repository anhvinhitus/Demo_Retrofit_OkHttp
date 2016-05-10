package vn.com.vng.zalopay.data.api;

import java.util.HashMap;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import rx.Observable;
import vn.com.vng.zalopay.data.api.response.PlatformInfoResponse;

/**
 * Created by AnhHieu on 4/28/16.
 */
public interface AppConfigService {

    @FormUrlEncoded
    @POST("tpe/platforminfo")
    Observable<PlatformInfoResponse> platforminfo(@Query("platformcode") String platformcode,
                                                  @Query("dscreentype") String dscreentype,
                                                  @Query("platforminfochecksum") String platforminfochecksum,
                                                  @Query("resourceversion") String resourceversion,
                                                  @Query("mno") String mno,
                                                  @Query("userid") long userid,
                                                  @Field("accesstoken") String accesstoken,
                                                  @QueryMap HashMap<String, String> params);

}
