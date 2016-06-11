package vn.com.vng.zalopay.transfer.models;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;
import vn.com.vng.zalopay.account.Constants;
import vn.com.vng.zalopay.domain.model.AbstractData;

/**
 * Created by longlv on 11/06/2016.
 */
public class TransferRecent extends AbstractData {

    public enum TransferType {
        ZALO_PAY(1), ATM_VISA(2);

        private final int value;

        private TransferType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    private long userId;
    private String userName;
    private String displayName;
    private String avatar;
    private String userGender;
    private String birthday;
    private String usingApp;
    private String phoneNumber;
    private int transferType;

    public TransferRecent(long userId, String userName, String displayName, String avatar, String userGender, String birthday, String usingApp, String phoneNumber, int transferType) {
        this.userId = userId;
        this.userName = userName;
        this.displayName = displayName;
        this.avatar = avatar;
        this.userGender = userGender;
        this.birthday = birthday;
        this.usingApp = usingApp;
        this.phoneNumber = phoneNumber;
        this.transferType = transferType;
    }

    public TransferRecent(JSONObject jsonObject) throws JSONException {
        super();
        if (jsonObject == null) {
            return;
        }
        Timber.d("Profile_jsonObject: %s", jsonObject.toString());
        userId = jsonObject.getLong(Constants.USERID);
        userName = jsonObject.getString(Constants.USERNAME);
        displayName = jsonObject.getString(Constants.DISPLAYNAME);
        avatar = jsonObject.getString(Constants.AVATAR);
        userGender = jsonObject.getString(Constants.USERGENDER);
        birthday = jsonObject.getString(Constants.BIRTHDAY);
        usingApp = jsonObject.getString(Constants.USINGAPP);
    }

    public static final Creator<TransferRecent> CREATOR = new Creator<TransferRecent>() {

        @Override
        public TransferRecent createFromParcel(Parcel parcelSource) {
            // Must read values in the same order as they were placed in
            long userId = parcelSource.readLong();
            String userName = parcelSource.readString();
            String displayName = parcelSource.readString();
            String avatar = parcelSource.readString();
            String userGender = parcelSource.readString();
            String birthday = parcelSource.readString();
            String usingApp = parcelSource.readString();
            String phoneNumber = parcelSource.readString();
            int transferType = parcelSource.readInt();

            TransferRecent user = new TransferRecent(userId, userName, displayName, avatar, userGender, birthday, usingApp, phoneNumber, transferType);
            return user;
        }

        @Override
        public TransferRecent[] newArray(int size) {
            return new TransferRecent[size];
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
        dest.writeString(usingApp);
        dest.writeString(phoneNumber);
        dest.writeInt(transferType);
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

    public String getUsingApp() {
        return usingApp;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public int getTransferType() {
        return transferType;
    }
}
