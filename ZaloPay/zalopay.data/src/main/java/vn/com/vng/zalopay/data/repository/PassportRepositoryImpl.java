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
        if (userConfig.hasCurrentUser()) {
            Timber.d("accessToken[%s]", userConfig.getCurrentUser().accesstoken);
        }
    }

    @Override
    public Observable<User> login(final long zuid, String zAuthCode) {
        return passportFactory.login(zuid, zAuthCode)
                .map(this::saveUser);
    }

    @Override
    public Observable<Boolean> logout() {
        return passportFactory.logout().map(logoutResponse -> Boolean.TRUE);
    }

    @Override
    public Observable<User> verifyCode(String code) {
        return passportFactory.verifyInvitationCode(code)
                .map(this::saveUser);
    }

    private User transformWithZaloInfo(LoginResponse response) {
        User user = userEntityDataMapper.transform(response);
        user.displayName = userConfig.getDisPlayName();
        user.avatar = userConfig.getAvatar();
        user.zaloId = userConfig.getZaloId();
        Timber.d("displayName %s avatar %s zaloid %s", user.displayName, user.avatar, user.zaloId);
        return user;
    }

    private User saveUser(LoginResponse response) {
        User user = transformWithZaloInfo(response);
        userConfig.setCurrentUser(user);
        userConfig.saveConfig(user);
        userConfig.updateZaloPayName(user.zalopayname);
        return user;
    }

}
