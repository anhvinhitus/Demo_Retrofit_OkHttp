package vn.com.vng.zalopay;

import android.content.SharedPreferences;
import android.text.TextUtils;

import javax.inject.Inject;
import javax.inject.Singleton;

import vn.com.vng.zalopay.domain.model.User;

/**
 * Created by AnhHieu on 3/27/16.
 */

@Singleton
public class UserConfig {

    private final SharedPreferences preferences;

    User currentUser;

    private final static Object sync = new Object();

    @Inject
    public UserConfig(SharedPreferences pref) {
        this.preferences = pref;
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
        if (user == null || TextUtils.isEmpty(user.session)) return;

        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(Constants.PREF_USER_EMAIL, user.email);
        editor.putString(Constants.PREF_USER_SESSION, user.session);
        editor.putLong(Constants.PREF_USER_ID, user.uid);
        editor.putString(Constants.PREF_USER_NAME, user.dname);
        editor.putString(Constants.PREF_USER_AVATAR, user.avatar);

        editor.apply();

    }

    public void loadConfig() {
        if (preferences.contains(Constants.PREF_USER_SESSION)) {
            String session = preferences.getString(Constants.PREF_USER_SESSION, "");
            long uid = preferences.getLong(Constants.PREF_USER_ID, 0);
            if (TextUtils.isEmpty(session) || uid <= 0) return;


            currentUser = new User(uid);
            currentUser.session = session;
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
}
