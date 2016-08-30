package vn.com.vng.zalopay.data.cache;

import org.json.JSONObject;

import java.util.List;

import vn.com.vng.zalopay.domain.model.Permission;
import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by AnhHieu on 4/26/16.
 */
public interface UserConfig {

    boolean hasCurrentUser();

    void setCurrentUser(User user);

    User getCurrentUser();

    void saveConfig(User user);

    void savePermission(int profileLevel, List<Permission> profilePermissions);

    void save(String email, String identity);

    void updateUserPhone(String phone);

    void saveZaloPayName(String accountName);

    void loadConfig();

    void clearConfig();

    String getSession();

    String getUserId();

    long getZaloId();

    void saveUserInfo(long zaloId, String avatar, String displayName, long birthData, int userGender);

    void saveZaloUserInfo(JSONObject json);

    String getAvatar();

    String getDisPlayName();

    boolean isSignIn();

    //  void clearAllUserDB();

    /*INVITATION*/
    void saveInvitationInfo(String uid, String session);

    String getSessionInvitation();

    String getUserIdInvitation();

    String getLastUid();

    void setLastUid(String uid);

    void setWaitingApproveProfileLevel3(boolean waitingApproveProfile);

    boolean isWaitingApproveProfileLevel3();
}
