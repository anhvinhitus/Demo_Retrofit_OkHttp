package vn.com.vng.zalopay.domain.repository;

import rx.Observable;
import vn.com.vng.zalopay.domain.model.ProfilePermisssion;
import vn.com.vng.zalopay.domain.model.MappingZaloAndZaloPay;

/**
 * Created by AnhHieu on 4/28/16.
 */
public interface AccountRepository {
    Observable<Boolean> updateProfile(String pin, String phonenumber);
    Observable<ProfilePermisssion> verifyOTPProfile(String otp);
    Observable<Boolean> recoverypin(String pin, String otp);
    Observable<MappingZaloAndZaloPay> getuserinfo(long zaloId, int systemlogin);
}
