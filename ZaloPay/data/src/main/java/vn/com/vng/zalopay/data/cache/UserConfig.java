package vn.com.vng.zalopay.data.cache;

import vn.com.vng.zalopay.data.api.entity.UserEntity;
import vn.com.vng.zalopay.data.api.response.LoginResponse;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by AnhHieu on 4/26/16.
 */
public interface UserConfig {

    boolean isClientActivated();

    void setCurrentUser(User user);

    User getCurrentUser();

    void saveConfig(User user);

    void saveConfig(UserEntity user);

    void saveConfig(LoginResponse response);

    void loadConfig();

    void clearConfig();

    String getSession();
}
