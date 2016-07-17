package vn.com.vng.zalopay.data.cache;

import okhttp3.RequestBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.api.response.MappingZaloAndZaloPayResponse;
import vn.com.vng.zalopay.data.api.response.UpdateProfileResponse;
import vn.com.vng.zalopay.domain.model.MappingZaloAndZaloPay;
import vn.com.vng.zalopay.domain.model.ProfilePermission;

/**
 * Created by AnhHieu on 7/3/16.
 */
public interface AccountStore {

    interface LocalStorage {
    }

    interface RequestService {

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

        @GET("um/getuserprofilelevel")
        Observable<UpdateProfileResponse> getUserProfileLevel(@Query("userid") String userid, @Query("accesstoken") String accesstoken);
    }

    interface UploadPhotoService {
        @Multipart
        @POST("umupload/preupdateprofilelevel3")
        Observable<BaseResponse> updateProfile3(@Part("userid") RequestBody userid,
                                                @Part("accesstoken") RequestBody accesstoken,
                                                @Part("identitynumber") RequestBody identitynumber,
                                                @Part("email") RequestBody email,
                                                @Part("fimg\"; filename=\"fimg.jpeg") RequestBody fimg,
                                                @Part("bimg\"; filename=\"bimg.jpeg") RequestBody bimg,
                                                @Part("avataimg\"; filename=\"avataimg.jpeg") RequestBody avataimg
        );
    }

    interface Repository {

        Observable<Boolean> updateProfile(String pin, String phonenumber);

        Observable<ProfilePermission> verifyOTPProfile(String otp);

        Observable<BaseResponse> recoverypin(String pin, String otp);

        Observable<MappingZaloAndZaloPay> getuserinfo(long zaloId, int systemlogin);

        Observable<Boolean> updateProfile3(String identityNumber,
                                           String email,
                                           String fimgPath,
                                           String bimgPath,
                                           String avatarPath);

        Observable<Boolean> updateProfile3(String identityNumber,
                                           String email,
                                           byte[] fimgPath,
                                           byte[] bimgPath,
                                           byte[] avatarPath);

        Observable<ProfilePermission> getUserProfileLevel();
    }
}
