package vn.com.vng.zalopay.data.api;


import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.api.response.LoginResponse;
import vn.com.vng.zalopay.data.api.response.LogoutResponse;

/**
 * Created by AnhHieu on 3/24/16.
 */
public interface PassportService {

    @FormUrlEncoded
    @POST("um/createaccesstoken")
    Observable<LoginResponse> login(@Field("appid") long appid, @Field("loginuid") long zuid, @Field("oauthcode") String zAuthCode);


    @FormUrlEncoded
    @POST("um/removeaccesstoken")
    Observable<LogoutResponse> logout(@Field("appid") long appid, @Field("userid") long uid, @Field("accesstoken") String accesstoken);

    @FormUrlEncoded
    @POST("um/verifyaccesstoken")
    Observable<BaseResponse> verifyAccessToken(@Field("appid") long appid, @Field("userid") long uid, @Field("accesstoken") String accesstoken);
}
