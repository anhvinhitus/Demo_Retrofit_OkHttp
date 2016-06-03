package vn.com.vng.zalopay.data.repository;

import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.data.api.AccountService;
import vn.com.vng.zalopay.domain.model.ProfilePermisssion;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.AccountRepository;

/**
 * Created by longlv on 03/06/2016.
 */
public class AccountRepositoryImpl extends BaseRepository implements AccountRepository {

    AccountService accountService;
    private User user;

    public AccountRepositoryImpl(AccountService accountService, User user) {
        this.accountService = accountService;
        this.user = user;
    }

    @Override
    public Observable<Boolean> updateProfile(String pin, String phonenumber) {
        return accountService.updateProfile(user.uid, user.accesstoken, pin, phonenumber).map(baseResponse -> Boolean.TRUE);
    }

    @Override
    public Observable<List<ProfilePermisssion>> verifyOTPProfile(String otp) {
        return accountService.verifyOTPProfile(user.uid, user.accesstoken, otp).map(baseResponse -> baseResponse.profilelevels);
    }
}
