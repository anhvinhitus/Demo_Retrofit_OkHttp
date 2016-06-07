package vn.com.vng.zalopay.domain.repository;

import rx.Observable;
import vn.com.vng.zalopay.domain.model.ProfilePermisssion;

/**
 * Created by AnhHieu on 4/28/16.
 */
public interface AccountRepository {
    Observable<Boolean> updateProfile(String pin, String phonenumber);
    Observable<ProfilePermisssion> verifyOTPProfile(String otp);
    Observable<Boolean> recoverypin(String pin, String otp);
}
