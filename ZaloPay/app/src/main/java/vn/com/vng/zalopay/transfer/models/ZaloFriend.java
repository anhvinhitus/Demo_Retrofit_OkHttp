package vn.com.vng.zalopay.transfer.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;
import vn.com.vng.zalopay.account.Constants;
import vn.com.vng.zalopay.domain.model.AbstractData;

/**
 * Created by longlv on 11/06/2016.
 */
public class ZaloFriend extends AbstractData {

    private long userId;
    private String userName;
    private String displayName;
    private String avatar;
    private int userGender;
    private boolean usingApp;

    public ZaloFriend(long userId, String userName, String displayName, String avatar, int userGender, boolean usingApp) {
        this.userId = userId;
        this.userName = userName;
        this.displayName = displayName;
        this.avatar = avatar;
        this.userGender = userGender;
        this.usingApp = usingApp;
    }

    public ZaloFriend(JSONObject jsonObject) throws JSONException {
        super();
        if (jsonObject == null) {
            return;
        }
        Timber.d("Profile_jsonObject: %s", jsonObject.toString());
        userId = jsonObject.getLong(Constants.USERID);
        userName = jsonObject.getString(Constants.USERNAME);
        displayName = jsonObject.getString(Constants.DISPLAYNAME);
        avatar = jsonObject.getString(Constants.AVATAR);
        userGender = jsonObject.getInt(Constants.USERGENDER);
        usingApp = jsonObject.getBoolean(Constants.USINGAPP);
    }

    public static final Parcelable.Creator<ZaloFriend> CREATOR = new Parcelable.Creator<ZaloFriend>() {

        @Override
        public ZaloFriend createFromParcel(Parcel parcelSource) {
            // Must read values in the same order as they were placed in
            long userId = parcelSource.readLong();
            String userName = parcelSource.readString();
            String displayName = parcelSource.readString();
            String avatar = parcelSource.readString();
            int userGender = parcelSource.readInt();
            boolean usingApp = parcelSource.readInt()==1?true:false;
            ZaloFriend user = new ZaloFriend(userId, userName, displayName, avatar, userGender, usingApp);
            return user;
        }

        @Override
        public ZaloFriend[] newArray(int size) {
            return new ZaloFriend[size];
        }

    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(userId);
        dest.writeString(userName);
        dest.writeString(displayName);
        dest.writeString(avatar);
        dest.writeInt(userGender);
        dest.writeInt(usingApp?1:0);
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

    public int getUserGender() {
        return userGender;
    }

    public boolean isUsingApp() {
        return usingApp;
    }
}
