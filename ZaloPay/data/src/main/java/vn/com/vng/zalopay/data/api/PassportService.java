package vn.com.vng.zalopay.data.api;


import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.response.LoginResponse;
import vn.com.vng.zalopay.data.api.response.LogoutResponse;
import vn.com.vng.zalopay.data.api.response.VerifyInvitationCodeResponse;

/**
 * Created by AnhHieu on 3/24/16.
 * *
 */
public interface PassportService {

    @FormUrlEncoded
    @POST(Constants.UM_API.CREATEACCESSTOKEN)
    Observable<LoginResponse> login(@Field("appid") long appid, @Field("loginuid") long zuid, @Field("oauthcode") String zAuthCode);


    @FormUrlEncoded
    @POST(Constants.UM_API.REMOVEACCESSTOKEN)
    Observable<LogoutResponse> logout(@Field("appid") long appid, @Field("userid") String uid, @Field("accesstoken") String accesstoken);


    @FormUrlEncoded
    @POST(Constants.UM_API.VERIFYCODETEST)
    Observable<VerifyInvitationCodeResponse> verifyCode(@Field("userid") String uid, @Field("accesstoken") String accesstoken, @Field("codetest") String codetest);
}
