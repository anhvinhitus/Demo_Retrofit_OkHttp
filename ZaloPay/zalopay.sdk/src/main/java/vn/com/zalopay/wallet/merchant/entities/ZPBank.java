package vn.com.zalopay.wallet.merchant.entities;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import vn.com.zalopay.wallet.constants.BankStatus;

/**
 * used by app
 */

public class ZPBank implements Parcelable {
    public static final Creator<ZPBank> CREATOR = new Creator<ZPBank>() {
        @Override
        public ZPBank createFromParcel(Parcel source) {
            return new ZPBank(source);
        }

        @Override
        public ZPBank[] newArray(int size) {
            return new ZPBank[size];
        }
    };
    public String bankCode;
    public String bankLogo;
    public Boolean isBankAccount = false;
    public String bankName;
    @BankStatus
    public int bankStatus = BankStatus.ACTIVE;
    public String bankMessage;

    public ZPBank(String pCardCode) {
        this.bankCode = pCardCode;
    }

    ZPBank(Parcel in) {
        this.bankCode = in.readString();
        this.bankLogo = in.readString();
        this.isBankAccount = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.bankName = in.readString();
        this.bankStatus = in.readInt();
        this.bankMessage = in.readString();
    }

    public ZPBank(ZPBank clone) {
        this.bankCode = clone.bankCode;
        this.bankLogo = clone.bankLogo;
        this.isBankAccount = clone.isBankAccount;
        this.bankName = clone.bankName;
        this.bankStatus = clone.bankStatus;
        this.bankMessage = clone.bankMessage;
    }

    public void setBankStatus(@BankStatus int bankStatus) {
        this.bankStatus = bankStatus;
    }

    public Boolean isBankAccount() {
        return isBankAccount;
    }

    public void setIsBankAccount(Boolean pIsBankAccount) {
        isBankAccount = pIsBankAccount;
    }

    @Override
    public boolean equals(Object object) {
        boolean sameSame = false;

        if (object != null && object instanceof ZPBank) {
            ZPBank other = (ZPBank) object;

            if (!TextUtils.isEmpty(((ZPBank) object).bankCode))
                sameSame = this.bankCode.equals(other.bankCode);
        }

        return sameSame;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.bankCode);
        dest.writeString(this.bankLogo);
        dest.writeValue(this.isBankAccount);
        dest.writeString(this.bankName);
        dest.writeInt(this.bankStatus);
        dest.writeString(this.bankMessage);
    }
}
