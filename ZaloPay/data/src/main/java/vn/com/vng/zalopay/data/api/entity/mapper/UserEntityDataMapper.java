package vn.com.vng.zalopay.data.api.entity.mapper;

import javax.inject.Inject;
import javax.inject.Singleton;

import vn.com.vng.zalopay.data.api.response.LoginResponse;
import vn.com.vng.zalopay.domain.model.User;

@Singleton
public class UserEntityDataMapper {

    @Inject
    public UserEntityDataMapper() {
    }

    public User transform(LoginResponse response) {
        User user = null;
        if (response != null) {
            user = new User();
            user.accesstoken = response.accesstoken;
            user.expirein = response.expirein;
        }

        return user;
    }
}
