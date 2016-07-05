package vn.com.vng.zalopay.transfer.models;

import android.database.Cursor;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import timber.log.Timber;
import vn.com.vng.zalopay.account.Constants;
import vn.com.vng.zalopay.data.cache.model.TransferRecentDao;

/**
 * Created by longlv on 11/06/2016.
 */
@Parcel
public class TransferRecent {

    public enum TransferType {
        ZALO_PAY(1), ATM_VISA(2);

        private final int value;

        TransferType(int value) {
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

    long userId;
    String zaloPayId;
    String userName;
    String displayName;
    String avatar;
    int userGender;
    String birthday;
    boolean usingApp;
    String phoneNumber;
    int transferType;
    long amount;
    String message;

    public TransferRecent() {

    }
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
//        Timber.d("Profile_jsonObject: %s", jsonObject.toString());
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
        usingApp = cursor.getInt(cursor.getColumnIndex(TransferRecentDao.Properties.UsingApp.columnName)) == 1;
        phoneNumber = cursor.getString(cursor.getColumnIndex(TransferRecentDao.Properties.PhoneNumber.columnName));
        transferType = cursor.getInt(cursor.getColumnIndex(TransferRecentDao.Properties.TransferType.columnName));
        amount = cursor.getLong(cursor.getColumnIndex(TransferRecentDao.Properties.Amount.columnName));
        message = cursor.getString(cursor.getColumnIndex(TransferRecentDao.Properties.Message.columnName));
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
