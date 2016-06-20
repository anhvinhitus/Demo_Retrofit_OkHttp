package vn.com.vng.zalopay.data.cache;

import android.app.Activity;

import org.json.JSONObject;

import java.util.List;

import vn.com.vng.zalopay.data.api.entity.UserEntity;
import vn.com.vng.zalopay.domain.model.ProfilePermisssion;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by AnhHieu on 4/26/16.
 */
public interface UserConfig {

    boolean hasCurrentUser();

    void setCurrentUser(User user);

    User getCurrentUser();

    void saveConfig(User user);

    void updateProfilePermissions(int profileLevel, List<ProfilePermisssion.Permission> profilePermisssions);

    void loadConfig();

    void clearConfig();

    String getSession();

    long getZaloId();

    void saveUserInfo(long zaloId, String avatar, String displayName, long birthData, int userGender);

    void saveZaloUserInfo(JSONObject json);

    String getAvatar();

    String getDisPlayName();

    boolean isSignIn();

    void clearAllUserDB();

    void signOutAndCleanData(Activity activity);
}
