package vn.com.vng.zalopay.domain.repository;

import rx.Observable;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by AnhHieu on 3/26/16.
 * Phần login, logout của ứng dụng.
 */
public interface PassportRepository {

    Observable<User> login(long zuid, String zAuthCode);

    Observable<Boolean> logout(String uid, String token);

    Observable<User> verifyCode(String code);
}
