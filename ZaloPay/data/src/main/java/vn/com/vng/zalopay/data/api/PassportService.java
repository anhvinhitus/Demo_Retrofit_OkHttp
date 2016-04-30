package vn.com.vng.zalopay.data.api;


import java.util.HashMap;

import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;
import rx.Observable;
import vn.com.vng.zalopay.data.api.response.LoginResponse;
import vn.com.vng.zalopay.data.api.response.LogoutResponse;

/**
 * Created by AnhHieu on 3/24/16.
 */
public interface PassportService {

    @FormUrlEncoded
    @POST("um/createaccesstoken")
    Observable<LoginResponse> login(@FieldMap HashMap<String, String> authParams, @QueryMap HashMap<String, String> params);

    @FormUrlEncoded
    @POST("um/removeaccesstoken")
    Observable<LogoutResponse> logout(@Field("accesstoken") String accesstoken, @FieldMap HashMap<String, String> authParams, @QueryMap HashMap<String, String> params);
}
