package vn.com.vng.zalopay.domain.repository;

import rx.Observable;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by AnhHieu on 3/26/16.
 */
public interface PassportRepository {
    Observable<User> login(String user, String password);

    Observable<User> register(String user, String password);
}
