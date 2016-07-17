package vn.com.vng.zalopay.account.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;
import vn.com.vng.zalopay.account.Constants;
import vn.com.vng.zalopay.domain.model.AbstractData;

/**
 * Created by longlv on 22/04/2016.
 */
public class ZaloProfile extends AbstractData {

    private long userId;
    private String userName;
    private String displayName;
    private String avatar;
    private String userGender;
    private String birthday;
    private String yahooId;
    private String zingMeId;
    private String facebookId;
    private String twitterId;
    private String googleId;

    public ZaloProfile() {
        this.userId = -1;
        this.userName = "";
        this.displayName = "";
        this.avatar = "";
        this.userGender = "";
        this.birthday = "";
        this.yahooId = "";
        this.zingMeId = "";
        this.facebookId = "";
        this.twitterId = "";
        this.googleId = "";
    }

    public ZaloProfile(long userId, String userName, String displayName, String avatar, String userGender, String birthday, String yahooId, String zingMeId, String facebookId, String twitterId, String googleId) {
        this.userId = userId;
        this.userName = userName;
        this.displayName = displayName;
        this.avatar = avatar;
        this.userGender = userGender;
        this.birthday = birthday;
        this.yahooId = yahooId;
        this.zingMeId = zingMeId;
        this.facebookId = facebookId;
        this.twitterId = twitterId;
        this.googleId = googleId;
    }

    public ZaloProfile(JSONObject jsonObject) throws JSONException {
        super();
        if (jsonObject == null) {
            return;
        }
//        Timber.d("Profile_jsonObject: %s", jsonObject.toString());
        userId = jsonObject.getLong(Constants.USERID);
        userName = jsonObject.getString(Constants.USERNAME);
        displayName = jsonObject.getString(Constants.DISPLAYNAME);
        avatar = jsonObject.getString(Constants.AVATAR);
        userGender = jsonObject.getString(Constants.USERGENDER);
        birthday = jsonObject.getString(Constants.BIRTHDAY);
        yahooId = jsonObject.getString(Constants.YAHOOID);
        zingMeId = jsonObject.getString(Constants.ZINGMEID);
        facebookId = jsonObject.getString(Constants.FACEBOOKID);
        twitterId = jsonObject.getString(Constants.TWITTERID);
        googleId = jsonObject.getString(Constants.GOOGLEID);
    }

    public static final Parcelable.Creator<ZaloProfile> CREATOR = new Parcelable.Creator<ZaloProfile>() {

        @Override
        public ZaloProfile createFromParcel(Parcel parcelSource) {
            // Must read values in the same order as they were placed in
            long userId = parcelSource.readLong();
            String userName = parcelSource.readString();
            String displayName = parcelSource.readString();
            String avatar = parcelSource.readString();
            String userGender = parcelSource.readString();
            String birthday = parcelSource.readString();
            String yahooId = parcelSource.readString();
            String zingMeId = parcelSource.readString();
            String facebookId = parcelSource.readString();
            String twitterId = parcelSource.readString();
            String googleId = parcelSource.readString();
            ZaloProfile user = new ZaloProfile(userId, userName, displayName, avatar, userGender, birthday, yahooId, zingMeId, facebookId, twitterId, googleId);
            return user;
        }

        @Override
        public ZaloProfile[] newArray(int size) {
            return new ZaloProfile[size];
        }

    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(userId);
        dest.writeString(userName);
        dest.writeString(displayName);
        dest.writeString(avatar);
        dest.writeString(userGender);
        dest.writeString(birthday);
        dest.writeString(yahooId);
        dest.writeString(zingMeId);
        dest.writeString(facebookId);
        dest.writeString(twitterId);
        dest.writeString(googleId);
    }

    public long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getUserGender() {
        return userGender;
    }

    public String getBirthday() {
        return birthday;
    }

    public String getYahooId() {
        return yahooId;
    }

    public String getZingMeId() {
        return zingMeId;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public String getTwitterId() {
        return twitterId;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setUserGender(String userGender) {
        this.userGender = userGender;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public void setYahooId(String yahooId) {
        this.yahooId = yahooId;
    }

    public void setZingMeId(String zingMeId) {
        this.zingMeId = zingMeId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    public void setTwitterId(String twitterId) {
        this.twitterId = twitterId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }
}
