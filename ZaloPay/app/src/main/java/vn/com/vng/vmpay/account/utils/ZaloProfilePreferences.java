package vn.com.vng.vmpay.account.utils;

import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

import vn.com.vng.vmpay.account.Constants;
import vn.com.vng.vmpay.account.models.ZaloProfile;

/**
 * Created by longlv on 22/04/2016.
 */
@Singleton
public class ZaloProfilePreferences {

    private SharedPreferences mPreferences;

    @Inject
    public ZaloProfilePreferences(SharedPreferences sharedPreferences) {
        mPreferences = sharedPreferences;
    }

    private void clearProfile() {
        setUserId(0);
        setUserName("");
        setDisplayName("");
        setAvatar("");
        setUserGender("");
        setBirthday("");
        setYahooId("");
        setZingMeId("");
        setFacebookId("");
        setTwitterId("");
        setGoogleId("");
        //clear auth zalo
        setAuthCode("");
        setChannel("");
    }

    public ZaloProfile getZaloProfile() {
        long userId = getUserId();
        String userName = getUserName();
        String displayName = getDisplayName();
        String avatar = getAvatar();
        String userGender = getUserGender();
        String birthday = getBirthday();
        String yahooId = getYahooId();
        String zingmeId = getZingMeId();
        String facebookId = getFacebookId();
        String twitterId = getTwitterId();
        String googleId = getGoogleId();
        return new ZaloProfile(userId, userName, displayName, avatar, userGender, birthday, yahooId, zingmeId, facebookId, twitterId, googleId);
    }

    public void setProfile(ZaloProfile zaloProfile) {
        if (zaloProfile == null) {
            return;
        }
        setUserId(zaloProfile.getUserId());
        setUserName(zaloProfile.getUserName());
        setDisplayName(zaloProfile.getDisplayName());
        setAvatar(zaloProfile.getAvatar());
        setUserGender(zaloProfile.getUserGender());
        setBirthday(zaloProfile.getBirthday());
        setYahooId(zaloProfile.getYahooId());
        setZingMeId(zaloProfile.getZingMeId());
        setFacebookId(zaloProfile.getFacebookId());
        setTwitterId(zaloProfile.getTwitterId());
        setGoogleId(zaloProfile.getGoogleId());
    }

    public void setAuthCode(String autnCode) {
        mPreferences.edit().putString(Constants.STORAGE_KEY_ZALO_AUTHCODE, autnCode).apply();
    }

    public String getAuthCode() {
        return mPreferences.getString(Constants.STORAGE_KEY_ZALO_AUTHCODE, "");
    }

    public void setChannel(String channel) {
        mPreferences.edit().putString(Constants.STORAGE_KEY_ZALO_CHANNEL, channel).apply();
    }

    public String getChannel() {
        return mPreferences.getString(Constants.STORAGE_KEY_ZALO_CHANNEL, "");
    }

    public void setUserId(long uid) {
        mPreferences.edit().putLong(Constants.STORAGE_KEY_ZALO_USERID, uid).apply();
    }

    public void setUserName(String userName) {
        mPreferences.edit().putString(Constants.STORAGE_KEY_ZALO_USERNAME, userName).commit();

    }

    public void setDisplayName(String displayName) {
        mPreferences.edit().putString(Constants.STORAGE_KEY_ZALO_DISPLAYNAME, displayName).commit();
    }

    public void setAvatar(String avatar) {
        mPreferences.edit().putString(Constants.STORAGE_KEY_ZALO_AVATAR, avatar).commit();
    }

    public void setUserGender(String userGender) {
        mPreferences.edit().putString(Constants.STORAGE_KEY_ZALO_USERGENDER, userGender).commit();
    }

    public void setBirthday(String birthday) {
        mPreferences.edit().putString(Constants.STORAGE_KEY_ZALO_BIRTHDAY, birthday).commit();
    }

    public void setYahooId(String yahooId) {
        mPreferences.edit().putString(Constants.STORAGE_KEY_ZALO_YAHOOID, yahooId).commit();
    }

    public void setZingMeId(String zingMeId) {
        mPreferences.edit().putString(Constants.STORAGE_KEY_ZALO_ZINGMEID, zingMeId).commit();
    }

    public void setFacebookId(String facebookId) {
        mPreferences.edit().putString(Constants.STORAGE_KEY_ZALO_FACEBOOKID, facebookId).commit();
    }

    public void setTwitterId(String twitterId) {
        mPreferences.edit().putString(Constants.STORAGE_KEY_ZALO_TWITTERID, twitterId).commit();
    }

    public void setGoogleId(String googleId) {
        mPreferences.edit().putString(Constants.STORAGE_KEY_ZALO_GOOGLEID, googleId).commit();
    }

    public long getUserId() {
        return mPreferences.getLong(Constants.STORAGE_KEY_ZALO_USERID, 0);
    }

    public String getUserName() {
        return mPreferences.getString(Constants.STORAGE_KEY_ZALO_USERNAME, "");
    }

    public String getDisplayName() {
        return mPreferences.getString(Constants.STORAGE_KEY_ZALO_DISPLAYNAME, "");
    }

    public String getAvatar() {
        return mPreferences.getString(Constants.STORAGE_KEY_ZALO_AVATAR, "");
    }

    public String getUserGender() {
        return mPreferences.getString(Constants.STORAGE_KEY_ZALO_USERGENDER, "");
    }

    public String getBirthday() {
        return mPreferences.getString(Constants.STORAGE_KEY_ZALO_BIRTHDAY, "");
    }

    public String getYahooId() {
        return mPreferences.getString(Constants.STORAGE_KEY_ZALO_YAHOOID, "");
    }

    public String getZingMeId() {
        return mPreferences.getString(Constants.STORAGE_KEY_ZALO_ZINGMEID, "");
    }

    public String getFacebookId() {
        return mPreferences.getString(Constants.STORAGE_KEY_ZALO_FACEBOOKID, "");
    }

    public String getTwitterId() {
        return mPreferences.getString(Constants.STORAGE_KEY_ZALO_TWITTERID, "");
    }

    public String getGoogleId() {
        return mPreferences.getString(Constants.STORAGE_KEY_ZALO_GOOGLEID, "");
    }
}
