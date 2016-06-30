package vn.com.vng.zalopay.data.api;

import okhttp3.RequestBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.api.response.MappingZaloAndZaloPayResponse;
import vn.com.vng.zalopay.data.api.response.UpdateProfileResponse;

/**
 * Created by longlv on 03/06/2016.
 */
public interface AccountService {

    @FormUrlEncoded
    @POST("um/updateprofile")
    Observable<BaseResponse> updateProfile(@Query("userid") String userid, @Query("accesstoken") String accesstoken, @Field("pin") String pin, @Field("phonenumber") String phonenumber);

    @FormUrlEncoded
    @POST("um/verifyotpprofile")
    Observable<UpdateProfileResponse> verifyOTPProfile(@Query("userid") String userid, @Query("accesstoken") String accesstoken, @Field("otp") String otp);

    @FormUrlEncoded
    @POST("um/recoverypin")
    Observable<BaseResponse> recoverypin(@Query("userid") String userid, @Query("accesstoken") String accesstoken, @Field("pin") String pin, @Field("otp") String otp);

    @FormUrlEncoded
    @POST("um/getuserinfo")
    Observable<MappingZaloAndZaloPayResponse> getuserinfo(@Query("userid") String userid, @Query("accesstoken") String accesstoken, @Field("loginuid") long zaloId, @Field("systemlogin") int systemlogin);

    @Multipart
    @POST("umupload/preupdateprofilelevel3")
    Observable<BaseResponse> updateProfile3(@Part("userid") long userid,
                                            @Part("accesstoken") String accesstoken,
                                            @Part("identitynumber") String identitynumber,
                                            @Part("email") String email,
                                            @Part("fimg") RequestBody fimg,
                                            @Part("bimg") RequestBody bimg,
                                            @Part("avataimg") RequestBody avataimg
    );

}
