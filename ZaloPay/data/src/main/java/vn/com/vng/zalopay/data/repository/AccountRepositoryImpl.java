package vn.com.vng.zalopay.data.repository;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import rx.Observable;
import vn.com.vng.zalopay.data.api.AccountService;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.domain.model.ProfilePermission;
import vn.com.vng.zalopay.domain.model.MappingZaloAndZaloPay;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.AccountRepository;

/**
 * Created by longlv on 03/06/2016.
 */
public class AccountRepositoryImpl implements AccountRepository {

    AccountService accountService;
    final User user;
    final UserConfig userConfig;

    public AccountRepositoryImpl(AccountService accountService, UserConfig userConfig, User user) {
        this.accountService = accountService;
        this.user = user;
        this.userConfig = userConfig;
    }

    @Override
    public Observable<Boolean> updateProfile(String pin, String phonenumber) {
        return accountService.updateProfile(user.uid, user.accesstoken, pin, phonenumber).map(baseResponse -> Boolean.TRUE);
    }

    @Override
    public Observable<ProfilePermission> verifyOTPProfile(String otp) {
        return accountService.verifyOTPProfile(user.uid, user.accesstoken, otp)
                .map(baseResponse -> {
                    ProfilePermission profilePermission = new ProfilePermission();
                    profilePermission.profileLevel = baseResponse.profilelevel;
                    profilePermission.profilePermissions = baseResponse.profilePermisssions;
                    return profilePermission;
                })
                .doOnNext(profilePermission -> {
                    userConfig.updateProfilePermissions(profilePermission.profileLevel, profilePermission.profilePermissions);
                });
    }

    @Override
    public Observable<Boolean> recoverypin(String pin, String otp) {
        return accountService.recoverypin(user.uid, user.accesstoken, pin, otp)
                .map(baseResponse -> Boolean.TRUE);
    }

    @Override
    public Observable<MappingZaloAndZaloPay> getuserinfo(long zaloId, int systemlogin) {
        if (zaloId <= 0) {
            return null;
        }

        return accountService.getuserinfo(user.uid, user.accesstoken, zaloId, systemlogin).map(mappingZaloAndZaloPayResponse -> {
            MappingZaloAndZaloPay mappingZaloAndZaloPay = new MappingZaloAndZaloPay();
            mappingZaloAndZaloPay.setZaloId(zaloId);
            mappingZaloAndZaloPay.setZaloPayId(mappingZaloAndZaloPayResponse.userid);
            mappingZaloAndZaloPay.setPhonenumber(mappingZaloAndZaloPayResponse.phonenumber);
            return mappingZaloAndZaloPay;
        });
    }

    @Override
    public Observable<Boolean> updateProfile3(String identityNumber, String email, String fimgPath, String bimgPath, String avatarPath) {

        RequestBody fimg = requestBodyFromPathFile(fimgPath);
        RequestBody bimg = requestBodyFromPathFile(bimgPath);
        RequestBody avatar = requestBodyFromPathFile(avatarPath);

        return accountService.updateProfile3(user.uid, user.accesstoken, identityNumber, email, fimg, bimg, avatar)
                .map(baseResponse -> Boolean.TRUE);
    }

    private RequestBody requestBodyFromPathFile(String filePath) {
        File file = new File(filePath);
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        return requestBody;
    }
}
