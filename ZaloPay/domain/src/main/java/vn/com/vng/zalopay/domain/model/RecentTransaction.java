package vn.com.vng.zalopay.domain.model;

import org.parceler.Parcel;

/**
 * Created by longlv on 11/06/2016.
 */
@Parcel
public class RecentTransaction {

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

    public long zaloId;
    public String zaloPayId;
    public String zaloPayName;
    public String displayName;
    public String avatar;
    public int userGender;
    public String birthday;
    public boolean usingApp;
    public String phoneNumber;
    int transferType;
    public long amount;
    public String message;

    public RecentTransaction() {
    }

    public RecentTransaction(long zaloId, String zaloPayId, String userName, String displayName, String avatar, int userGender, String birthday, boolean usingApp, String phoneNumber, int transferType, long amount, String message) {
        this.zaloId = zaloId;
        this.zaloPayId = zaloPayId;
        this.zaloPayName = userName;
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

    public long getZaloId() {
        return zaloId;
    }

    public String getZaloPayId() {
        return zaloPayId;
    }

    public String getZaloPayName() {
        return zaloPayName;
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
