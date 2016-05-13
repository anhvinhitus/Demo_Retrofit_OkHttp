package vn.com.vng.zalopay.data.repository;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import vn.com.vng.zalopay.data.api.entity.mapper.UserEntityDataMapper;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.repository.datasource.PassportFactory;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.PassportRepository;

/**
 * Created by AnhHieu on 3/26/16.
 */

@Singleton
public class PassportRepositoryImpl implements PassportRepository {

    private PassportFactory passportFactory;
    private UserEntityDataMapper userEntityDataMapper;

    private UserConfig userConfig;

    @Inject
    public PassportRepositoryImpl(PassportFactory passportFactory, UserEntityDataMapper userEntityDataMapper, UserConfig userConfig) {
        this.passportFactory = passportFactory;
        this.userEntityDataMapper = userEntityDataMapper;
        this.userConfig = userConfig;
    }

    @Override
    public Observable<User> login() {
        return passportFactory.login()
                .map(userEntity -> userEntityDataMapper.transform(userEntity));
    }

    @Override
    public Observable<User> login(final long zuid, String zAuthCode) {
        return passportFactory.login(zuid, zAuthCode).map(userEntity -> {
            User user = userEntityDataMapper.transform(userEntity, zuid);
            user.dname = userConfig.getDisPlayName();
            user.avatar = userConfig.getAvatar();
            return user;
        });
    }

    @Override
    public Observable<Boolean> logout() {
        return passportFactory.logout().map(logoutResponse -> Boolean.TRUE);
    }

    @Override
    public Observable<Boolean> verifyAccessToken(long userId, String token) {
        return passportFactory.verifyAccessToken(userId, token).map(baseResponse -> Boolean.TRUE);
    }
}
