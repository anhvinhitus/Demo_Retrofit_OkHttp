package vn.com.vng.zalopay;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.Collection;
import java.util.List;

import de.greenrobot.dao.AbstractDao;
import timber.log.Timber;
import vn.com.vng.zalopay.account.ui.activities.LoginZaloActivity;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.domain.model.ProfilePermisssion;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.interactor.event.ZaloProfileInfoEvent;
import vn.com.vng.zalopay.utils.JsonUtil;

/**
 * Created by AnhHieu on 4/26/16.
 * Implementation of UserConfig
 */
public class UserConfigImpl implements UserConfig {

    private final SharedPreferences preferences;

    private final DaoSession daoSession;
    User currentUser;

    EventBus eventBus;

    private final static Object sync = new Object();

    public UserConfigImpl(DaoSession daoSession, SharedPreferences pref, EventBus eventBus) {
        this.preferences = pref;
        this.eventBus = eventBus;
        this.daoSession = daoSession;
    }


    public boolean hasCurrentUser() {
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
        if (user == null || TextUtils.isEmpty(user.accesstoken)) {
            return;
        }

        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(Constants.PREF_USER_EMAIL, user.email);
        editor.putString(Constants.PREF_USER_SESSION, user.accesstoken);
        editor.putString(Constants.PREF_USER_ID, user.uid);
        editor.putString(Constants.PREF_USER_NAME, user.dname);
        editor.putString(Constants.PREF_USER_AVATAR, user.avatar);
        editor.putInt(Constants.PREF_PROFILELEVEL, user.profilelevel);
        String permissionsStr = JsonUtil.toJsonArrayString(user.profilePermisssions);
        Timber.d("saveProfilePermissions permissions: %s", permissionsStr);
        editor.putString(Constants.PREF_PROFILEPERMISSIONS, permissionsStr);

        editor.apply();

    }

    public void updateProfilePermissions(int profilelevel, List<ProfilePermisssion.Permission> profilePermisssions) {
        if (currentUser == null) {
            return;
        }
        currentUser.profilelevel = profilelevel;
        currentUser.profilePermisssions = profilePermisssions;
        saveProfilePermissions(profilelevel, profilePermisssions);
    }

    private void saveProfilePermissions(int profilelevel, List<ProfilePermisssion.Permission> profilePermisssions) {
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
            currentUser.uid = preferences.getString(Constants.PREF_USER_ID, "");
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
    public void saveUserInfo(long zaloId, String avatar, String displayName, long birthData, int userGender) {

        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(Constants.PREF_ZALO_ID, zaloId);
        editor.putString(Constants.PREF_USER_NAME, displayName);
        editor.putString(Constants.PREF_USER_AVATAR, avatar);
        editor.putLong(Constants.PREF_USER_BIRTHDATE, birthData);
        editor.putInt(Constants.PREF_USER_GENDER, userGender);
//        editor.putLong(Constants.PREF_USER_ID, uid);
        editor.apply();

        Timber.d("save UserInfo hasCurrentUser %s", hasCurrentUser());

        if (hasCurrentUser()) {
            currentUser.avatar = avatar;
            currentUser.dname = displayName;
            currentUser.birthDate = birthData;
            currentUser.userGender = userGender;
            currentUser.zaloId = zaloId;
        }

        Timber.d("save EventBus post ");
        eventBus.postSticky(new ZaloProfileInfoEvent(zaloId, displayName, avatar));
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

    @Override
    public void signOutAndCleanData(Activity activity) {
        clearConfig();
        clearAllUserDB();
        ZaloSDK.Instance.unauthenticate();
        AndroidApplication.instance().releaseUserComponent();
        startLoginActivity(activity, true);
        activity.finish();
    }

    private void startLoginActivity(Activity activity, boolean clearTop) {
        if (activity == null) {
            return;
        }
        Intent intent = new Intent(activity, LoginZaloActivity.class);
        if (clearTop) {
            intent.putExtra("finish", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        }

        activity.startActivity(intent);
    }

    public void clearAllUserDB() {
        Timber.tag("UserConfigFactory").d("clearAllUserDB..............");
        clearConfig();
        setCurrentUser(null);
        clearAllCacheDatabase();
        clearAllDatabase();
    }

    private void clearAllCacheDatabase() {
        daoSession.clear();
    }

    private void clearAllDatabase() {
        Collection<AbstractDao<?, ?>> daoCollection = daoSession.getAllDaos();
        for (AbstractDao<?, ?> dao : daoCollection) {
            if (dao != null) {
                dao.deleteAll();
            }
        }
    }
}
