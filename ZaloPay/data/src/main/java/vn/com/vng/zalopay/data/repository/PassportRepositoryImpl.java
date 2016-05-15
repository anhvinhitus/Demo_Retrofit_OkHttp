package vn.com.vng.zalopay.data.repository;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import timber.log.Timber;
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
    public Observable<User> login(final long zuid, String zAuthCode) {
        return passportFactory.login(zuid, zAuthCode)
                .map(userEntity -> {
                    User user = userEntityDataMapper.transform(userEntity);
                    user.dname = userConfig.getDisPlayName();
                    user.avatar = userConfig.getAvatar();
                    return user;
                }).doOnNext(user -> {

                            Timber.d("save User");
                            userConfig.setCurrentUser(user);
                            userConfig.saveConfig(user);
                        }
                );
    }

    @Override
    public Observable<Boolean> logout(long uid, String token) {
        return passportFactory.logout(uid, token).map(logoutResponse -> Boolean.TRUE);
    }

    @Override
    public Observable<Boolean> verifyAccessToken(long userId, String token) {
        return passportFactory.verifyAccessToken(userId, token).map(baseResponse -> Boolean.TRUE);
    }
}
