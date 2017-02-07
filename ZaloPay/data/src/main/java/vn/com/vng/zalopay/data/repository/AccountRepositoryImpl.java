package vn.com.vng.zalopay.data.repository;

import android.support.annotation.Nullable;

import java.io.File;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.NetworkError;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.cache.AccountStore;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.domain.Constants;
import vn.com.vng.zalopay.domain.model.Person;
import vn.com.vng.zalopay.domain.model.ProfileInfo3;
import vn.com.vng.zalopay.domain.model.ProfileLevel2;
import vn.com.vng.zalopay.domain.model.User;

import static vn.com.vng.zalopay.data.util.Utils.sha256Base;

/**
 * Created by longlv on 03/06/2016.
 * Implementation for Account Repository
 */
public class AccountRepositoryImpl implements AccountStore.Repository {
    private final int TIMEOUT_CACHE_OTP_STATE = 5 * 60 * 1000;
    private final AccountStore.RequestService mRequestService;
    private final AccountStore.UploadPhotoService mUploadPhotoService;
    private final AccountStore.LocalStorage mLocalStore;
    private final User mUser;
    private final UserConfig mUserConfig;

    public AccountRepositoryImpl(AccountStore.LocalStorage localStorage,
                                 AccountStore.RequestService accountService,
                                 AccountStore.UploadPhotoService photoService,
                                 UserConfig userConfig,
                                 User user) {
        this.mLocalStore = localStorage;
        this.mRequestService = accountService;
        this.mUploadPhotoService = photoService;
        this.mUser = user;
        this.mUserConfig = userConfig;
        Timber.d("accessToken[%s]", mUser.accesstoken);
    }

    @Override
    public Observable<Boolean> updateUserProfileLevel2(String pin, String phonenumber) {
        pin = sha256Base(pin);
        return mRequestService.updateProfile(mUser.zaloPayId, mUser.accesstoken, pin, phonenumber)
                .map(baseResponse -> Boolean.TRUE);
    }

    @Override
    public Observable<Boolean> verifyOTPProfile(String otp) {
        return mRequestService.verifyOTPProfile(mUser.zaloPayId, mUser.accesstoken, otp)
                .doOnNext(response -> savePermission(response.profilelevel, response.permisstion.toString()))
                .flatMap(response -> clearProfileInfo2())
                .map(response -> Boolean.TRUE)
                ;
    }

    @Override
    public Observable<Boolean> getUserProfileLevelCloud() {
        return mRequestService.getUserProfileLevel(mUser.zaloPayId, mUser.accesstoken)
                .doOnNext(response -> {
                    savePermission(response.profilelevel, response.permisstion.toString());
                    saveUserInfo(response.email, response.identityNumber);
                    saveZalopayName(response.zalopayname);
                }).map(response -> Boolean.TRUE)
                ;
    }

