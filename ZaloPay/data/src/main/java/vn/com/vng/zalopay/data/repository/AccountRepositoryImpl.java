package vn.com.vng.zalopay.data.repository;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import rx.Observable;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.domain.model.MappingZaloAndZaloPay;
import vn.com.vng.zalopay.domain.model.ProfilePermission;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by longlv on 03/06/2016.
 *
 */
public class AccountRepositoryImpl implements AccountStore.Repository {

    final AccountStore.RequestService mRequestService;
    final AccountStore.UploadPhotoService mUploadPhotoService;

    final User mUser;
    final UserConfig mUserConfig;


    public AccountRepositoryImpl(AccountStore.RequestService accountService,
                                 AccountStore.UploadPhotoService photoService,
                                 UserConfig userConfig,
                                 User user) {
        this.mRequestService = accountService;
        this.mUploadPhotoService = photoService;
        this.mUser = user;
        this.mUserConfig = userConfig;
    }

    @Override
    public Observable<Boolean> updateUserProfileLevel2(String pin, String phonenumber) {
        return mRequestService.updateProfile(mUser.uid, mUser.accesstoken, pin, phonenumber)
                .map(baseResponse -> Boolean.TRUE);
    }

    @Override
    public Observable<ProfilePermission> verifyOTPProfile(String otp) {
        return mRequestService.verifyOTPProfile(mUser.uid, mUser.accesstoken, otp)
                .map(baseResponse -> {
                    ProfilePermission profilePermission = new ProfilePermission();
                    profilePermission.profileLevel = baseResponse.profilelevel;
                    profilePermission.profilePermissions = baseResponse.profilePermissions;
                    return profilePermission;
                })
                .doOnNext(profilePermission -> {
                    mUserConfig.updateProfilePermissions(
                            profilePermission.profileLevel,
                            profilePermission.profilePermissions);
                });
    }

    @Override
    public Observable<ProfilePermission> getUserProfileLevel() {
        return mRequestService.getUserProfileLevel(mUser.uid, mUser.accesstoken)
                .doOnNext(response -> {
                    mUser.profilelevel = response.profilelevel;
                    mUser.profilePermissions = response.profilePermissions;
                    mUser.email = response.email;
                    mUser.identityNumber = response.identityNumber;

                    mUserConfig.updateProfile(
                            response.profilelevel,
                            response.profilePermissions,
                            response.email,
                            response.identityNumber);
                })
                .map(baseResponse -> {
                    ProfilePermission profilePermission = new ProfilePermission();
                    profilePermission.profileLevel = baseResponse.profilelevel;
                    profilePermission.profilePermissions = baseResponse.profilePermissions;
                    return profilePermission;
                });
    }

    @Override
    public Observable<BaseResponse> recoverypin(String pin, String otp) {
        return mRequestService.recoverypin(mUser.uid, mUser.accesstoken, pin, otp);
    }

    @Override
    public Observable<MappingZaloAndZaloPay> getuserinfo(long zaloId, int systemlogin) {
        if (zaloId <= 0) {
            return null;
        }

        return mRequestService.getuserinfo(mUser.uid, mUser.accesstoken, zaloId, systemlogin)
                .map(mappingZaloAndZaloPayResponse -> {
            MappingZaloAndZaloPay mappingZaloAndZaloPay = new MappingZaloAndZaloPay();
            mappingZaloAndZaloPay.setZaloId(zaloId);
            mappingZaloAndZaloPay.setZaloPayId(mappingZaloAndZaloPayResponse.userid);
            mappingZaloAndZaloPay.setPhonenumber(mappingZaloAndZaloPayResponse.phonenumber);
            return mappingZaloAndZaloPay;
        });
    }

    @Override
    public Observable<Boolean> updateUserProfileLevel3(String identityNumber,
                                                       String email,
                                                       String frontImagePath,
                                                       String backImagePath,
                                                       String avatarPath) {

        RequestBody fimg = requestBodyFromFile(frontImagePath);
        RequestBody bimg = requestBodyFromFile(backImagePath);
        RequestBody avatar = requestBodyFromFile(avatarPath);

        return mUploadPhotoService.updateProfile3(
                requestBodyParam(mUser.uid),
                requestBodyParam(mUser.accesstoken),
                requestBodyParam(identityNumber),
                requestBodyParam(email),
                fimg,
                bimg,
                avatar)
                .map(baseResponse -> Boolean.TRUE);
    }

    @Override
    public Observable<Boolean> updateUserProfileLevel3(String identityNumber,
                                                       final String email,
                                                       byte[] frontImage,
                                                       byte[] backImage,
                                                       byte[] avatar) {

        RequestBody frontImageBodyRequest = requestBodyFromData(frontImage);
        RequestBody backImageBodyRequest = requestBodyFromData(backImage);
        RequestBody avatarBodyRequest = requestBodyFromData(avatar);

        return mUploadPhotoService.updateProfile3(
                requestBodyParam(mUser.uid),
                requestBodyParam(mUser.accesstoken),
                requestBodyParam(identityNumber),
                requestBodyParam(email),
                frontImageBodyRequest,
                backImageBodyRequest,
                avatarBodyRequest)
              /*  .doOnNext(baseResponse1 -> {
                    mUser.email = email;
                    mUser.identityNumber = identityNumber;
                })*/
                .map(baseResponse -> Boolean.TRUE);
    }

    private RequestBody requestBodyFromFile(String filePath) {
        File file = new File(filePath);
        return RequestBody.create(MediaType.parse("image/*"), file);
    }

    private RequestBody requestBodyFromData(byte[] data) {
        return RequestBody.create(MediaType.parse("image/*"), data);
    }

    private RequestBody requestBodyParam(String param) {
        return RequestBody.create(MediaType.parse("text/plain"), param);
    }

}
