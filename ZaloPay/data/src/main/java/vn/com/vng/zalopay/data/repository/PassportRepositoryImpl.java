package vn.com.vng.zalopay.data.repository;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.entity.mapper.UserEntityDataMapper;
import vn.com.vng.zalopay.data.api.response.LoginResponse;
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
                .map(response -> saveUser(response))
                ;
    }

    @Override
    public Observable<Boolean> logout() {
        return passportFactory.logout().map(logoutResponse -> Boolean.TRUE);
    }

    @Override
    public Observable<User> verifyCode(String code) {
        return passportFactory.verifyInvitationCode(code)
                .map(response -> saveUser(response))
                ;
    }

    private User transformWithZaloInfo(LoginResponse response) {
        User user = userEntityDataMapper.transform(response);
        user.dname = userConfig.getDisPlayName();
        user.avatar = userConfig.getAvatar();
        user.zaloId = userConfig.getZaloId();
        Timber.d("dname %s avatar %s zaloid %s", user.dname, user.avatar, user.zaloId);
        return user;
    }

    private User saveUser(LoginResponse response) {
        User user = transformWithZaloInfo(response);
        userConfig.setCurrentUser(user);
        userConfig.saveConfig(user);
        userConfig.saveZaloPayName(user.zalopayname);
        return user;
    }

}