    @Override
    public Observable<Person> getUserInfoByZaloPayName(String zaloPayName) {
        zaloPayName = zaloPayName.toLowerCase();
        Person cachedItem = mLocalStore.get(zaloPayName);
        if (cachedItem != null) {
            return Observable.just(cachedItem);
        } else {
            String finalZaloPayName = zaloPayName;
            return mRequestService.getUserInfoByZaloPayName(zaloPayName, mUser.zaloPayId, mUser.accesstoken)
                    .map(response -> {
                        Person item = new Person(response.userid);
                        item.avatar = response.avatar;
                        item.displayName = response.displayName;
                        item.phonenumber = response.phoneNumber;
                        item.zalopayname = finalZaloPayName;

                        mLocalStore.put(item);
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
    public Observable<Boolean> recoveryPin(String oldPin, String newPin) {
        oldPin = sha256Base(oldPin);
        newPin = sha256Base(newPin);

        return mRequestService.recoverypin(mUser.zaloPayId, mUser.accesstoken, oldPin, newPin, null)
                .flatMap(baseResponse -> saveChangePinState(true))
                .map(baseResponse -> Boolean.TRUE);
    }

    @Override
    public Observable<Boolean> verifyRecoveryPin(String otp) {
        return mRequestService.recoverypin(mUser.zaloPayId, mUser.accesstoken, null, null, otp)
                .flatMap(baseResponse -> resetChangePinState())
                .map(baseResponse -> Boolean.TRUE);
    }

  /*  @Override
    public Observable<MappingZaloAndZaloPay> getUserInfo(long zaloId, int systemLogin) {
        return mRequestService.getuserinfo(mUser.zaloPayId, mUser.accesstoken, zaloId, systemLogin)
                .map(response -> {
                    //If person exist in cache then update cache
                    Person person = mLocalStore.getById(response.userid);
                    if (person != null) {
                        person.zaloId = zaloId;
                        person.phonenumber = response.phonenumber;
                        person.zalopayname = response.zalopayname;
                        mLocalStore.put(person);
                    }

                    MappingZaloAndZaloPay mappingZaloAndZaloPay = new MappingZaloAndZaloPay();
                    mappingZaloAndZaloPay.zaloId = zaloId;
                    mappingZaloAndZaloPay.zaloPayId = response.userid;
                    mappingZaloAndZaloPay.phonenumber = response.phonenumber;
                    mappingZaloAndZaloPay.zaloPayName = response.zalopayname;
                    return mappingZaloAndZaloPay;
                });
    }*/

    @Override
    public Observable<Person> getUserInfoByZaloPayId(String zaloPayId) {
        Person cachedItem = mLocalStore.getById(zaloPayId);
        Timber.d("getUserInfoByZaloPayId, cachedItem [%s]", cachedItem);
        if (cachedItem != null) {
            return Observable.just(cachedItem);
        } else {
            return mRequestService.getUserInfoByZaloPayId(zaloPayId, mUser.zaloPayId, mUser.accesstoken)
                    .map(response -> {
                        Person item = new Person(zaloPayId);
                        item.avatar = response.avatar;
                        item.displayName = response.displayName;
                        item.phonenumber = response.phoneNumber;
                        item.zalopayname = response.zalopayname;

                        mLocalStore.put(item);
                        return item;
                    });
        }
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
                .doOnNext(response -> {
                    mLocalStore.saveProfileInfo3(email, identityNumber, null, null, null);
                    mUserConfig.setWaitingApproveProfileLevel3(true);
                })
                .doOnError(throwable -> {
                    Timber.d("throwable update profile 3");
                    if (throwable instanceof BodyException) {
                        if (((BodyException) throwable).errorCode == NetworkError.WAITING_APPROVE_PROFILE_LEVEL_3) {
                            mLocalStore.saveProfileInfo3(email, identityNumber, null, null, null);
                            mUserConfig.setWaitingApproveProfileLevel3(true);
                        }
                    }
                })
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


    private void savePermission(int profileLevel, String permissions) {
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

    @Override
    public Observable<ProfileInfo3> getProfileInfo3Cache() {
        return ObservableHelper.makeObservable(() -> {
            Map<String, String> map = mLocalStore.getProfileInfo3();
            ProfileInfo3 info = new ProfileInfo3();
            info.email = map.get("email");
            info.identity = map.get("identity");
            info.foregroundImg = map.get("foregroundImg");
            info.backgroundImg = map.get("backgroundImg");
            info.avatarImg = map.get("avatarImg");
            return info;
        });
    }

    @Override
    public Observable<Boolean> saveProfileInfo3(String email, String identity, @Nullable String foregroundImg, @Nullable String backgroundImg, @Nullable String avatarImg) {
        return ObservableHelper.makeObservable(() -> {
            mLocalStore.saveProfileInfo3(email, identity, foregroundImg, backgroundImg, avatarImg);
            return Boolean.TRUE;
        });
    }

    @Override
    public Observable<ProfileLevel2> getProfileLevel2Cache() {
        return ObservableHelper.makeObservable(() -> {
            Map map = mLocalStore.getProfileLevel2();
            Object phoneNumberObj = map.get(Constants.ProfileLevel2.PHONE_NUMBER);
            Object isReceivedOtpObj = map.get(Constants.ProfileLevel2.RECEIVE_OTP);
            Object timeReceivedOtpObj = map.get(Constants.ProfileLevel2.TIME_RECEIVE_OTP);

            ProfileLevel2 profileLevel2 = new ProfileLevel2();
            if (phoneNumberObj != null) {
                profileLevel2.phoneNumber = phoneNumberObj.toString();
            }
            if (isReceivedOtpObj == null || timeReceivedOtpObj == null) {
                return profileLevel2;
            }
            try {
                boolean isReceivedOtp = Boolean.valueOf(isReceivedOtpObj.toString());
                long timeReceiveOtp = Long.valueOf(timeReceivedOtpObj.toString());
                profileLevel2.isReceivedOtp = (isReceivedOtp &&
                        System.currentTimeMillis() - timeReceiveOtp <= TIMEOUT_CACHE_OTP_STATE);
            } catch (NumberFormatException e) {
                return profileLevel2;
            }
            return profileLevel2;
        });
    }

    @Override
    public Observable<Void> saveProfileInfo2(String phoneNumber, boolean receiveOtp) {
        Timber.d("saveProfileInfo2 phone [%s] receiveOtp [%s]",
                phoneNumber, receiveOtp);
        return ObservableHelper.makeObservable(() -> {
            mLocalStore.saveProfileInfo2(phoneNumber, receiveOtp);
            return null;
        });
    }

    @Override
    public Observable<Void> clearProfileInfo2() {
        return ObservableHelper.makeObservable(() -> {
            mLocalStore.saveProfileInfo2("", false);
            return null;
        });
    }

    @Override
    public Observable<Boolean> getChangePinState() {
        return ObservableHelper.makeObservable(() -> {
            Map map = mLocalStore.getChangePinState();
            Object isReceivedOtpObj = map.get(Constants.ChangePin.RECEIVE_OTP_KEY);
            Object timeReceiveOtpObj = map.get(Constants.ChangePin.TIME_RECEIVE_OTP_KEY);
            if (isReceivedOtpObj == null || timeReceiveOtpObj == null) {
                return Boolean.FALSE;
            }
            boolean isReceivedOtp = Boolean.valueOf(isReceivedOtpObj.toString());
            try {
                long timeReceivedOtp = Long.valueOf(timeReceiveOtpObj.toString());
                return (isReceivedOtp &&
                        (System.currentTimeMillis() - timeReceivedOtp <= TIMEOUT_CACHE_OTP_STATE));
            } catch (NumberFormatException e) {
                return Boolean.FALSE;
            }
        });
    }

    @Override
    public Observable<Void> saveChangePinState(boolean receiveOtp) {
        return ObservableHelper.makeObservable(() -> {
            mLocalStore.saveChangePinState(receiveOtp);
            return null;
        });
    }

    @Override
    public Observable<Void> resetChangePinState() {
        return saveChangePinState(false);
    }

    @Override
    public Observable<Boolean> validatePin(String pin) {
        pin = sha256Base(pin);
        return mRequestService.validatePin(pin, mUser.zaloPayId, mUser.accesstoken)
                .map(BaseResponse::isSuccessfulResponse)
                ;
    }
}
