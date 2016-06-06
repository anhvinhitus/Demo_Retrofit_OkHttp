package vn.com.vng.zalopay.data.repository;

import rx.Observable;
import vn.com.vng.zalopay.data.api.AccountService;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.domain.model.ProfilePermisssion;
import vn.com.vng.zalopay.domain.repository.AccountRepository;

/**
 * Created by longlv on 03/06/2016.
 */
public class AccountRepositoryImpl extends BaseRepository implements AccountRepository {

    AccountService accountService;
    private UserConfig userConfig;

    public AccountRepositoryImpl(AccountService accountService, UserConfig userConfig) {
        this.accountService = accountService;
        this.userConfig = userConfig;
    }

    @Override
    public Observable<Boolean> updateProfile(String pin, String phonenumber) {
        long uid = -1;
        String accesstoken = "";
        if (userConfig.getCurrentUser() != null) {
            uid = userConfig.getCurrentUser().uid;
            accesstoken = userConfig.getCurrentUser().accesstoken;
        }
        return accountService.updateProfile(uid, accesstoken, pin, phonenumber).map(baseResponse -> Boolean.TRUE);
    }

    @Override
    public Observable<ProfilePermisssion> verifyOTPProfile(String otp) {
        long uid = -1;
        String accesstoken = "";
        if (userConfig.getCurrentUser() != null) {
            uid = userConfig.getCurrentUser().uid;
            accesstoken = userConfig.getCurrentUser().accesstoken;
        }
        return accountService.verifyOTPProfile(uid, accesstoken, otp)
                .map(baseResponse -> {
                    ProfilePermisssion profilePermisssion = new ProfilePermisssion();
                    profilePermisssion.profileLevel = baseResponse.profilelevel;
                    profilePermisssion.profilePermisssions = baseResponse.profilePermisssions;
                    return profilePermisssion;
                })
                .doOnNext(profilePermisssion -> {
                    userConfig.saveProfilePermissions(profilePermisssion.profileLevel, profilePermisssion.profilePermisssions);
                });
    }
}
