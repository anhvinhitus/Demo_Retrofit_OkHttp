package vn.com.vng.zalopay.data.repository;

import android.text.TextUtils;

import java.io.File;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import rx.Observable;
import vn.com.vng.zalopay.data.api.entity.mapper.UserEntityDataMapper;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.domain.model.MappingZaloAndZaloPay;
import vn.com.vng.zalopay.domain.model.Permission;
import vn.com.vng.zalopay.domain.model.Person;
import vn.com.vng.zalopay.domain.model.User;

import static vn.com.vng.zalopay.data.util.Utils.*;

/**
 * Created by longlv on 03/06/2016.
 * Implementation for Account Repository
 */
public class AccountRepositoryImpl implements AccountStore.Repository {

    private final AccountStore.RequestService mRequestService;
    private final AccountStore.UploadPhotoService mUploadPhotoService;
    private final AccountStore.LocalStorage localStorage;
    private final User mUser;
    private final UserConfig mUserConfig;

    private UserEntityDataMapper userEntityDataMapper;

    public AccountRepositoryImpl(AccountStore.LocalStorage localStorage,
                                 AccountStore.RequestService accountService,
                                 AccountStore.UploadPhotoService photoService,
                                 UserConfig userConfig,
                                 User user, UserEntityDataMapper userEntityDataMapper) {
        this.localStorage = localStorage;
        this.mRequestService = accountService;
        this.mUploadPhotoService = photoService;
        this.mUser = user;
        this.mUserConfig = userConfig;
        this.userEntityDataMapper = userEntityDataMapper;
    }

    @Override
    public Observable<Boolean> updateUserProfileLevel2(String pin, String phonenumber, String zalopayName) {
        if (!TextUtils.isEmpty(zalopayName)) {
            zalopayName = zalopayName.toLowerCase();
        }
        pin = sha256Base(pin);
        return mRequestService.updateProfile(mUser.zaloPayId, mUser.accesstoken, pin, phonenumber, zalopayName)
                .map(baseResponse -> Boolean.TRUE);
    }

    @Override
    public Observable<Boolean> verifyOTPProfile(String otp) {
        return mRequestService.verifyOTPProfile(mUser.zaloPayId, mUser.accesstoken, otp)
                .doOnNext(response -> savePermission(response.profilelevel, userEntityDataMapper.transform(response.permisstion)))
                .map(response -> Boolean.TRUE)
                ;
    }

    @Override
    public Observable<Boolean> getUserProfileLevelCloud() {
        return mRequestService.getUserProfileLevel(mUser.zaloPayId, mUser.accesstoken)
                .doOnNext(response -> {
                    savePermission(response.profilelevel, userEntityDataMapper.transform(response.permisstion));
                    saveUserInfo(response.email, response.identityNumber);
                    saveZalopayName(response.zalopayname);
                }).map(response -> Boolean.TRUE)
                ;
    }

    @Override
    public Observable<Person> getUserInfoByZaloPayName(String zaloPayName) {
        zaloPayName = zaloPayName.toLowerCase();
        Person cachedItem = localStorage.get(zaloPayName);
        if (cachedItem != null) {
            return Observable.just(cachedItem);
        } else {
            String finalZaloPayName = zaloPayName;
            return mRequestService.getUserInfoByZaloPayName(zaloPayName, mUser.zaloPayId, mUser.accesstoken)
                    .map(response -> {
                        Person item = new Person();
                        item.zaloPayId = response.userid;
                        item.avatar = response.avatar;
                        item.displayName = response.displayName;
                        item.phonenumber = response.phoneNumber;
                        item.zalopayname = finalZaloPayName;

                        localStorage.put(item);
                        return item;
                    });
        }
    }

    @Override
    public Observable<Boolean> checkZaloPayNameExist(String zaloPayName) {
        zaloPayName = zaloPayName.toLowerCase();
        return mRequestService.checkZaloPayNameExist(zaloPayName, mUser.zaloPayId, mUser.accesstoken)
                .map(BaseResponse::isSuccessfulResponse);
    }

    @Override
    public Observable<BaseResponse> recoveryPin(String oldPin, String newPin) {
        oldPin = sha256Base(oldPin);
        newPin = sha256Base(newPin);

        return mRequestService.recoverypin(mUser.zaloPayId, mUser.accesstoken, oldPin, newPin, null);
    }

    @Override
    public Observable<BaseResponse> verifyRecoveryPin(String otp) {
        return mRequestService.recoverypin(mUser.zaloPayId, mUser.accesstoken, null, null, otp);
    }

    @Override
    public Observable<MappingZaloAndZaloPay> getUserInfo(long zaloId, int systemLogin) {
        return mRequestService.getuserinfo(mUser.zaloPayId, mUser.accesstoken, zaloId, systemLogin)
                .map(mappingZaloAndZaloPayResponse -> {
                    MappingZaloAndZaloPay mappingZaloAndZaloPay = new MappingZaloAndZaloPay();
                    mappingZaloAndZaloPay.setZaloId(zaloId);
                    mappingZaloAndZaloPay.setZaloPayId(mappingZaloAndZaloPayResponse.userid);
                    mappingZaloAndZaloPay.setPhonenumber(mappingZaloAndZaloPayResponse.phonenumber);
                    return mappingZaloAndZaloPay;
                });
    }

    @Override
    public Observable<Person> getUserInfoByZaloPayId(String zaloPayId) {
        Person cachedItem = localStorage.getById(zaloPayId);
        if (cachedItem != null) {
            return Observable.just(cachedItem);
        } else {
            return mRequestService.getUserInfoByZaloPayId(zaloPayId, mUser.zaloPayId, mUser.accesstoken)
                    .map(response -> {
                        Person item = new Person();
                        item.zaloPayId = zaloPayId;
                        item.avatar = response.avatar;
                        item.displayName = response.displayName;
                        item.phonenumber = response.phoneNumber;
                        item.zalopayname = response.zalopayname;

                        localStorage.put(item);
                        return item;
                    });
        }
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
                requestBodyParam(mUser.zaloPayId),
                requestBodyParam(mUser.accesstoken),
                requestBodyParam(identityNumber),
                requestBodyParam(email),
                fimg,
                bimg,
                avatar)
                .map(baseResponse -> Boolean.TRUE);
    }

    @Override
    public Observable<Boolean> updateZaloPayName(String zaloPayName) {
        return mRequestService.updateZaloPayName(zaloPayName, mUser.zaloPayId, mUser.accesstoken)
                .doOnNext(response -> saveZalopayName(zaloPayName))
                .map(BaseResponse::isSuccessfulResponse);
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
                requestBodyParam(mUser.zaloPayId),
                requestBodyParam(mUser.accesstoken),
                requestBodyParam(identityNumber),
                requestBodyParam(email),
                frontImageBodyRequest,
                backImageBodyRequest,
                avatarBodyRequest)
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


    private void savePermission(int profileLevel, List<Permission> permissions) {
        mUser.profilelevel = profileLevel;
        mUser.profilePermissions = permissions;
        mUserConfig.savePermission(profileLevel, permissions);
    }

    private void saveUserInfo(String email, String identityNumber) {
        mUser.email = email;
        mUser.identityNumber = identityNumber;
        mUserConfig.save(email, identityNumber);
    }

    private void saveZalopayName(String accountName) {
        mUser.zalopayname = accountName;
        mUserConfig.updateZaloPayName(accountName);
    }

}
