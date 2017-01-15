package vn.com.vng.zalopay;

import android.content.SharedPreferences;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.event.ZaloPayNameEvent;
import vn.com.vng.zalopay.event.ZaloProfileInfoEvent;

/**
 * Created by AnhHieu on 4/26/16.
 * Implementation of UserConfig
 */
public class UserConfigImpl implements UserConfig {

    private final SharedPreferences preferences;

    private User currentUser;

    private EventBus eventBus;

    private final static Object sync = new Object();

    public UserConfigImpl(SharedPreferences pref, EventBus eventBus) {
        this.preferences = pref;
        this.eventBus = eventBus;
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
        editor.putString(Constants.PREF_USER_ID, user.zaloPayId);
        editor.putString(Constants.PREF_USER_NAME, user.displayName);
        editor.putString(Constants.PREF_USER_AVATAR, user.avatar);
        editor.putInt(Constants.PREF_PROFILE_LEVEL, user.profilelevel);
        editor.putLong(Constants.PREF_USER_PHONE, user.phonenumber);
        editor.putString(Constants.PREF_PROFILE_PERMISSIONS, user.profilePermissions);

        editor.apply();

    }

    @Override
    public void updateUserPhone(String phone) {
        if (currentUser == null) {
            return;
        }
        try {
            long phoneNumber = Long.valueOf(phone);
            currentUser.phonenumber = phoneNumber;
            saveUserPhone(phoneNumber);
        } catch (NumberFormatException e) {
            Timber.e(e, "NumberFormatException phone: %s", phone);
        }
    }

