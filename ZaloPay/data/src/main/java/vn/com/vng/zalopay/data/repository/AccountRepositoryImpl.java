package vn.com.vng.zalopay.data.repository;

import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.data.api.AccountService;
import vn.com.vng.zalopay.domain.model.ProfilePermisssion;
import vn.com.vng.zalopay.domain.repository.AccountRepository;

/**
 * Created by longlv on 03/06/2016.
 */
public class AccountRepositoryImpl extends BaseRepository implements AccountRepository {

    AccountService accountService;

    public AccountRepositoryImpl(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public Observable<Boolean> updateProfile(String pin, String phonenumber) {
        return accountService.updateProfile(pin, phonenumber).map(baseResponse -> Boolean.TRUE);
    }

    @Override
    public Observable<List<ProfilePermisssion>> verifyOTPProfile(String otp) {
        return accountService.verifyOTPProfile(otp).map(baseResponse -> baseResponse.profilelevels);
    }
}
