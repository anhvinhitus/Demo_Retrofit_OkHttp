package vn.com.vng.zalopay.transfer.models;

import android.database.Cursor;
import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;
import vn.com.vng.zalopay.account.Constants;
import vn.com.vng.zalopay.data.cache.model.TransferRecentDao;
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
    private String zaloPayId;
    private String userName;
    private String displayName;
    private String avatar;
    private int userGender;
    private String birthday;
    private boolean usingApp;
    private String phoneNumber;
    private int transferType;
    private long amount;
    private String message;

    public TransferRecent(long userId, String zaloPayId, String userName, String displayName, String avatar, int userGender, String birthday, boolean usingApp, String phoneNumber, int transferType, long amount, String message) {
        this.userId = userId;
        this.zaloPayId = zaloPayId;
        this.userName = userName;
        this.displayName = displayName;
        this.avatar = avatar;
        this.userGender = userGender;
        this.birthday = birthday;
        this.usingApp = usingApp;
        this.phoneNumber = phoneNumber;
        this.transferType = transferType;
        this.amount = amount;
        this.message = message;
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
        userGender = jsonObject.getInt(Constants.USERGENDER);
        birthday = jsonObject.getString(Constants.BIRTHDAY);
        usingApp = jsonObject.getBoolean(Constants.USINGAPP);
    }

    public TransferRecent(Cursor cursor) {
        super();
        if (cursor == null) return;
        userId = cursor.getLong(cursor.getColumnIndex(TransferRecentDao.Properties.Id.columnName));
        zaloPayId = cursor.getString(cursor.getColumnIndex(TransferRecentDao.Properties.ZaloPayId.columnName));
        userName = cursor.getString(cursor.getColumnIndex(TransferRecentDao.Properties.UserName.columnName));
        displayName = cursor.getString(cursor.getColumnIndex(TransferRecentDao.Properties.DisplayName.columnName));
        avatar = cursor.getString(cursor.getColumnIndex(TransferRecentDao.Properties.Avatar.columnName));
        userGender = cursor.getInt(cursor.getColumnIndex(TransferRecentDao.Properties.UserGender.columnName));
        birthday = cursor.getString(cursor.getColumnIndex(TransferRecentDao.Properties.Birthday.columnName));
        usingApp = cursor.getInt(cursor.getColumnIndex(TransferRecentDao.Properties.UsingApp.columnName)) ==1?true:false;
        phoneNumber = cursor.getString(cursor.getColumnIndex(TransferRecentDao.Properties.PhoneNumber.columnName));
        transferType = cursor.getInt(cursor.getColumnIndex(TransferRecentDao.Properties.TransferType.columnName));
        amount = cursor.getLong(cursor.getColumnIndex(TransferRecentDao.Properties.Amount.columnName));
        message = cursor.getString(cursor.getColumnIndex(TransferRecentDao.Properties.Message.columnName));
    }

    public static final Creator<TransferRecent> CREATOR = new Creator<TransferRecent>() {

        @Override
        public TransferRecent createFromParcel(Parcel parcelSource) {
            // Must read values in the same order as they were placed in
            long userId = parcelSource.readLong();
            String zaloPayId = parcelSource.readString();
            String userName = parcelSource.readString();
            String displayName = parcelSource.readString();
            String avatar = parcelSource.readString();
            int userGender = parcelSource.readInt();
            String birthday = parcelSource.readString();
            boolean usingApp = parcelSource.readInt() == 1 ? true : false;
            String phoneNumber = parcelSource.readString();
            int transferType = parcelSource.readInt();
            long amount= parcelSource.readLong();
            String message = parcelSource.readString();
            TransferRecent user = new TransferRecent(userId, zaloPayId, userName, displayName, avatar, userGender, birthday, usingApp, phoneNumber, transferType, amount, message);
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
        dest.writeString(zaloPayId);
        dest.writeString(userName);
        dest.writeString(displayName);
        dest.writeString(avatar);
        dest.writeInt(userGender);
        dest.writeString(birthday);
        dest.writeInt(usingApp ? 1 : 0);
        dest.writeString(phoneNumber);
        dest.writeInt(transferType);
        dest.writeLong(amount);
        dest.writeString(message);
    }

    public long getUserId() {
        return userId;
    }

    public String getZaloPayId() {
        return zaloPayId;
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

    public String getBirthday() {
        return birthday;
    }

    public boolean isUsingApp() {
        return usingApp;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public int getTransferType() {
        return transferType;
    }

    public long getAmount() {
        return amount;
    }

    public String getMessage() {
        return message;
    }
}