    private void saveUserPhone(long phone) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(Constants.PREF_USER_PHONE, phone);
        editor.apply();
    }

    @Override
    public void savePermission(int profileLevel, String profilePermissions) {
        if (currentUser != null) {
            currentUser.profilelevel = profileLevel;
            currentUser.profilePermissions = profilePermissions;
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(Constants.PREF_PROFILE_LEVEL, profileLevel);
        Timber.d("saveProfilePermissions permissions: %s", profilePermissions);
        editor.putString(Constants.PREF_PROFILE_PERMISSIONS, profilePermissions);
        editor.apply();
    }

    @Override
    public void save(String email, String identity) {
        if (currentUser != null) {
            currentUser.email = email;
            currentUser.identityNumber = identity;

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Constants.PREF_USER_IDENTITY_NUMBER, identity);
            editor.putString(Constants.PREF_USER_EMAIL, email);
            editor.apply();
        }
    }

    @Override
    public void setAccessToken(String accessToken) {

        if (currentUser != null) {
            currentUser.accesstoken = accessToken;
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.PREF_USER_SESSION, accessToken);
        editor.apply();
    }

    public void loadConfig() {
        if (preferences.contains(Constants.PREF_USER_SESSION)) {
            String session = preferences.getString(Constants.PREF_USER_SESSION, "");
            if (TextUtils.isEmpty(session)) return;

            currentUser = new User(preferences.getString(Constants.PREF_USER_ID, ""));
            currentUser.accesstoken = session;
            currentUser.expirein = preferences.getLong(Constants.PREF_USER_EXPIREIN, -1);
            currentUser.zaloId = preferences.getLong(Constants.PREF_ZALO_ID, -1);
            currentUser.email = preferences.getString(Constants.PREF_USER_EMAIL, "");
            currentUser.displayName = preferences.getString(Constants.PREF_USER_NAME, "");
            currentUser.avatar = preferences.getString(Constants.PREF_USER_AVATAR, "");
            currentUser.birthDate = preferences.getLong(Constants.PREF_USER_BIRTHDAY, 0);
            currentUser.profilelevel = preferences.getInt(Constants.PREF_PROFILE_LEVEL, 0);
            currentUser.phonenumber = preferences.getLong(Constants.PREF_USER_PHONE, 0L);
            currentUser.profilePermissions = preferences.getString(Constants.PREF_PROFILE_PERMISSIONS, "");
            currentUser.identityNumber = preferences.getString(Constants.PREF_USER_IDENTITY_NUMBER, "");
            currentUser.zalopayname = preferences.getString(Constants.PREF_USER_ZALOPAY_NAME, "");
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
        editor.remove(Constants.PREF_USER_PHONE);
        editor.remove(Constants.PREF_PROFILE_LEVEL);
        editor.remove(Constants.PREF_USER_BIRTHDAY);
        editor.remove(Constants.PREF_PROFILE_PERMISSIONS);
        editor.remove(Constants.PREF_INVITATION_SESSION);
        editor.remove(Constants.PREF_INVITATION_USERID);
        editor.remove(Constants.PREF_USER_IDENTITY_NUMBER);
        editor.remove(Constants.PREF_USER_ZALOPAY_NAME);
        editor.remove(Constants.PREF_WAITING_APPROVE_PROFILE_LEVEL3);
        editor.apply();
    }

    @Override
    public void saveUserInfo(long zaloId, String avatar, String displayName, long birthData, int userGender) {

        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(Constants.PREF_ZALO_ID, zaloId);
        editor.putString(Constants.PREF_USER_NAME, displayName);
        editor.putString(Constants.PREF_USER_AVATAR, avatar);
        editor.putLong(Constants.PREF_USER_BIRTHDAY, birthData);
        editor.putInt(Constants.PREF_USER_GENDER, userGender);
//        editor.putLong(Constants.PREF_USER_ID, zaloPayId);
        editor.apply();

        Timber.d("save UserInfo hasCurrentUser %s", hasCurrentUser());

        if (hasCurrentUser()) {
            currentUser.avatar = avatar;
            currentUser.displayName = displayName;
            currentUser.birthDate = birthData;
            currentUser.userGender = userGender;
            currentUser.zaloId = zaloId;
        }

        Timber.d("save EventBus post ");
        eventBus.postSticky(new ZaloProfileInfoEvent(zaloId, displayName, avatar, birthData, userGender));
    }


    @Override
    public void saveZaloUserInfo(JSONObject profile) {
        Timber.i("Zalo User Info: %s", profile);
        JSONObject json = profile.optJSONObject("result");

        if (json == null) {
            return;
        }

        long zaloId = json.optLong("userId");
        String displayName = json.optString("displayName");
        String avatar = json.optString("largeAvatar");
        long birthday = json.optLong("birthDate");
        int userGender = json.optInt("userGender");

        saveUserInfo(zaloId, avatar, displayName, birthday, userGender);
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
    public String getUserId() {
        return preferences.getString(Constants.PREF_USER_ID, "");
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
    public void saveInvitationInfo(String uid, String session) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.PREF_INVITATION_USERID, uid);
        editor.putString(Constants.PREF_INVITATION_SESSION, session);
        editor.apply();
    }

    @Override
    public String getSessionInvitation() {
        return preferences.getString(Constants.PREF_INVITATION_SESSION, "");
    }

    @Override
    public String getUserIdInvitation() {
        return preferences.getString(Constants.PREF_INVITATION_USERID, "");
    }

    @Override
    public String getLastUid() {
        return preferences.getString(Constants.PREF_USER_LAST_USER_ID, "");
    }

    @Override
    public void setLastUid(String uid) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.PREF_USER_LAST_USER_ID, uid);
        editor.apply();
    }

    @Override
    public void setWaitingApproveProfileLevel3(boolean waitingApproveProfile) {
        SharedPreferences.Editor editor = preferences.edit();
        if (waitingApproveProfile) {
            editor.putLong(Constants.PREF_WAITING_APPROVE_PROFILE_LEVEL3, System.currentTimeMillis() / 1000);
        } else {
            editor.putLong(Constants.PREF_WAITING_APPROVE_PROFILE_LEVEL3, 0);
        }
        editor.apply();
    }

    /**
     * If user have updated ProfileLevel3 then cache this and disable UpdateProfileLevel3 UI.
     * Server'll check UpdateProfile request in 24h.
     *
     * @return is Waiting Approve ProfileLevel3
     */
    @Override
    public boolean isWaitingApproveProfileLevel3() {
        String email = preferences.getString(Constants.PREF_USER_EMAIL, "");
        String identity = preferences.getString(Constants.PREF_USER_IDENTITY_NUMBER, "");
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(identity)) {
            setWaitingApproveProfileLevel3(false);
            return false;
        }
        long lastTime = preferences.getLong(Constants.PREF_WAITING_APPROVE_PROFILE_LEVEL3, 0);
        long currentTime = System.currentTimeMillis() / 1000;
        return !(currentTime - lastTime >= 24 * 60 * 60);
    }

    @Override
    public void updateZaloPayName(String accountName) {
        if (currentUser == null) {
            return;
        }
        currentUser.zalopayname = accountName;
        saveZaloPayName(accountName);
    }

    private void saveZaloPayName(String accountName) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.PREF_USER_ZALOPAY_NAME, accountName);
        editor.apply();

        eventBus.post(new ZaloPayNameEvent(accountName));
    }

    @Override
    public void removeFingerprint() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(Constants.PREF_KEY_PASSWORD);
        editor.remove(Constants.PREF_KEY_PASSWORD_IV);
        editor.apply();
    }

    @Override
    public void useProtectAccount(boolean isUse) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Constants.PREF_USE_PROTECT_PROFILE, isUse);
        editor.apply();
    }

    @Override
    public boolean isUseProtectAccount() {
        return preferences.getBoolean(Constants.PREF_USE_PROTECT_PROFILE, true);
    }

    @Override
    public String getEncryptedPassword() {
        return preferences.getString(Constants.PREF_KEY_PASSWORD, "");
    }

    @Override
    public String getEncryptedPasswordIV() {
        return preferences.getString(Constants.PREF_KEY_PASSWORD_IV, "");
    }

    @Override
    public void setEncryptedPassword(String passwordBase64, String iv) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.PREF_KEY_PASSWORD, passwordBase64);
        editor.putString(Constants.PREF_KEY_PASSWORD_IV, iv);
        editor.apply();

    }
}
