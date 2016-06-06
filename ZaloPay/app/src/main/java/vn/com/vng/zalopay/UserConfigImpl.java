package vn.com.vng.zalopay;

import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.data.api.entity.UserEntity;
import vn.com.vng.zalopay.data.api.response.LoginResponse;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.domain.model.ProfilePermisssion;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.interactor.event.ZaloProfileInfoEvent;
import vn.com.vng.zalopay.utils.JsonUtil;

/**
 * Created by AnhHieu on 4/26/16.
 */
public class UserConfigImpl implements UserConfig {

    private final SharedPreferences preferences;

    User currentUser;

    EventBus eventBus;

    private final static Object sync = new Object();

    public UserConfigImpl(SharedPreferences pref, EventBus eventBus) {
        this.preferences = pref;
        this.eventBus = eventBus;
    }


    public boolean isClientActivated() {
        synchronized (sync) {
            return currentUser != null;
        }
    }

    public void setCurrentUser(User user) {
        synchronized (sync) {
            currentUser = user;
        }
    }

    public User getCurrentUser() {
        synchronized (sync) {
            return currentUser;
        }
    }

    public void saveConfig(User user) {
        if (user == null || TextUtils.isEmpty(user.accesstoken)) return;

        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(Constants.PREF_USER_EMAIL, user.email);
        editor.putString(Constants.PREF_USER_SESSION, user.accesstoken);
        editor.putLong(Constants.PREF_USER_ID, user.uid);
        editor.putString(Constants.PREF_USER_NAME, user.dname);
        editor.putString(Constants.PREF_USER_AVATAR, user.avatar);
        editor.putInt(Constants.PREF_PROFILELEVEL, user.profilelevel);
        String permissionsStr = JsonUtil.toJsonArrayString(user.profilePermisssions);
        Timber.d("saveProfilePermissions permissions: %s", permissionsStr);
        editor.putString(Constants.PREF_PROFILEPERMISSIONS, permissionsStr);

        editor.apply();

    }

    public void saveProfilePermissions(int profilelevel, List<ProfilePermisssion.Permission> profilePermisssions) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(Constants.PREF_PROFILELEVEL, profilelevel);
        Gson gson = new Gson();
        String permissionsStr = JsonUtil.toJsonArrayString(profilePermisssions);
        Timber.d("saveProfilePermissions permissions: %s", permissionsStr);
        editor.putString(Constants.PREF_PROFILEPERMISSIONS, permissionsStr);
        editor.apply();
    }

    public void loadConfig() {
        if (preferences.contains(Constants.PREF_USER_SESSION)) {
            String session = preferences.getString(Constants.PREF_USER_SESSION, "");
            //   long uid = preferences.getLong(Constants.PREF_USER_ID, 0);
            if (TextUtils.isEmpty(session)) return;


            currentUser = new User();
            currentUser.accesstoken = session;
            currentUser.expirein = preferences.getLong(Constants.PREF_USER_EXPIREIN, -1);
            currentUser.uid = preferences.getLong(Constants.PREF_USER_ID, -1);
            currentUser.zaloId = preferences.getLong(Constants.PREF_ZALO_ID, -1);
            currentUser.email = preferences.getString(Constants.PREF_USER_EMAIL, "");
            currentUser.dname = preferences.getString(Constants.PREF_USER_NAME, "");
            currentUser.avatar = preferences.getString(Constants.PREF_USER_AVATAR, "");
            currentUser.birthDate = preferences.getLong(Constants.PREF_USER_BIRTHDATE, 0);
            currentUser.profilelevel = preferences.getInt(Constants.PREF_PROFILELEVEL, 0);
            currentUser.setPermissions(preferences.getString(Constants.PREF_PROFILEPERMISSIONS, ""));
        }
    }

    public void clearConfig() {
        SharedPreferences.Editor editor = preferences.edit();

        editor.remove(Constants.PREF_USER_SESSION);
        editor.remove(Constants.PREF_USER_AVATAR);
        editor.remove(Constants.PREF_USER_EMAIL);
        editor.remove(Constants.PREF_USER_NAME);
        editor.remove(Constants.PREF_USER_ID);
        editor.remove(Constants.PREF_ZALO_ID);

        editor.apply();
    }

    @Override
    public void saveConfig(UserEntity user) {
        //empty
    }

    @Override
    public void saveConfig(LoginResponse response) {
        Timber.tag("UserConfig").d("saveConfig.............");
        if (response == null || !response.isSuccessfulResponse()) return;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.PREF_USER_SESSION, response.accesstoken);
        editor.putLong(Constants.PREF_USER_EXPIREIN, response.expirein);

        editor.apply();
    }

    @Override
    public void saveConfig(LoginResponse response, long zuid) {
        Timber.tag("UserConfig").d("saveConfig.............zuid:" + zuid);
        if (response == null || !response.isSuccessfulResponse()) return;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.PREF_USER_SESSION, response.accesstoken);
        editor.putLong(Constants.PREF_USER_EXPIREIN, response.expirein);
        editor.putLong(Constants.PREF_USER_ID, zuid);
        editor.apply();
    }


    @Override
    public long getUserId() {
        if (isClientActivated()) {
            return getCurrentUser().uid;
        }
        return -1;
    }

    @Override
    public void saveUserInfo(long zaloId) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(Constants.PREF_ZALO_ID, zaloId);
        editor.apply();

        Timber.d("save UserInfo isClientActivated %s", isClientActivated());

        if (isClientActivated()) {
            currentUser.zaloId = zaloId;
        }
    }

    @Override
    public void saveUserInfo(long zaloId, String avatar, String displayName, long birthData, int userGender) {

        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(Constants.PREF_ZALO_ID, zaloId);
        editor.putString(Constants.PREF_USER_NAME, displayName);
        editor.putString(Constants.PREF_USER_AVATAR, avatar);
        editor.putLong(Constants.PREF_USER_BIRTHDATE, birthData);
        editor.putInt(Constants.PREF_USER_GENDER, userGender);
//        editor.putLong(Constants.PREF_USER_ID, uid);
        editor.apply();

        Timber.d("save UserInfo isClientActivated %s", isClientActivated());

        if (isClientActivated()) {
            currentUser.avatar = avatar;
            currentUser.dname = displayName;
            currentUser.birthDate = birthData;
            currentUser.userGender = userGender;
            currentUser.zaloId = zaloId;
        }

        Timber.d("save EventBus post ");
        eventBus.post(new ZaloProfileInfoEvent(zaloId, displayName, avatar));
    }


    @Override
    public void saveZaloUserInfo(JSONObject profile) {
        JSONObject json = profile.optJSONObject("result");

        if (json == null) return;
        long userId = json.optInt("userId");
        String displayName = json.optString("displayName");
        String avatar = json.optString("largeAvatar");
        long birthday = json.optInt("birthDate");
        int userGender = json.optInt("userGender");

        saveUserInfo(userId, avatar, displayName, birthday, userGender);
    }

    @Override
    public long getZaloId() {
        return preferences.getLong(Constants.PREF_ZALO_ID, -1);
    }

    @Override
    public boolean isSignIn() {
        return !TextUtils.isEmpty(getSession());
    }


    @Override
    public String getSession() {
        return preferences.getString(Constants.PREF_USER_SESSION, "");
    }

    @Override
    public String getAvatar() {
        return preferences.getString(Constants.PREF_USER_AVATAR, "");
    }

    @Override
    public String getDisPlayName() {
        return preferences.getString(Constants.PREF_USER_NAME, "");
    }


}
