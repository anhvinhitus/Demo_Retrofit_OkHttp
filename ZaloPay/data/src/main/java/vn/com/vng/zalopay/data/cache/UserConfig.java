package vn.com.vng.zalopay.data.cache;

import org.json.JSONObject;

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

    void saveConfig(LoginResponse response, long zuid);

    void loadConfig();

    void clearConfig();

    String getSession();

    long getUserId();

    long getZaloId();

    void saveUserInfo(long zaloId);

    void saveUserInfo(long zaloId, String avatar, String displayName, long birthData, int userGender);

    void saveZaloUserInfo(JSONObject json);

    String getAvatar();

    String getDisPlayName();

    boolean isSignIn();
}
