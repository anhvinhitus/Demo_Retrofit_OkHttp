package vn.com.vng.zalopay.data.api;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.api.response.UpdateProfileResponse;

/**
 * Created by longlv on 03/06/2016.
 */
public interface AccountService {

    @FormUrlEncoded
    @POST("um//updateprofile")
    Observable<BaseResponse> updateProfile(@Field("pin") String pin, @Field("phonenumber") String phonenumber);

    @FormUrlEncoded
    @POST("um/verifyotpprofile")
    Observable<UpdateProfileResponse> verifyOTPProfile(@Field("otp") String otp);
}
