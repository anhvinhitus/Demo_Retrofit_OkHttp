package vn.com.vng.zalopay.data.repository;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import vn.com.vng.zalopay.data.repository.datasource.PassportFactory;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.PassportRepository;

/**
 * Created by AnhHieu on 3/26/16.
 */

@Singleton
public class PassportRepositoryImpl implements PassportRepository {


   /* private PassportFactory passportFactory;
   private UserEntityDataMapper userEntityDataMapper;

    @Inject
    public PassportRepositoryImpl(PassportFactory passportFactory, UserEntityDataMapper userEntityDataMapper) {
        this.passportFactory = passportFactory;
        this.userEntityDataMapper = userEntityDataMapper;
    }

    @Override
    public Observable<User> login(String user, String password) {
        return passportFactory.loginByEmail(user, password)
                .map(userEntity -> userEntityDataMapper.transform(userEntity));
    }

    @Override
    public Observable<User> register(String user, String password) {
        return passportFactory.registerByEmail(user, password).map(userEntity -> userEntityDataMapper.transform(userEntity));
    }*/

    @Override
    public Observable<User> login(String user, String password) {
        return null;
    }

    @Override
    public Observable<User> register(String user, String password) {
        return null;
    }
}
