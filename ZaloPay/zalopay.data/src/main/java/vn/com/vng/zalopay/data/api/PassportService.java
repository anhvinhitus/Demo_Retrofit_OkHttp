package vn.com.vng.zalopay.data.api;


import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
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

    @API_NAME(https = ZPEvents.API_UM_LOGINVIAZALO, connector = ZPEvents.CONNECTOR_UM_LOGINVIAZALO)
    @FormUrlEncoded
    @POST(Constants.UM_API.LOGINVIAZALO)
    Observable<LoginResponse> loginViaZalo(@Field("appid") long appid, @Field("zaloid") long zaloid, @Field("oauthcode") String oauthcode);


    @API_NAME(https = ZPEvents.API_UM_REMOVEACCESSTOKEN, connector = ZPEvents.CONNECTOR_UM_REMOVEACCESSTOKEN)
    @FormUrlEncoded
    @POST(Constants.UM_API.REMOVEACCESSTOKEN)
    Observable<LogoutResponse> logout(@Field("appid") long appid, @Field("userid") String uid, @Field("accesstoken") String accesstoken);

    @API_NAME(https = ZPEvents.API_UM_REGISTERPHONENUMBER, connector = ZPEvents.CONNECTOR_UM_REGISTERPHONENUMBER)
    @FormUrlEncoded
    @POST(Constants.UM_API.REGISTERPHONENUMBER)
    Observable<BaseResponse> registerPhoneNumber(@Query("zaloid") long zaloid, @Query("oauthcode") String oauthcode, @Field("pin") String pin, @Field("phonenumber") String phonenumber);

    @API_NAME(https = ZPEvents.API_UM_AUTHENPHONENUMBER, connector = ZPEvents.CONNECTOR_UM_AUTHENPHONENUMBER)
    @FormUrlEncoded
    @POST(Constants.UM_API.AUTHENPHONENUMBER)
    Observable<LoginResponse> authenticatePhoneNumber(@Query("zaloid") long zaloid, @Query("oauthcode") String oauthcode, @Field("otp") String otp);

}
