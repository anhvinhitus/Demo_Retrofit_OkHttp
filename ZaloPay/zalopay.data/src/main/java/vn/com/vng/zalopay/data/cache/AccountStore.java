package vn.com.vng.zalopay.data.cache;

import android.support.annotation.Nullable;

import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.api.response.GetUserInfoByZPIDResponse;
import vn.com.vng.zalopay.data.api.response.GetUserInfoByZPNameResponse;
import vn.com.vng.zalopay.data.api.response.UserProfileLevelResponse;
import vn.com.vng.zalopay.domain.model.Person;
import vn.com.vng.zalopay.domain.model.ProfileInfo3;
import vn.com.vng.zalopay.network.API_NAME;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by AnhHieu on 7/3/16.
 * AccountStore to provide User Profile management feature
 */
public interface AccountStore {

    interface LocalStorage {
        Person get(String zpName);

        Person getById(String zaloPayId);

        void put(Person person);

        void saveProfileInfo3(String email, String identity, String foregroundImg, String backgroundImg, String avatarImg);

        Map<String, String> getProfileInfo3();

        Map<String, String> getChangePinState();

        void saveChangePinState(boolean receiveOtp);
    }

    interface RequestService {

        @API_NAME(https = ZPEvents.API_UM_RECOVERYPIN, connector = ZPEvents.CONNECTOR_UM_RECOVERYPIN)
        @FormUrlEncoded
        @POST(Constants.UM_API.RECOVERYPIN)
        Observable<BaseResponse> recoverypin(@Query("userid") String userid, @Query("accesstoken") String accesstoken, @Field("oldpin") String oldPin, @Field("pin") String newPin, @Field("otp") String otp);

        @API_NAME(https = ZPEvents.API_UM_GETUSERPROFILELEVEL, connector = ZPEvents.CONNECTOR_UM_GETUSERPROFILELEVEL)
        @GET(Constants.UM_API.GETUSERPROFILELEVEL)
        Observable<UserProfileLevelResponse> getUserProfileLevel(@Query("userid") String userid, @Query("accesstoken") String accesstoken);

        @API_NAME(https = ZPEvents.API_UM_GETUSERINFOBYZALOPAYNAME, connector = ZPEvents.CONNECTOR_UM_GETUSERINFOBYZALOPAYNAME)
        @GET(Constants.UM_API.GETUSERINFOBYZALOPAYNAME)
        Observable<GetUserInfoByZPNameResponse> getUserInfoByZaloPayName(@Query("zalopayname") String zalopayname, @Query("userid") String userid, @Query("accesstoken") String accesstoken);

        @API_NAME(https = ZPEvents.API_UM_GETUSERINFOBYZALOPAYID, connector = ZPEvents.CONNECTOR_UM_GETUSERINFOBYZALOPAYID)
        @GET(Constants.UM_API.GETUSERINFOBYZALOPAYID)
        Observable<GetUserInfoByZPIDResponse> getUserInfoByZaloPayId(@Query("requestid") String zalopayid, @Query("userid") String userid, @Query("accesstoken") String accesstoken);

        @API_NAME(https = ZPEvents.API_UM_CHECKZALOPAYNAMEEXIST, connector = ZPEvents.CONNECTOR_UM_CHECKZALOPAYNAMEEXIST)
        @GET(Constants.UM_API.CHECKZALOPAYNAMEEXIST)
        Observable<BaseResponse> checkZaloPayNameExist(@Query("zalopayname") String zalopayname, @Query("userid") String userid, @Query("accesstoken") String accesstoken);

        @API_NAME(https = ZPEvents.API_UM_UPDATEZALOPAYNAME, connector = ZPEvents.CONNECTOR_UM_UPDATEZALOPAYNAME)
        @FormUrlEncoded
        @POST(Constants.UM_API.UPDATEZALOPAYNAME)
        Observable<BaseResponse> updateZaloPayName(@Field("zalopayname") String zalopayname, @Field("userid") String userid, @Field("accesstoken") String accesstoken);

        @API_NAME(https = ZPEvents.API_UM_VALIDATEPIN, connector = ZPEvents.CONNECTOR_UM_VALIDATEPIN)
        @FormUrlEncoded
        @POST(Constants.UM_API.VALIDATEPIN)
        Observable<BaseResponse> validatePin(@Field("pin") String pin, @Field("userid") String userid, @Field("accesstoken") String accesstoken);

    }

    interface UploadPhotoService {
        @API_NAME(https = ZPEvents.API_UMUPLOAD_PREUPDATEPROFILELEVEL3, connector = ZPEvents.CONNECTOR_UMUPLOAD_PREUPDATEPROFILELEVEL3)
        @Multipart
        @POST(Constants.UMUPLOAD_API.PREUPDATEPROFILELEVEL3)
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

        Observable<String> validatePinSha256(String pin);

        Observable<String> changePassword(String newPassword, String oldPassword);

        Observable<String> changePasswordSha256(String oldHashedPassword, String newHashedPassword);

        Observable<Boolean> verifyChangePassword(String otp);

        Observable<Person> getUserInfoByZaloPayId(String zaloPayId);

        Observable<Boolean> updateUserProfileLevel3(String identityNumber,
                                                    String email,
                                                    byte[] frontImage,
                                                    byte[] backImage,
                                                    byte[] avatar);

        Observable<Boolean> getUserProfileLevelCloud();

        Observable<Person> getUserInfoByZaloPayName(String zaloPayName);

        Observable<Boolean> checkZaloPayNameExist(String zaloPayName);

        Observable<Boolean> updateZaloPayName(String zaloPayName);

        Observable<ProfileInfo3> getProfileInfo3Cache();

        Observable<Boolean> saveProfileInfo3(String email, String identity, @Nullable String foregroundImg, @Nullable String backgroundImg, @Nullable String avatarImg);

        Observable<Boolean> getChangePinState();

        Observable<Void> saveChangePinState(boolean receiveOtp);

        Observable<Void> resetChangePinState();
    }
}
