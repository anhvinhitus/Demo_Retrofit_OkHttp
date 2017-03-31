package vn.com.vng.zalopay.data.api;


import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.response.LoginResponse;
import vn.com.vng.zalopay.data.api.response.LogoutResponse;
import vn.com.vng.zalopay.data.api.response.VerifyInvitationCodeResponse;
import vn.com.vng.zalopay.network.API_NAME;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by AnhHieu on 3/24/16.
 * *
 */
public interface PassportService {

    @API_NAME(ZPEvents.API_UM_CREATEACCESSTOKEN)
    @FormUrlEncoded
    @POST(Constants.UM_API.CREATEACCESSTOKEN)
    Observable<LoginResponse> login(@Field("appid") long appid, @Field("loginuid") long zuid, @Field("oauthcode") String zAuthCode);


    @API_NAME(ZPEvents.API_UM_REMOVEACCESSTOKEN)
    @FormUrlEncoded
    @POST(Constants.UM_API.REMOVEACCESSTOKEN)
    Observable<LogoutResponse> logout(@Field("appid") long appid, @Field("userid") String uid, @Field("accesstoken") String accesstoken);


    @API_NAME(ZPEvents.API_UM_VERIFYCODETEST)
    @FormUrlEncoded
    @POST(Constants.UM_API.VERIFYCODETEST)
    Observable<VerifyInvitationCodeResponse> verifyCode(@Field("userid") String uid, @Field("accesstoken") String accesstoken, @Field("codetest") String codetest);
}
