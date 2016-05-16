package vn.com.vng.zalopay;

import android.content.SharedPreferences;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import timber.log.Timber;
import vn.com.vng.zalopay.data.api.entity.UserEntity;
import vn.com.vng.zalopay.data.api.response.LoginResponse;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.interactor.event.ZaloProfileInfoEvent;

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
            currentUser.email = preferences.getString(Constants.PREF_USER_EMAIL, "");
            currentUser.dname = preferences.getString(Constants.PREF_USER_NAME, "");
            currentUser.avatar = preferences.getString(Constants.PREF_USER_AVATAR, "");
        }
    }

    public void clearConfig() {
        SharedPreferences.Editor editor = preferences.edit();

        editor.remove(Constants.PREF_USER_SESSION);
        editor.remove(Constants.PREF_USER_AVATAR);
        editor.remove(Constants.PREF_USER_EMAIL);
        editor.remove(Constants.PREF_USER_NAME);
        editor.remove(Constants.PREF_USER_ID);

        editor.apply();
    }

    @Override
    public void saveConfig(UserEntity user) {
        //empty
    }

    @Override
    public void saveConfig(LoginResponse response) {
        Timber.tag("##########################@@@@@@@@").d("saveConfig.............");
        if (response == null || !response.isSuccessfulResponse()) return;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.PREF_USER_SESSION, response.accesstoken);
        editor.putLong(Constants.PREF_USER_EXPIREIN, response.expirein);

        editor.apply();
    }

    @Override
    public void saveConfig(LoginResponse response, long zuid) {
        Timber.tag("##########################@@@@@@@@").d("saveConfig.............zuid:" + zuid);
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
    public void saveUserInfo(long uid, String avatar, String displayName) {

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.PREF_USER_NAME, displayName);
        editor.putString(Constants.PREF_USER_AVATAR, avatar);
        editor.putLong(Constants.PREF_USER_ID, uid);
        editor.apply();

        Timber.d("save UserInfo isClientActivated %s", isClientActivated());

        if (isClientActivated()) {
            currentUser.avatar = avatar;
            currentUser.dname = displayName;
            Timber.d("save EventBus post ");
            eventBus.post(new ZaloProfileInfoEvent(uid, displayName, avatar));
        }
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
