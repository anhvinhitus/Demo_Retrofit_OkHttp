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
import vn.com.vng.zalopay.data.api.response.UserProfileLevelResponse;
import vn.com.vng.zalopay.domain.model.MappingZaloAndZaloPay;

/**
 * Created by AnhHieu on 7/3/16.
 * AccountStore to provide User Profile management feature
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
        Observable<UserProfileLevelResponse> getUserProfileLevel(@Query("userid") String userid, @Query("accesstoken") String accesstoken);
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

        Observable<Boolean> updateUserProfileLevel2(String pin, String phoneNumber);

        Observable<Boolean> verifyOTPProfile(String otp);

        Observable<BaseResponse> recoveryPin(String pin, String otp);

        Observable<MappingZaloAndZaloPay> getUserInfo(long zaloId, int systemLogin);

        Observable<Boolean> updateUserProfileLevel3(String identityNumber,
                                                    String email,
                                                    String frontImagePath,
                                                    String backImagePath,
                                                    String avatarPath);

        Observable<Boolean> updateUserProfileLevel3(String identityNumber,
                                                    String email,
                                                    byte[] frontImage,
                                                    byte[] backImage,
                                                    byte[] avatar);

        Observable<Boolean> getUserProfileLevelCloud();
    }
}
